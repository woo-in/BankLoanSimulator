package bankapp.loan.web.controller.home;

import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.credit.CreditLoanProduct;
import bankapp.loan.model.common.product.LoanProductDetail;
import bankapp.loan.model.common.product.ProductStatus;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.service.common.rate.InterestRateTypeService;
import bankapp.loan.service.common.repayment.RepaymentMethodService;
//import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.loan.service.credit.CreditLoanService;
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

    // todo : 임시 테스트 용, 반드시 삭제할 것 (은행고객이 은행원 사이트 내부에 접속하는 격)
    private final RepaymentMethodService repaymentMethodService;
    private final InterestRateTypeService interestRateTypeService;



    public LoanHomeController(CreditLoanService creditLoanService ,
                              RepaymentMethodService repaymentMethodService,
                              InterestRateTypeService interestRateTypeService) {
        this.creditLoanService = creditLoanService;
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
    }

    @RequestMapping("/home")
    public String showHome(){
        prepareRepaymentMethod();
        return "loan/loan-home";
    }

//    @RequestMapping("/credit")
//    public String showCreditList(Model model)
//    {
////        prepareLoanProduct();
//        prepareCreditLoanListModel(model);
//        return "loan/credit/list";
//    }
//
//    @RequestMapping("/credit/{type}")
//    public String showCreditDetail(@PathVariable("type") String type, Model model){
//        CreditLoanProduct creditLoanProduct = creditLoanService.findCreditLoanProductByLoanProductSlug(type);
//        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);
//        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);
//        return "loan/credit/product-detail";
//    }
//
//
//    @RequestMapping("/credit/{type}/inquiry")
//    public String showLoanInquiryForm(@PathVariable("type") String type, Model model){
//        CreditLoanProduct creditLoanProduct = creditLoanService.findCreditLoanProductByLoanProductSlug(type);
//        LoanProductInfoResponse loanProductInfoResponse = LoanProductInfoResponse.from(creditLoanProduct);
//        model.addAttribute("LoanProductInfoResponse" , loanProductInfoResponse);
//
//        return "loan/credit/user-input";
//    }
//
//    @PostMapping("/credit/{type}/calculate")
//    public String processLoanInquiry(@PathVariable("type") String type, Model model){
//        // 입력을 바탕으로 신용등급 계산
//        // 대출상품에 따라 신용등급에 따른 , 실제 가산이자 조회
//
//
//
//
//    }
//
//
//
//
//
//
//
//    private void prepareCreditLoanListModel(Model model){
//
//        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();
//
//        for(CreditLoanProduct creditLoanProduct : creditLoanService.findAllCreditLoanProducts()){
//            loanProductInfoResponses.add(LoanProductInfoResponse.from(creditLoanProduct));
//        }
//
//        model.addAttribute("LoanProductInfoResponses" , loanProductInfoResponses);
//    }
//
//    // todo : 임시 함수 (신용대출상품저장)
//    private void prepareLoanProduct(){
//
//
//        LoanProductDetail loanProductDetail = new LoanProductDetail("온 국민이 즐기는 대출", "국민 누구나");
//
//        CreditLoanProduct creditLoanProduct = CreditLoanProduct.builder()
//                        .loanProductName("우인 기본 대출")
//                        .loanProductSlug("default")
//                        .maxInterestRate(new BigDecimal("15.113"))
//                        .minInterestRate(new BigDecimal("12.331"))
//                        .maxLoanAmount(new BigDecimal(30000000))
//                        .maxLoanTerm(30)
//                        .loanProductDetail(loanProductDetail)
//                        .status(ProductStatus.ACTIVE)
//                        .build();
//
//            creditLoanService.saveCreditLoanProduct(creditLoanProduct);
//    }

    // todo : 임시 함수 (대출상환방법저장)
    private void prepareRepaymentMethod(){
        RepaymentMethod method1 = RepaymentMethod.builder()
                .methodCode("EQUAL_PRINCIPAL")
                .methodName("원금균등상환")
                .build();

        RepaymentMethod method2 = RepaymentMethod.builder()
                .methodCode("EQUAL_PAYMENT") // 원리금균등상환
                .methodName("원리금균등상환")
                .build();

        RepaymentMethod method3 = RepaymentMethod.builder()
                .methodCode("BULLET_PAYMENT") // 만기일시상환
                .methodName("만기일시상환")
                .build();

        repaymentMethodService.saveRepayment(method1);
        repaymentMethodService.saveRepayment(method2);
        repaymentMethodService.saveRepayment(method3);
    }



}

