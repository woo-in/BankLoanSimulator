package bankapp.loan.product.web.controller;


import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.service.InterestRateTypeService;
import bankapp.loan.product.service.RepaymentMethodService;
import bankapp.loan.web.request.InterestRateTypeRequest;
import bankapp.loan.web.request.LoanProductRequest;
import bankapp.loan.web.request.RepaymentMethodRequest;
import bankapp.loan.web.response.InterestRateTypeInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.loan.web.response.RepaymentMethodInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/loan")
public class AdminController {

    private final RepaymentMethodService repaymentMethodService;
    private final InterestRateTypeService interestRateTypeService;

    public AdminController(RepaymentMethodService repaymentMethodService,
                           InterestRateTypeService interestRateTypeService) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
    }


    @GetMapping
    public String AdminHome() { return "loan/admin/admin-home" ;}

    @GetMapping("/repayment-methods")
    public String showRepaymentMethods(Model model){

        List<RepaymentMethodInfoResponse> responses = getRepaymentMethodResponses();

        model.addAttribute("repaymentMethodInfoResponses", responses);
        model.addAttribute("newRepaymentMethod", new RepaymentMethodRequest());

        return "loan/admin/repayment-methods";
    }
    @PostMapping("/repayment-methods")
    public String registerRepaymentMethod(@ModelAttribute("newRepaymentMethod") RepaymentMethodRequest requestDto) {
        repaymentMethodService.saveRepayment(requestDto);
        return "redirect:/admin/loan/repayment-methods";
    }
    @PostMapping("/setup/repayment-methods")
    public String setupRepaymentMethods(RedirectAttributes redirectAttributes){
        repaymentMethodService.saveDefaultRepayment();
        redirectAttributes.addFlashAttribute("message", "기본 상환 방식이 성공적으로 등록되었습니다!");
        return "redirect:/admin/loan";
    }

    @GetMapping("/interest-types")
    public String showInterestTypes(Model model) {

        List<InterestRateTypeInfoResponse> interestTypeInfoResponses = getInterestTypeResponses();

        model.addAttribute("interestTypeInfoResponses" , interestTypeInfoResponses);
        model.addAttribute("newInterestType" , new InterestRateTypeRequest());

        return "loan/admin/interest-types";
    }
    @PostMapping("/interest-types")
    public String registerInterestType(@ModelAttribute("newInterestType") InterestRateTypeRequest requestDto) {
        interestRateTypeService.saveInterestRateType(requestDto);
        return "redirect:/admin/loan/interest-types";
    }
    @PostMapping("/setup/interest-types")
    public String setupInterestType(RedirectAttributes redirectAttributes){
        interestRateTypeService.saveDefaultInterestRateType();
        redirectAttributes.addFlashAttribute("message", "기본 금리 종류가 성공적으로 등록되었습니다!");
        return "redirect:/admin/loan";
    }




//    // 대출 상품 추가 페이지
//    @GetMapping("/loan-products")
//    public String showLoanProducts(Model model) {
//
//        // 상환 방법 보여주기
//        // TODO : 중복 코드 메서드 화
//        List<RepaymentMethod> methods = repaymentMethodService.findAllMethods();
//        List<RepaymentMethodInfoResponse> repaymentMethodInfoResponses = new ArrayList<>();
//        for(RepaymentMethod method : methods){
//            if(method.getIsActive()) {
//                repaymentMethodInfoResponses.add(RepaymentMethodInfoResponse.from(method));
//            }
//        }
//        model.addAttribute("repaymentMethodInfoResponses" , repaymentMethodInfoResponses);
//
//
//        // 금리 종류 보여주기
//        // TODO : 중복 코드 메서드 화
//        List<InterestRateType> types = interestRateTypeService.findAllTypes(); // (findAllTypes() 가정)
//        List<InterestRateTypeInfoResponse> interestTypeInfoResponses = new ArrayList<>();
//        for(InterestRateType type : types){
//            if(type.getIsActive()) {
//                interestTypeInfoResponses.add(InterestRateTypeInfoResponse.from(type));
//            }
//        }
//        model.addAttribute("interestTypeInfoResponses" , interestTypeInfoResponses);
//
//
//        // 현재 등록 대출 상품 보여주기
//        List<LoanProduct> products = loanProductService.findAllTypes();
//        List<LoanProductInfoResponse> loanProductInfoResponses = new ArrayList<>();
//        for(LoanProduct product : products){
//            loanProductInfoResponses.add(LoanProductInfoResponse.from(product));
//        }
//        model.addAttribute("loanProductInfoResponses" , loanProductInfoResponses);
//        // TODO: 임시로 무조건 신용대출만 가능하게 함.
//        // TODO : 대출 단위 , 최소 최대 입력받을 때 검증해야함 예를들어 , 최저 한도가 500 최고 한도가 50 ?
//        model.addAttribute("newLoanProduct" , new LoanProductRequest());
//
//        return  "loan/temp-admin/loanProducts";
//    }
//
//    @PostMapping("/loan-products")
//    public String registerInterestType(@ModelAttribute("newLoanProduct") LoanProductRequest loanProductRequest) {
//        creditLoanProductService.saveCreditLoanProduct(loanProductRequest);
//        return "redirect:/temp-admin/loan-products";
//    }






    private List<RepaymentMethodInfoResponse> getRepaymentMethodResponses() {

        List<RepaymentMethod> methods = repaymentMethodService.findAllMethods();
        List<RepaymentMethodInfoResponse> responses = new ArrayList<>();

        for (RepaymentMethod method : methods) {
            responses.add(RepaymentMethodInfoResponse.from(method));
        }

        return responses;
    }
    private List<InterestRateTypeInfoResponse> getInterestTypeResponses() {

        List<InterestRateType> types = interestRateTypeService.findAllTypes();

        List<InterestRateTypeInfoResponse> interestTypeInfoResponses = new ArrayList<>();
        for(InterestRateType type : types){
            interestTypeInfoResponses.add(InterestRateTypeInfoResponse.from(type));
        }

        return interestTypeInfoResponses;
    }

}
