package bankapp.loan.product.web.controller;


import bankapp.loan.product.model.CreditLoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.service.*;
import bankapp.loan.product.web.validator.LoanProductValidator;
import bankapp.loan.web.request.InterestRateTypeRequest;
import bankapp.loan.web.request.LoanProductRequest;
import bankapp.loan.web.request.RepaymentMethodRequest;
import bankapp.loan.web.response.InterestRateTypeInfoResponse;
import bankapp.loan.web.response.LoanProductInfoResponse;
import bankapp.loan.web.response.RepaymentMethodInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private final CreditLoanProductService creditLoanProductService;
    private final LoanProductValidator loanProductValidator;

    public AdminController(RepaymentMethodService repaymentMethodService,
                           InterestRateTypeService interestRateTypeService,
                           CreditLoanProductService creditLoanProductService,
                           LoanProductValidator loanProductValidator) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
        this.creditLoanProductService = creditLoanProductService;
        this.loanProductValidator = loanProductValidator;
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

    @GetMapping("/loan-products")
    public String showLoanProducts(Model model) {
        List<RepaymentMethodInfoResponse> repaymentMethods = getActiveRepaymentMethods();
        List<InterestRateTypeInfoResponse> interestRateTypes = getActiveInterestRateTypes();
        List<LoanProductInfoResponse> loanProducts = getAllLoanProductResponses();

        model.addAttribute("repaymentMethodInfoResponses", repaymentMethods);
        model.addAttribute("loanProductInfoResponses", loanProducts);
        model.addAttribute("interestTypeInfoResponses", interestRateTypes);
        model.addAttribute("newLoanProduct", new LoanProductRequest());

        return "loan/admin/loan-products";
    }
    @PostMapping("/loan-products")
    public String registerLoanProduct(
            @ModelAttribute("newLoanProduct") LoanProductRequest loanProductRequest,
            BindingResult bindingResult,
            Model model
    ) {

        loanProductValidator.validate(loanProductRequest, bindingResult);

        if (bindingResult.hasErrors()) {
            log.info("Validation errors: {}", bindingResult.getAllErrors());

            List<RepaymentMethodInfoResponse> repaymentMethods = getActiveRepaymentMethods();
            List<InterestRateTypeInfoResponse> interestRateTypes = getActiveInterestRateTypes();
            List<LoanProductInfoResponse> loanProducts = getAllLoanProductResponses();

            model.addAttribute("repaymentMethodInfoResponses", repaymentMethods);
            model.addAttribute("loanProductInfoResponses", loanProducts);
            model.addAttribute("interestTypeInfoResponses", interestRateTypes);

            return "loan/admin/loan-products";
        }

        creditLoanProductService.saveCreditLoanProduct(loanProductRequest);
        return "redirect:/admin/loan/loan-products";
    }







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

    private List<RepaymentMethodInfoResponse> getActiveRepaymentMethods() {
        List<RepaymentMethod> methods = repaymentMethodService.findAllMethods();
        List<RepaymentMethodInfoResponse> responses = new ArrayList<>();

        for (RepaymentMethod method : methods) {
            // Boolean 체크 (Null 안전하게 처리)
            if (Boolean.TRUE.equals(method.getIsActive())) {
                responses.add(RepaymentMethodInfoResponse.from(method));
            }
        }
        return responses;
    }
    private List<InterestRateTypeInfoResponse> getActiveInterestRateTypes() {
        List<InterestRateType> types = interestRateTypeService.findAllTypes();
        List<InterestRateTypeInfoResponse> responses = new ArrayList<>();

        for (InterestRateType type : types) {
            if (Boolean.TRUE.equals(type.getIsActive())) {
                responses.add(InterestRateTypeInfoResponse.from(type));
            }
        }
        return responses;
    }
    private List<LoanProductInfoResponse> getAllLoanProductResponses() {
        // 서비스 메서드 이름은 실제 구현에 맞춰 조정하세요 (예: findAllCreditLoanProducts)
        List<CreditLoanProduct> products = creditLoanProductService.findAllCreditLoanProducts();
        List<LoanProductInfoResponse> responses = new ArrayList<>();

        for (CreditLoanProduct product : products) {
            responses.add(LoanProductInfoResponse.from(product));
        }
        return responses;
    }
}
