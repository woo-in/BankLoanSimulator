package bankapp.loan.web.controller.home;

import bankapp.loan.common.enums.FinancialGrade;
import bankapp.loan.model.credit.CreditLoanProduct;
import bankapp.loan.service.component.LoanInquiryScorer;
import bankapp.loan.service.credit.CreditLoanService;
import bankapp.loan.web.request.CreditCheckRequest;
import bankapp.loan.web.response.InterestRateInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/loan")
public class LoanHomeController {

    private final CreditLoanService creditLoanService;
    private final LoanInquiryScorer loanInquiryScorer;

    public LoanHomeController(CreditLoanService creditLoanService , LoanInquiryScorer loanInquiryScorer) {
        this.creditLoanService = creditLoanService;
        this.loanInquiryScorer = loanInquiryScorer;
    }

    @RequestMapping("/home")
    public String showHome(){
        return "loan/loan-home";
    }

    @RequestMapping("/credit")
    public String showCreditList(Model model)
    {
        prepareCreditLoanListModel(model);
        return "loan/credit/list";
    }

    // 대출 상품 대략 표현
    @RequestMapping("/credit/{type}")
    public String showCreditDetail(@PathVariable("type") String type, Model model){
        CreditLoanProduct creditLoanProduct = creditLoanService.findCreditLoanProductByLoanProductSlug(type);
        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);
        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);
        return "loan/credit/product-detail";
    }

    @RequestMapping("/credit/{type}/inquiry")
    public String showLoanInquiryForm(@PathVariable("type") String type, Model model){

        CreditLoanProduct creditLoanProduct = creditLoanService.findCreditLoanProductByLoanProductSlug(type);
        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);
        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);

        return "loan/credit/user-input";
    }

    @PostMapping("/credit/{type}/calculate")
    public String processLoanInquiry(@PathVariable("type") String type,
                                   @Valid @ModelAttribute CreditCheckRequest creditCheckRequest,
                                   Model model) {
        // 입력을 바탕으로 신용등급 계산 , 고객 유저 금리 정보 계산
        FinancialGrade financialGrade = loanInquiryScorer.getFinancialGrade(creditCheckRequest);
        InterestRateInfoResponse interestRateInfoResponse= creditLoanService.calculateInterestRate(financialGrade, type);

        // 상품 정보
        CreditLoanProduct creditLoanProduct = creditLoanService.findCreditLoanProductByLoanProductSlug(type);
        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);

        // 추가
        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);
        model.addAttribute("InterestRateInfoResponse" , interestRateInfoResponse);

        return "loan/credit/customer-product-detail";

    }












    private void prepareCreditLoanListModel(Model model){



        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();

        for(CreditLoanProduct creditLoanProduct : creditLoanService.findAllCreditLoanProducts()){
            loanProductInfoResponses.add(LoanProductInfoResponse.from(creditLoanProduct));
        }

        model.addAttribute("LoanProductInfoResponses" , loanProductInfoResponses);
    }




}

