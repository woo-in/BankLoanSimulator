package bankapp.loan.underwriting.web.controller;

import bankapp.account.model.account.Account;
import bankapp.account.service.check.AccountCheckService;
import bankapp.core.common.SessionConst;
import bankapp.loan.underwriting.model.ApplicationStatus;
import bankapp.loan.underwriting.model.LoanApplication;
import bankapp.loan.underwriting.service.LoanApplicationService;
import bankapp.loan.underwriting.web.customerdto.*;
import bankapp.loan.underwriting.web.response.BriefAppliedLoanApplicationResponse;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/management/loan")
public class UnderwritingCustomerController {


    // todo : id 유효성 검사 해야 함 (누구나 접근 가능)
    private final LoanApplicationService loanApplicationService;
    private final AccountCheckService accountCheckService;

    public UnderwritingCustomerController(LoanApplicationService loanApplicationService,
                                          AccountCheckService accountCheckService) {
        this.loanApplicationService = loanApplicationService;
        this.accountCheckService = accountCheckService;
    }

    @GetMapping("/loan-applications")
    public String showLoanApplications(Model model,
                                       @SessionAttribute(value = SessionConst.LOGIN_MEMBER, required = false) Member loginMember){
        List<BriefAppliedLoanApplicationResponse> briefAppliedLoanApplicationResponses = new ArrayList<>();
        List<LoanApplication> loanApplications = loanApplicationService.getLoanApplicationsByMemberId(loginMember.getMemberId());

        for(LoanApplication loanApplication : loanApplications){
            log.info("loan applications : {} ", loanApplication);
            briefAppliedLoanApplicationResponses.add(BriefAppliedLoanApplicationResponse.from(loanApplication));
        }

        log.info(briefAppliedLoanApplicationResponses.toString());

        model.addAttribute("briefAppliedLoanApplicationResponses", briefAppliedLoanApplicationResponses);
        return "loan/customer/loan-application-list";
    }

    @GetMapping("/loan-applications/{id}")
    public String showAppliedLoanApplicationDetail(@PathVariable Long id,
                                                   Model model) {
        LoanApplication application = loanApplicationService.getLoanApplicationById(id);

        if (application.getApplicationStatus() == ApplicationStatus.REJECTED) {
            // 거절
            RejectedCustomerApplicationResponse response = RejectedCustomerApplicationResponse.from(application);
            model.addAttribute("loanApplication", response);
            return "loan/customer/rejected-application-detail";
        }

        if (application.getApplicationStatus() == ApplicationStatus.APPROVED) {
            ApprovedCustomerApplicationResponse response = ApprovedCustomerApplicationResponse.from(application);
            model.addAttribute("loanApplication", response);
            return "loan/customer/approved-application-detail";
        }

        if(application.getApplicationStatus() == ApplicationStatus.APPLIED) {
            AppliedCustomerApplicationResponse response = AppliedCustomerApplicationResponse.from(application);
            model.addAttribute("loanApplication", response);
            return "loan/customer/applied-application-detail";
        }

        // default
        return "loan/customer/loan-application-list";
    }

    /**
     * 대출 신청 취소 (Withdraw)
     */
    @PostMapping("/loan-applications/{id}/withdraw")
    public String withdrawApplication(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);

        ApplicationStatus status = application.getApplicationStatus();
        if (status != ApplicationStatus.APPLIED && status != ApplicationStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 처리되었거나 취소할 수 없는 상태입니다.");
            return "redirect:/management/loan/loan-applications/" + id;
        }


        loanApplicationService.cancelApplication(id);

