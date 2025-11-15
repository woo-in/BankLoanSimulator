package bankapp.loan.web.controller.home;

import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.loan.model.common.application.LoanApplication;
import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.service.common.application.LoanApplicationService;
import bankapp.loan.service.common.contract.LoanContractService;
import bankapp.loan.service.common.product.LoanProductService;
import bankapp.loan.service.common.rate.InterestRateTypeService;
import bankapp.loan.service.common.repayment.RepaymentMethodService;
import bankapp.loan.service.credit.CreditLoanService;
import bankapp.loan.web.request.InterestRateTypeRequest;
import bankapp.loan.web.request.LoanProductRequest;
import bankapp.loan.web.request.RepaymentMethodRequest;
import bankapp.loan.web.response.InterestRateTypeInfoResponse;
import bankapp.loan.web.response.LoanApplicationCompleteResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.loan.web.response.RepaymentMethodInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/temp-admin")
public class TempAdminController {

    private final RepaymentMethodService repaymentMethodService;
    private final InterestRateTypeService interestRateTypeService;
    private final LoanProductService loanProductService;
    private final CreditLoanService creditLoanService;
    private final LoanApplicationService loanApplicationService;
    private final LoanContractService loanContractService;

    public TempAdminController(RepaymentMethodService repaymentMethodService,
                               InterestRateTypeService interestRateTypeService,
                               LoanProductService loanProductService,
                               CreditLoanService creditLoanService,
                               LoanApplicationService loanApplicationService,
                               LoanContractService loanContractService) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
        this.loanProductService = loanProductService;
        this.creditLoanService = creditLoanService;
        this.loanApplicationService = loanApplicationService;
        this.loanContractService = loanContractService;
    }

    // 임시 관리 홈
    @GetMapping
    public String tempAdminHome( ){
        return "loan/temp-admin/tempHome";
    }

    // 상환 방법 설정 페이지
    @GetMapping("/repayment-methods")
    public String showRepaymentMethods(Model model){

        // DB 에서 조회
        List<RepaymentMethod> methods = repaymentMethodService.findAllMethods();
        // response 객체 바인딩
        List<RepaymentMethodInfoResponse> repaymentMethodInfoResponses = new ArrayList<>();
        for(RepaymentMethod method : methods){
            repaymentMethodInfoResponses.add(RepaymentMethodInfoResponse.from(method));
        }
        // Model 에 담아서 View 로 전달
        model.addAttribute("repaymentMethodInfoResponses" , repaymentMethodInfoResponses);
        model.addAttribute("newRepaymentMethod" , new RepaymentMethodRequest());

        return "loan/temp-admin/repaymentMethods";
    }

    @PostMapping("/repayment-methods")
    public String registerRepaymentMethod(@ModelAttribute("newRepaymentMethod") RepaymentMethodRequest requestDto) {
        repaymentMethodService.saveRepayment(requestDto);
        return "redirect:/temp-admin/repayment-methods";
    }

    // 금리 유형 설정 페이지
    @GetMapping("/interest-types")
    public String showInterestTypes(Model model) {

        List<InterestRateType> types = interestRateTypeService.findAllTypes(); // (findAllTypes() 가정)

        // 2. response 객체 바인딩
        List<InterestRateTypeInfoResponse> interestTypeInfoResponses = new ArrayList<>();
        for(InterestRateType type : types){
            interestTypeInfoResponses.add(InterestRateTypeInfoResponse.from(type));
        }

        // 3. Model 에 담아서 View 로 전달
        model.addAttribute("interestTypeInfoResponses" , interestTypeInfoResponses);
        model.addAttribute("newInterestType" , new InterestRateTypeRequest());

        return "loan/temp-admin/interestTypes";
    }

    @PostMapping("/interest-types")
    public String registerInterestType(@ModelAttribute("newInterestType") InterestRateTypeRequest requestDto) {

        interestRateTypeService.saveInterestRateType(requestDto.toEntity());
        // 3. 목록 페이지로 리다이렉트
        return "redirect:/temp-admin/interest-types";
    }

    // 대출 상품 추가 페이지
    @GetMapping("/loan-products")
    public String showLoanProducts(Model model) {

        // 상환 방법 보여주기
        // TODO : 중복 코드 메서드 화
        List<RepaymentMethod> methods = repaymentMethodService.findAllMethods();
        List<RepaymentMethodInfoResponse> repaymentMethodInfoResponses = new ArrayList<>();
        for(RepaymentMethod method : methods){
            if(method.getIsActive()) {
                repaymentMethodInfoResponses.add(RepaymentMethodInfoResponse.from(method));
            }
        }
        model.addAttribute("repaymentMethodInfoResponses" , repaymentMethodInfoResponses);


        // 금리 종류 보여주기
        // TODO : 중복 코드 메서드 화
        List<InterestRateType> types = interestRateTypeService.findAllTypes(); // (findAllTypes() 가정)
        List<InterestRateTypeInfoResponse> interestTypeInfoResponses = new ArrayList<>();
        for(InterestRateType type : types){
            if(type.getIsActive()) {
                interestTypeInfoResponses.add(InterestRateTypeInfoResponse.from(type));
            }
        }
        model.addAttribute("interestTypeInfoResponses" , interestTypeInfoResponses);


        // 현재 등록 대출 상품 보여주기
        List<LoanProduct> products = loanProductService.findAllTypes();
        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();
        for(LoanProduct product : products){
            loanProductInfoResponses.add(LoanProductInfoResponse.from(product));
        }
        model.addAttribute("loanProductInfoResponses" , loanProductInfoResponses);
        // TODO: 임시로 무조건 신용대출만 가능하게 함.
        // TODO : 대출 단위 , 최소 최대 입력받을 때 검증해야함 예를들어 , 최저 한도가 500 최고 한도가 50 ?
        model.addAttribute("newLoanProduct" , new LoanProductRequest());

        return  "loan/temp-admin/loanProducts";
    }

    @PostMapping("/loan-products")
    public String registerInterestType(@ModelAttribute("newLoanProduct") LoanProductRequest loanProductRequest) {
        creditLoanService.saveCreditLoanProduct(loanProductRequest);
        return "redirect:/temp-admin/loan-products";
    }

    // 대출 신청 관리 페이지
    @GetMapping("loan-applications")
    public String showLoanApplications(Model model) {

        List<LoanApplicationCompleteResponse> loanApplicationCompleteResponses = new ArrayList<>();
        List<LoanApplication> loanApplications = loanApplicationService.getAppliedApplications();

        for(LoanApplication loanApplication : loanApplications){
            loanApplicationCompleteResponses.add(LoanApplicationCompleteResponse.from(loanApplication));
        }

        model.addAttribute("loanApplicationCompleteResponses", loanApplicationCompleteResponses);
        return "loan/temp-admin/loanApplications";

    }

    // 거절
    @PostMapping("/loan-applications/{id}/reject")
    public String rejectLoanApplication(@PathVariable("id") Long applicationId) {

        try {
            loanApplicationService.rejectApplication(applicationId);
        } catch (Exception e) {
            log.error("대출 거절 처리 중 오류 발생", e);
            // 필요 시 에러 페이지나 플래시 메시지 처리
        }

        return "redirect:/temp-admin/loan-applications";
    }

    // 승인
    @PostMapping("/loan-applications/{id}/approve")
    public String approveLoanApplication(@PathVariable("id") Long applicationId) {

        try{

            // 1. 승인
            loanApplicationService.approveApplication(applicationId);

            // 2. 신청서 찾기
            LoanApplication loanApplication = loanApplicationService.findById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대출 신청 정보를 찾을 수 없습니다. ID: " + applicationId));

            // 3. 대출 계좌 및 계약서 작성 , 입금 진행 , 스케줄러 작성 <하나의 트랜잭션으로 이뤄져야 함>
            OpenLoanAccountRequest openLoanAccountRequest = new OpenLoanAccountRequest();
            openLoanAccountRequest.setMemberId(loanApplication.getMember().getMemberId());
            // 원금만큼 대출 계좌 +
            openLoanAccountRequest.setBalance(loanApplication.getLoanAmount());
            openLoanAccountRequest.setNickname("대출계좌");

            loanContractService.saveLoanContract(openLoanAccountRequest ,loanApplication );

            // 4. 입금 진행 , 스케줄러 작성


        }catch (Exception e){
            log.error("대출 성공 처리 중 오류 발생", e);
        }

        return "redirect:/temp-admin/loan-applications";

    }







    // todo : 승인
    // 1. 대출 신청 승인 (신청에서 상태 바꾸기)

    // 2. 대출 약정 체결 (LoanContract)
    // 3. 대출 실행 (대출 계좌 생성 , 입금 진행)
    // 4. 상환 스케줄 생성 고려사항 : (대출금리 , 금리종류 , 상환방법)



    // 추가적 고려 사항 : 중도 상환시 , 연체시 , 금리인하 요구시)
    // 그리고 더 ...
    // 휴일 처리 (Business Day Convention)
    // 막달 단수 조정 (Last Payment Adjustment)
    // 변동 금리 재산정 (Variable Rate Recalculation)
    // 초회차 기산일 처리 (Broken Period Interest)


}
