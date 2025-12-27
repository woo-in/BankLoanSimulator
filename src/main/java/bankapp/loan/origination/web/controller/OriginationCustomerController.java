package bankapp.loan.origination.web.controller;


import bankapp.core.common.SessionConst;
import bankapp.loan.origination.component.BriefDsrCalculator;
import bankapp.loan.origination.model.ApplicationStatus;
import bankapp.loan.origination.service.LoanContractService;
import bankapp.loan.origination.service.LoanOriginationService;
import bankapp.loan.origination.web.request.ApplicationAuthRequest;
import bankapp.loan.origination.web.request.FinancialInfoRequest;
import bankapp.loan.origination.web.response.ApplicationResponse;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.loan.origination.web.request.ApplicationRequest;
import bankapp.loan.servicing.component.AmortizationCalculator;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.model.Member;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
    private final AmortizationCalculator amortizationCalculator;

    public OriginationCustomerController(CreditLoanProductService creditLoanProductService,
                                         LoanContractService loanContractService,
                                         BriefDsrCalculator briefDsrCalculator,
                                         LoanOriginationService loanOriginationService,
                                         AmortizationCalculator amortizationCalculator) {
        this.creditLoanProductService = creditLoanProductService;
        this.loanContractService = loanContractService;
        this.briefDsrCalculator = briefDsrCalculator;
        this.loanOriginationService = loanOriginationService;
        this.amortizationCalculator = amortizationCalculator;
    }


    // todo : pendingLoan 이 대출 전반의 과정에 관여 하도록 리펙터링 (must refactor)
    // todo : 각 단계마다 , 고유의 상태로 관리해야 할듯 (같은 상태여도 뛰면 ?) - 가능

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

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.DRAFT) {
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
                                         Model model) {

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.DRAFT) {
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

        return "redirect:/loan/credit/" + type + "/apply/description";
    }


    @GetMapping("/credit/{type}/apply/description")
    public String showProductDescription(@PathVariable("type") String type,
                                         @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                                         Model model) {

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.PRE_CHECKED) {
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
        InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, pendingLoanApplicationId);
        ApplicationResponse applicationResponse = loanOriginationService.getApplicationResponse(pendingLoanApplicationId);

        BigDecimal estimatedPayment = amortizationCalculator.calculateFirstMonthEstimatedPayment(
                applicationResponse.getLoanAmount(),
                applicationResponse.getLoanTerm(),
                interestRateInfoResponse.getMinFinalInterestRate(),
                applicationResponse.getRepaymentMethod()
        );
        model.addAttribute("loanProductInfoResponse" , loanProductInfoResponse);
        model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
        model.addAttribute("applicationResponse", applicationResponse);
        model.addAttribute("monthlyPayment", estimatedPayment);


        return "loan/credit/product-description";
    }

    @GetMapping("/credit/{type}/apply/terms")
    public String showTermsAgreement(@PathVariable("type") String type,
                                     @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                                     Model model) {

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.PRE_CHECKED) {
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);
        model.addAttribute("loanProductInfoResponse", loanProductInfoResponse);

        return "loan/credit/terms-agreement";
    }


    @GetMapping("/credit/{type}/apply/auth")
    public String showAuthForm(@PathVariable("type") String type,
                               @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                               Model model){

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.PRE_CHECKED) {
            return "redirect:/loan/credit/" + type + "/inquiry";
        }

        model.addAttribute("productSlug", type);
        model.addAttribute("applicationAuthRequest" , new ApplicationAuthRequest());
        return "/loan/credit/loan-auth-form";
    }


    // todo : 유저 입장에서 대출 신청 한 것 확인할 수 있는 폼 필요할듯
    @PostMapping("/credit/{type}/apply/complete")
    public String completeLoanApplication(@PathVariable("type") String type,
                                          @SessionAttribute(value = SessionConst.PENDING_LOAN_ID, required = false) Long pendingLoanApplicationId,
                                          @Validated @ModelAttribute ApplicationAuthRequest applicationAuthRequest,
                                          BindingResult bindingResult,
                                          Model model){

        if (pendingLoanApplicationId == null || loanOriginationService.getApplicationStatus(pendingLoanApplicationId) != ApplicationStatus.PRE_CHECKED) {
            return "redirect:/loan/credit/" + type + "/inquiry";
        }
        if(bindingResult.hasErrors()){
            model.addAttribute("productSlug", type);
            return "loan/credit/loan-auth-form";
        }


        try {
            loanOriginationService.completeOrigination(pendingLoanApplicationId ,applicationAuthRequest);
            return "loan/credit/application-complete";
        }catch(IncorrectPasswordException e){
            bindingResult.rejectValue("password", "invalid", "비밀번호가 일치하지 않습니다.");
        }


        model.addAttribute("productSlug", type);
        return "loan/credit/loan-auth-form";

    }




















    private void populateLoanInfoModel(String productSlug, Long pendingApplicationId, Model model) {
        LoanProductInfoResponse productInfo = creditLoanProductService.getLoanProductInfo(productSlug);
        InterestRateInfoResponse rateInfo = loanOriginationService.calculateInterestRate(productSlug, pendingApplicationId);

        model.addAttribute("loanProductInfoResponse", productInfo);
        model.addAttribute("interestRateInfoResponse", rateInfo);
    }
}
