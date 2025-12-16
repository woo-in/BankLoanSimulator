package bankapp.loan.web.controller;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.product.model.CreditLoanProduct;
import bankapp.loan.origination.service.LoanApplicationService;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.web.request.CreditCheckRequest;
import bankapp.loan.web.request.LoanApplicationRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import bankapp.loan.web.response.LoanApplicationCompleteResponse;
import bankapp.loan.web.response.LoanApplicationFormResponse;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import bankapp.member.model.Member;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller
@RequestMapping("/loan")
public class LoanHomeController {

    private final CreditLoanProductService creditLoanProductService;
    private final LoanApplicationService loanApplicationService;
    private final InterestRateCalculator interestRateCalculator;

    public LoanHomeController(CreditLoanProductService creditLoanProductService,
                              LoanApplicationService loanApplicationService,
                              InterestRateCalculator interestRateCalculator) {
        this.creditLoanProductService = creditLoanProductService;
        this.loanApplicationService = loanApplicationService;
        this.interestRateCalculator = interestRateCalculator;
    }


    @RequestMapping("/credit/{type}/inquiry")
    public String showLoanInquiryForm(@PathVariable("type") String type, Model model){

        CreditLoanProduct creditLoanProduct = creditLoanProductService.findCreditLoanProductByLoanProductSlug(type);
        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);
        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);

        return "loan/credit/user-input";
    }




    @PostMapping("/credit/{type}/calculate")
    public String processLoanInquiry(@PathVariable("type") String type,
                                     @Valid @ModelAttribute CreditCheckRequest creditCheckRequest,
                                     Model model,
                                     HttpSession session) {

        // 입력을 바탕으로 신용등급 계산 , 고객 유저 금리 정보 계산
        InterestRateInfoResponse interestRateInfoResponse = interestRateCalculator.calculateInterestRateInfo(type,creditCheckRequest);

        // 상품 정보
        CreditLoanProduct creditLoanProduct = creditLoanProductService.findCreditLoanProductByLoanProductSlug(type);
        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);

        // 견적정보 세션에 추가
        session.setAttribute("loanProductInfoResponse" , loanProductInfoResponse);
        session.setAttribute("interestRateInfoResponse", interestRateInfoResponse);

        // 추가
        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);
        model.addAttribute("InterestRateInfoResponse" , interestRateInfoResponse);

        return "loan/credit/customer-product-detail";

    }

    // 대출 신청
    @GetMapping("/credit/{type}/apply")
    public String showLoanApplyForm(@PathVariable("type") String type,
                                    Model model,
                                    HttpSession session) {

        LoanProductInfoResponse loanProductInfoResponse = (LoanProductInfoResponse) session.getAttribute("loanProductInfoResponse");
        InterestRateInfoResponse interestRateInfoResponse = (InterestRateInfoResponse) session.getAttribute("interestRateInfoResponse");

        if (loanProductInfoResponse == null || interestRateInfoResponse == null || !loanProductInfoResponse.getLoanProductSlug().equals(type)) {
            log.warn("[비정상 접근] /apply 호출. 금리 조회 단계로 리다이렉트. Slug: {}", type);
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        LoanApplicationFormResponse formResponse = LoanApplicationFormResponse.from(creditLoanProductService.findCreditLoanProductByLoanProductSlug(type) , interestRateInfoResponse);

        model.addAttribute("formSetup", formResponse);
        model.addAttribute("newApplicationRequest", new LoanApplicationRequest());

        return "loan/credit/apply-form";

    }

    @PostMapping("/credit/{type}/apply")
    public String processLoanApplication(@PathVariable("type") String type,
                                         @Valid @ModelAttribute("newApplicationRequest") LoanApplicationRequest request,
                                         BindingResult bindingResult,
                                         Model model,
                                         HttpSession session) {

        // 1. 세션에서 견적 정보 다시 확인 (위조된 POST 방지)
        LoanProductInfoResponse productInfo = (LoanProductInfoResponse) session.getAttribute("loanProductInfoResponse");
        InterestRateInfoResponse rateInfo = (InterestRateInfoResponse) session.getAttribute("interestRateInfoResponse");
        Member member = (Member) session.getAttribute("loginMember");

        // 2. 비정상 접근 차단 (세션 없거나, URL의 {type}이 세션의 slug와 다르거나)
        if (productInfo == null || rateInfo == null || !productInfo.getLoanProductSlug().equals(type)) {
            log.warn("[비정상 접근] /apply(POST) 호출. 금리 조회 단계로 리다이렉트. Slug: {}", type);
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        // 3. 폼 유효성 검사 (예: <select>에서 아무것도 선택하지 않은 경우)
        if (bindingResult.hasErrors()) {
            log.warn("대출 신청 폼 유효성 검사 실패: {}", bindingResult.getAllErrors());
            // [중요] 폼으로 다시 돌려보낼 때, 'formSetup' 객체를 다시 만들어 보내줘야 함

            LoanApplicationFormResponse formResponse = LoanApplicationFormResponse.from(creditLoanProductService.findCreditLoanProductByLoanProductSlug(type) , rateInfo);

            model.addAttribute("formSetup", formResponse);
            // newApplicationRequest는 이미 model에 담겨있음
            return "loan/credit/apply-form"; // 에러와 함께 폼 뷰를 다시 렌더링
        }

        // 4. (보안 강화) 서버에서 한번 더 금액/기간이 상품의 min/max/unit 정책에 맞는지 검증
        // TODO: creditLoanService.validateApplicationRequest(request, productInfo);

        // 5. [핵심] 서비스 레이어에 신청 데이터 저장 요청
        // TODO : 예외처리 try - catch
        LoanApplication loanApplication = loanApplicationService.saveLoanApplication(request, productInfo, rateInfo, member);
        log.info("대출 신청 처리 성공: {}", request);

        // 6. 신청 완료 후 세션 비우기 (중복 제출 방지)
        session.removeAttribute("loanProductInfoResponse");
        session.removeAttribute("interestRateInfoResponse");
        session.setAttribute("loanApplication", loanApplication);

        // 7. 신청 완료 페이지로 리다이렉트
        return "redirect:/loan/complete"; // (예시: 신청 완료 페이지)
    }


    // TODO : 시간 +1시간 문제 (2시에 신청했는데 3시로 신청)
    // TODO : 리다이렉트 -> 뒤로가기 문제
    @GetMapping("/complete")
    public String showLoanCompleteForm(Model model, HttpSession session){
            LoanApplication loanApplication = (LoanApplication) session.getAttribute("loanApplication");
            model.addAttribute("completedApplication" , LoanApplicationCompleteResponse.from(loanApplication));
            session.removeAttribute("loanApplication");
            return "loan/credit/apply-complete";
    }

    // todo : 고정금리인지 변동금리인지에 따라 아얘 path 를 달리해야 함. 


}

