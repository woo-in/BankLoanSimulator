package bankapp.loan.product.web.controller;


import bankapp.loan.product.model.CreditLoanProduct;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/loan")
public class ProductCustomerController {

    private final CreditLoanProductService creditLoanProductService;

    public ProductCustomerController(CreditLoanProductService creditLoanProductService) {
        this.creditLoanProductService = creditLoanProductService;
    }

    @RequestMapping("/home")
    public String showHome(){
        return "loan/loan-home";
    }

    @RequestMapping("/credit")
    public String showCreditList(Model model) {
        prepareCreditLoanListModel(model);
        return "loan/credit/list";
    }

    @RequestMapping("/credit/{type}")
    public String showCreditDetail(@PathVariable("type") String type, Model model){
        LoanProductInfoResponse response = getLoanProductResponse(type);
        model.addAttribute("LoanProductInfoResponse", response);
        return "loan/credit/product-detail";
    }



    private void prepareCreditLoanListModel(Model model){

        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();

        for(CreditLoanProduct creditLoanProduct : creditLoanProductService.findAllCreditLoanProducts()){
            loanProductInfoResponses.add(LoanProductInfoResponse.from(creditLoanProduct));
        }

        model.addAttribute("LoanProductInfoResponses" , loanProductInfoResponses);
    }
    private LoanProductInfoResponse getLoanProductResponse(String slug) {
        CreditLoanProduct product = creditLoanProductService.findCreditLoanProductByLoanProductSlug(slug);
        return LoanProductInfoResponse.from(product);
    }
}
