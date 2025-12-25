package bankapp.loan.origination.web.controller;


import bankapp.core.common.SessionConst;
import bankapp.loan.origination.component.BriefDsrCalculator;
import bankapp.loan.origination.service.LoanContractService;
import bankapp.loan.origination.service.LoanOriginationService;
import bankapp.loan.origination.web.request.UserFinancialInfoRequest;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.member.model.Member;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

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
                                     @Valid @ModelAttribute UserFinancialInfoRequest userInfoRequest,
                                     @SessionAttribute(value = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                                     Model model) {


        List<ExistingLoanResponse> allExistingLoans = loanOriginationService.getIntegratedLoanList(loginMember, userInfoRequest);

        InterestRateInfoResponse interestRateInfoResponse = loanOriginationService.calculateInterestRate(type, userInfoRequest, allExistingLoans);
        BigDecimal currentDsrResponse = briefDsrCalculator.calculateDsr(userInfoRequest, allExistingLoans);
        LoanProductInfoResponse loanProductInfoResponse = creditLoanProductService.getLoanProductInfo(type);


        // todo : 새로 고침할 때마다 호출 , 멱등성 처리 필요할 수도 있음
        loanOriginationService.startOrigination(loginMember,type , userInfoRequest ,allExistingLoans);

        model.addAttribute("loanProductInfoResponse", loanProductInfoResponse);
        model.addAttribute("interestRateInfoResponse", interestRateInfoResponse);
        model.addAttribute("currentDsrResponse", currentDsrResponse);


        return "loan/credit/customer-product-detail";
    }





}
