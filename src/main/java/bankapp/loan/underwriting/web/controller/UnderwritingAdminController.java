package bankapp.loan.underwriting.web.controller;

import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.underwriting.service.LoanApplicationService;
import bankapp.loan.underwriting.web.response.AppliedLoanApplicationResponse;
import bankapp.loan.underwriting.web.response.BriefAppliedLoanApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/loan")
public class UnderwritingAdminController {

    private final LoanApplicationService loanApplicationService;


    public UnderwritingAdminController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
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


    // [추가 1] 상세 페이지 조회
    @GetMapping("/loan-applications/{id}")
    public String showLoanApplication(@PathVariable Long id, Model model) {
        // 서비스에서 엔티티 조회 (예외처리 필요)
        LoanApplication application = loanApplicationService.getLoanApplicationById(id);
        AppliedLoanApplicationResponse response = AppliedLoanApplicationResponse.from(application);

        model.addAttribute("loanApplication", response);
        return "loan/admin/loan-application-detail";
    }

    /**
     * 대출 심사 승인 처리
     * POST /admin/loan/loan-applications/{id}/approve
     */
    @PostMapping("/loan-applications/{id}/approve")
    public String approveApplication(@PathVariable Long id) {
        loanApplicationService.approveApplication(id);
        return "redirect:/admin/loan/loan-applications";
    }

    /**
     * 대출 심사 거절 처리
     * POST /admin/loan/loan-applications/{id}/reject
     * Param: reason (거절 사유)
     */
    @PostMapping("/loan-applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id, @RequestParam("reason") String reason) {
        loanApplicationService.rejectApplication(id, reason);
        return "redirect:/admin/loan/loan-applications";
    }

}

