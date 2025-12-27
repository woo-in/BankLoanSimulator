package bankapp.loan.web.controller;

import bankapp.loan.common.component.InterestRateCalculator;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.origination.service.LoanApplicationService;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.web.response.LoanApplicationCompleteResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller
@RequestMapping("/loan")
public class LoanHomeController {

    private final CreditLoanProductService creditLoanProductService;
    private final LoanApplicationService loanApplicationService;
    private final InterestRateCalculator interestRateCalculator;

    public LoanHomeController(CreditLoanProductService creditLoanProductService,
                              LoanApplicationService loanApplicationService,
                              InterestRateCalculator interestRateCalculator) {
        this.creditLoanProductService = creditLoanProductService;
        this.loanApplicationService = loanApplicationService;
        this.interestRateCalculator = interestRateCalculator;
    }







    // TODO : 시간 +1시간 문제 (2시에 신청했는데 3시로 신청)
    // TODO : 리다이렉트 -> 뒤로가기 문제
    @GetMapping("/complete")
    public String showLoanCompleteForm(Model model, HttpSession session){
            LoanApplication loanApplication = (LoanApplication) session.getAttribute("loanApplication");
            model.addAttribute("completedApplication" , LoanApplicationCompleteResponse.from(loanApplication));
            session.removeAttribute("loanApplication");
            return "loan/credit/apply-complete";
    }

    // todo : 고정금리인지 변동금리인지에 따라 아얘 path 를 달리해야 함. 


}

