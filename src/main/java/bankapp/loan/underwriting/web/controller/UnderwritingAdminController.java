package bankapp.loan.underwriting.web.controller;

import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.origination.service.LoanApplicationService;
import bankapp.loan.web.response.LoanApplicationCompleteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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



    // todo : 고도화 하면 검색 도입
    @GetMapping("/loan-applications")
    public String showLoanApplications(Model model) {

        List<LoanApplicationCompleteResponse> loanApplicationCompleteResponses = new ArrayList<>();
        List<LoanApplication> loanApplications = loanApplicationService.getAppliedApplications();

        for(LoanApplication loanApplication : loanApplications){
            loanApplicationCompleteResponses.add(LoanApplicationCompleteResponse.from(loanApplication));
        }

        model.addAttribute("loanApplicationCompleteResponses", loanApplicationCompleteResponses);
        return "loan/temp-admin/loanApplications";

    }

}
