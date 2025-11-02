package bankapp.loan.web.controller.home;

import bankapp.loan.model.common.rate.InterestRateType;
import bankapp.loan.model.common.repayment.RepaymentMethod;
import bankapp.loan.service.common.rate.InterestRateTypeService;
import bankapp.loan.service.common.repayment.RepaymentMethodService;
import bankapp.loan.web.request.InterestRateTypeRequest;
import bankapp.loan.web.request.RepaymentMethodRequest;
import bankapp.loan.web.response.InterestRateTypeInfoResponse;
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

    public TempAdminController(RepaymentMethodService repaymentMethodService,
                               InterestRateTypeService interestRateTypeService) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
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

        RepaymentMethod newMethod = requestDto.toEntity();
        repaymentMethodService.saveRepayment(newMethod);
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

        InterestRateType newType = requestDto.toEntity();

        interestRateTypeService.saveInterestRateType(newType);

        // 3. 목록 페이지로 리다이렉트
        return "redirect:/temp-admin/interest-types";
    }




}