        redirectAttributes.addFlashAttribute("message", "대출 신청이 정상적으로 취소되었습니다.");
        return "redirect:/management/loan/loan-applications";
    }


    /**
     * 대출 계약 (Contract)
     */
    @GetMapping("/loan-applications/{id}/contract")
    public String showLoanContractForm(@PathVariable Long id,
                                       @SessionAttribute(value = SessionConst.LOGIN_MEMBER) Member loginMember,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);


        // 2. 상태 확인 (APPROVED 상태에서만 계약 가능)
        if (application.getApplicationStatus() != ApplicationStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "계약 가능한 상태가 아닙니다.");
            return "redirect:/management/loan/loan-applications/" + id;
        }

        // 3. 승인된 정보 DTO 전달
        ApprovedCustomerApplicationResponse response = ApprovedCustomerApplicationResponse.from(application);
        model.addAttribute("loanApplication", response);
        model.addAttribute("memberName", loginMember.getName()); // 계약자명 표시용

        return "loan/customer/loan-contract-agreement";
    }
    // 추가정보 입력 폼

    @GetMapping("/loan-applications/{id}/contract/execution-info")
    public String showExecutionInfoForm(@PathVariable Long id,
                                        @SessionAttribute(value = SessionConst.LOGIN_MEMBER) Member loginMember,
                                        Model model) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);
        List<Account> availableAccounts = accountCheckService.findDepositAccountsByMember(loginMember);

        // 3. 모델에 데이터 담기
        model.addAttribute("loanApplication", application); // 상품명 등 표시용
        model.addAttribute("availableAccounts", availableAccounts);
        model.addAttribute("executionRequest", new ExecutionInfoRequest()); // 폼 바인딩용 DTO

        return "loan/customer/loan-execution-info";
    }

    /**
     * [Step 2-Action] 정보 입력 완료 -> 비밀번호 입력(Sign)으로 이동
     * - 여기서 바로 저장하지 않고, Sign 페이지로 데이터를 넘기거나 세션에 임시 저장하는 것이 일반적이지만,
     * - 편의상 여기서는 데이터를 검증하고 바로 Sign 페이지로 이동시킵니다. (데이터는 Sign POST 때 한꺼번에 처리하거나, 여기서 1차 저장)
     */
    @PostMapping("/loan-applications/{id}/contract/execution-info")
    public String processExecutionInfo(@PathVariable Long id,
                                       @ModelAttribute ExecutionInfoRequest executionRequest) {


        loanApplicationService.updateApplicationExecutionInfo(id, executionRequest); // 서비스 메서드 추가 필요

        return "redirect:/management/loan/loan-applications/" + id + "/contract/sign";
    }


    @GetMapping("/loan-applications/{id}/contract/sign")
    public String showContractSignForm(@PathVariable Long id,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        LoanApplication application = loanApplicationService.getLoanApplicationById(id);

        if (application.getApplicationStatus() != ApplicationStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "계약 가능한 상태가 아닙니다.");
            return "redirect:/management/loan/loan-applications/" + id;
        }

        // [변경] ContractAuthRequest DTO 전달
        model.addAttribute("contractAuthRequest", new ContractAuthRequest());
        model.addAttribute("loanApplicationId", id);

        return "loan/customer/loan-contract-sign";
    }

    /**
     * [Step 2] 전자 서명 완료 및 계약 체결 (POST)
     */
    @PostMapping("/loan-applications/{id}/contract/sign")
    public String completeContractSign(@PathVariable Long id,
                                       @Validated @ModelAttribute ContractAuthRequest request,
                                       @SessionAttribute(value = SessionConst.LOGIN_MEMBER) Member loginMember,
                                       BindingResult bindingResult,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("loanApplicationId", id);
            return "loan/customer/loan-contract-sign";
        }

        try {
            loanApplicationService.signContract(id , loginMember , request);
            redirectAttributes.addFlashAttribute("message", "전자 약정이 성공적으로 체결되었습니다. 대출금이 곧 입금됩니다.");
            return "redirect:/management/loan/loan-applications";

        } catch (IncorrectPasswordException e) {
            bindingResult.rejectValue("password", "invalid", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("loanApplicationId", id);
            return "loan/customer/loan-contract-sign";
        } catch (Exception e) {
            log.error("계약 체결 중 오류 발생", e);
            model.addAttribute("errorMessage", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "loan/customer/loan-contract-sign";
        }
    }


















}
