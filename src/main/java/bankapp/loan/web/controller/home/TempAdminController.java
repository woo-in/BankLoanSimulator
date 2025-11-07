package bankapp.loan.web.controller.home;

import bankapp.loan.model.common.product.LoanProduct;
import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.service.common.product.LoanProductService;
import bankapp.loan.service.common.rate.InterestRateTypeService;
import bankapp.loan.service.common.repayment.RepaymentMethodService;
import bankapp.loan.service.credit.CreditLoanService;
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

    public TempAdminController(RepaymentMethodService repaymentMethodService,
                               InterestRateTypeService interestRateTypeService,
                               LoanProductService loanProductService,
                               CreditLoanService creditLoanService) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
        this.loanProductService = loanProductService;
        this.creditLoanService = creditLoanService;
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
        model.addAttribute("newLoanProduct" , new LoanProductRequest());

        return  "loan/temp-admin/loanProducts";
    }

    @PostMapping("/loan-products")
    public String registerInterestType(@ModelAttribute("newLoanProduct") LoanProductRequest loanProductRequest) {
        creditLoanService.saveCreditLoanProduct(loanProductRequest);
        return "redirect:/temp-admin/loan-products";
    }


}
