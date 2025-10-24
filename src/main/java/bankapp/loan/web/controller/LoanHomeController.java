package bankapp.loan.web.controller;

import bankapp.loan.common.enums.RepaymentMethod;
import bankapp.loan.model.product.CreditLoanProduct;
import bankapp.loan.model.product.LoanProductDetail;
import bankapp.loan.model.product.ProductStatus;
import bankapp.loan.response.LoanProductInfoResponse;
import bankapp.loan.service.CreditLoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/loan")
public class LoanHomeController {

    private final CreditLoanService creditLoanService;

    public LoanHomeController(CreditLoanService creditLoanService) { this.creditLoanService = creditLoanService; }

    @RequestMapping("/home")
    public String showHome(){
        return "loan/loan-home";
    }

    @RequestMapping("/credit")
    public String showCreditList(Model model)
    {
//        prepareLoanProduct();
        prepareCreditLoanListModel(model);
        return "loan/credit/list";
    }

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
    public String processLoanInquiry(@PathVariable("type") String type, Model model){
        // 입력을 바탕으로 신용등급 계산
        // 대출상품에 따라 신용등급에 따른 , 실제 가산이자 조회




    }







    private void prepareCreditLoanListModel(Model model){

        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();

        for(CreditLoanProduct creditLoanProduct : creditLoanService.findAllCreditLoanProducts()){
            loanProductInfoResponses.add(LoanProductInfoResponse.from(creditLoanProduct));
        }

        model.addAttribute("LoanProductInfoResponses" , loanProductInfoResponses);
    }

    // todo : 임시 함수 (신용대출상품저장)
    private void prepareLoanProduct(){

        LoanProductDetail loanProductDetail = new LoanProductDetail("온 국민이 즐기는 대출", "국민 누구나");

        CreditLoanProduct creditLoanProduct = CreditLoanProduct.builder()
                        .loanProductName("우인 기본 대출")
                        .loanProductSlug("default")
                        .maxInterestRate(new BigDecimal("15.113"))
                        .minInterestRate(new BigDecimal("12.331"))
                        .maxLoanAmount(new BigDecimal(30000000))
                        .maxLoanTerm(30)
                        .loanProductDetail(loanProductDetail)
                        .status(ProductStatus.ACTIVE)
                        .repaymentMethod(RepaymentMethod.BULLET_REPAYMENT)
                        .repaymentMethod(RepaymentMethod.OVERDRAFT)
                        .build();

            creditLoanService.saveCreditLoanProduct(creditLoanProduct);
    }


}

