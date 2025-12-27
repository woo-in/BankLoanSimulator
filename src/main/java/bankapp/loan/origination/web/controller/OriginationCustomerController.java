package bankapp.loan.origination.web.controller;


import bankapp.core.common.SessionConst;
import bankapp.loan.origination.component.BriefDsrCalculator;
import bankapp.loan.origination.service.LoanContractService;
import bankapp.loan.origination.service.LoanOriginationService;
import bankapp.loan.origination.web.request.FinancialInfoRequest;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.loan.origination.web.request.ApplicationRequest;
import bankapp.member.model.Member;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import static bankapp.core.common.SessionConst.PENDING_LOAN_ID;

@Slf4j
@Controller
@RequestMapping("/loan")
public class OriginationCustomerController {

    private final CreditLoanProductService creditLoanProductService;
    private final LoanContractService loanContractService;
    private final BriefDsrCalculator briefDsrCalculator;
    private final LoanOriginationService loanOriginationService;

    public OriginationCustomerController(CreditLoanProductService creditLoanProductService,
                                         LoanContractService loanContractService,
                                         BriefDsrCalculator briefDsrCalculator,
                                         LoanOriginationService loanOriginationService) {
        this.creditLoanProductService = creditLoanProductService;
        this.loanContractService = loanContractService;
        this.briefDsrCalculator = briefDsrCalculator;
        this.loanOriginationService = loanOriginationService;
    }


    // todo : pendingLoan 이 대출 전반의 과정에 관여 하도록 리펙터링

    @RequestMapping("/credit/{type}/inquiry")
    public String showLoanInquiryForm(@PathVariable("type") String type,
                                      Model model,
                                      @SessionAttribute(value = SessionConst.LOGIN_MEMBER, required = false) Member loginMember){

        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
        model.addAttribute("loanProductInfoResponse" , loanProductInfoResponse);

        List<ExistingLoanResponse> existingLoanResponses = loanContractService.findAllContractResponsesByMember(loginMember);
        model.addAttribute("existingLoanContracts", existingLoanResponses);

        return "loan/credit/user-input";
    }


    @PostMapping("/credit/{type}/calculate")
    public String processLoanInquiry(@PathVariable("type") String type,
                                     @Valid @ModelAttribute FinancialInfoRequest userInfoRequest,
                                     @SessionAttribute(value = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                                     HttpSession session,
                                     Model model) {

        List<ExistingLoanResponse> allExistingLoans = loanOriginationService.getIntegratedLoanList(loginMember, userInfoRequest);

        BigDecimal currentDsrResponse = briefDsrCalculator.calculate(userInfoRequest, allExistingLoans);
        InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, userInfoRequest, allExistingLoans);
        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);


        // todo : 새로 고침할 때마다 호출 , 멱등성 처리 필요할 수도 있음 (전체적으로 생각)
        Long savedApplicationId = loanOriginationService.startOrigination(loginMember,type , userInfoRequest ,allExistingLoans);
        session.setAttribute(PENDING_LOAN_ID, savedApplicationId);

        model.addAttribute("loanProductInfoResponse", loanProductInfoResponse);
        model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
        model.addAttribute("currentDsrResponse", currentDsrResponse);


        return "loan/credit/customer-product-detail";
    }

    @GetMapping("/credit/{type}/apply")
    public String showLoanApplyForm(@PathVariable("type") String type,
                                    @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                                    Model model) {

        if (pendingLoanApplicationId == null) {
            log.warn("유효하지 않은 접근입니다. (PENDING_LOAN_ID 없음) - Slug: {}", type);
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
        InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, pendingLoanApplicationId);

        model.addAttribute("loanProductInfoResponse" , loanProductInfoResponse);
        model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
        model.addAttribute("newApplicationRequest", new ApplicationRequest());

        return "loan/credit/apply-form";

    }

    @PostMapping("/credit/{type}/apply")
    public String processLoanApplication(@PathVariable("type") String type,
                                         @Valid @ModelAttribute("newApplicationRequest") ApplicationRequest request,
                                         BindingResult bindingResult,
                                         @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                                         Model model,
                                         HttpSession session) {



        if (pendingLoanApplicationId == null) {
            log.warn("유효하지 않은 접근입니다. (PENDING_LOAN_ID 없음) - Slug: {}", type);
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        // todo : 유효성 검사는 일단 not null 만 체크 (추가로 체크 가능)
        if(bindingResult.hasErrors()){
            LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
            InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, pendingLoanApplicationId);
            model.addAttribute("loanProductInfoResponse" , loanProductInfoResponse);
            model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
            return "loan/credit/apply-form";
        }

        loanOriginationService.submitLoanApplication(pendingLoanApplicationId, request);


        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
        InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, pendingLoanApplicationId);
        model.addAttribute("loanProductInfoResponse" , loanProductInfoResponse);
        model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
        return "loan/credit/apply-form";
    }







    private void populateLoanInfoModel(String productSlug, Long pendingApplicationId, Model model) {
        LoanProductInfoResponse productInfo = creditLoanProductService.getLoanProductInfo(productSlug);
        InterestRateInfoResponse rateInfo = loanOriginationService.calculateInterestRate(productSlug, pendingApplicationId);

        model.addAttribute("loanProductInfoResponse", productInfo);
        model.addAttribute("interestRateInfoResponse", rateInfo);
    }




}
