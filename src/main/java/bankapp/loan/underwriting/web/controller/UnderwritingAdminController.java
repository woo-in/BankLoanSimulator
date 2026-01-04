package bankapp.loan.underwriting.web.controller;

import bankapp.loan.origination.component.DsrCalculator;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.underwriting.service.LoanApplicationService;
import bankapp.loan.underwriting.web.request.ApprovedLoanApplicationDto;
import bankapp.loan.underwriting.web.request.RejectedLoanApplicationDto;
import bankapp.loan.underwriting.web.response.AppliedLoanApplicationResponse;
import bankapp.loan.underwriting.web.response.BriefAppliedLoanApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/loan")
public class UnderwritingAdminController {

    private final LoanApplicationService loanApplicationService;
    private final DsrCalculator dsrCalculator;


    public UnderwritingAdminController(LoanApplicationService loanApplicationService,
                                       DsrCalculator dsrCalculator) {
        this.loanApplicationService = loanApplicationService;
        this.dsrCalculator = dsrCalculator;
    }


    // todo : 일단은 검토할 때 , 연체이력은 배제 -> 나중에 꼭 고려
    // todo : 고도화 하면 검색 도입
    @GetMapping("/loan-applications")
    public String showLoanApplications(Model model) {
        List<BriefAppliedLoanApplicationResponse> briefAppliedLoanApplicationResponses = new ArrayList<>();
        List<LoanApplication> loanApplications = loanApplicationService.getAppliedApplications();

        for(LoanApplication loanApplication : loanApplications){
            briefAppliedLoanApplicationResponses.add(BriefAppliedLoanApplicationResponse.from(loanApplication));
        }

        model.addAttribute("briefAppliedLoanApplicationResponses", briefAppliedLoanApplicationResponses);
        return "loan/admin/loan-application-list";
    }

    @GetMapping("/loan-applications/{id}")
    public String showLoanApplication(@PathVariable Long id, Model model) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);
        AppliedLoanApplicationResponse response = AppliedLoanApplicationResponse.from(application);

        log.info(response.toString());

        model.addAttribute("loanApplication", response);
        return "loan/admin/loan-application-detail";
    }

    /**
     * 대출 심사 승인 처리
     */
    @PostMapping("/loan-applications/{id}/approve/review")
    public String reviewApproval(@PathVariable Long id,
                                 @ModelAttribute ApprovedLoanApplicationDto request,
                                 Model model) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);

        // todo : 컨트롤러에서 최종 금리 계산은 옳지 않음
        BigDecimal totalRate = request.getApprovedBaseRate()
                .add(request.getApprovedProductSpread())
                .add(request.getApprovedCreditSpread())
                .add(request.getApprovedSelectionSpread());
        request.setApprovedFinalInterestRate(totalRate);

        BigDecimal calculatedDsr = dsrCalculator.calculate(application,request);
        request.setCalculatedDsr(calculatedDsr);

        model.addAttribute("loanApplication", application);
        model.addAttribute("request", request);

        return "loan/admin/loan-application-approve-review";
    }
    @PostMapping("/loan-applications/{id}/approve/complete")
    public String completeApproval(@PathVariable Long id,
                                   @ModelAttribute ApprovedLoanApplicationDto request){
        loanApplicationService.approveApplication(id , request);
        return "redirect:/admin/loan/loan-applications";
    }


    /**
     * 대출 심사 거절 처리
     */
    @PostMapping("/loan-applications/{id}/reject/review")
    public String rejectApplication(@PathVariable Long id,
                                    @ModelAttribute RejectedLoanApplicationDto request,
                                    Model model){
        LoanApplication application = loanApplicationService.getLoanApplicationById(id);
        model.addAttribute("loanApplication", application);
        model.addAttribute("request", request);

        return "loan/admin/loan-application-reject-review";
        //
    }
    @PostMapping("/loan-applications/{id}/reject/complete")
    public String completeReject(@PathVariable Long id,
                                   @ModelAttribute RejectedLoanApplicationDto request){
        loanApplicationService.rejectApplication(id, request);
        return "redirect:/admin/loan/loan-applications";
    }


}

