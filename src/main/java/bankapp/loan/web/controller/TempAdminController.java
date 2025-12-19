package bankapp.loan.web.controller;

import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.service.account.AccountService;
import bankapp.account.service.check.AccountCheckService;
import bankapp.loan.origination.model.LoanApplication;
import bankapp.loan.origination.model.LoanContract;
import bankapp.loan.product.model.LoanProduct;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.origination.service.LoanApplicationService;
import bankapp.loan.servicing.service.RepaymentBatchService;
import bankapp.loan.origination.service.LoanContractService;
import bankapp.loan.product.service.LoanProductService;
import bankapp.loan.product.service.InterestRateTypeService;
import bankapp.loan.product.service.RepaymentMethodService;
import bankapp.loan.servicing.service.RepaymentScheduleService;
import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.product.web.request.InterestRateTypeRequest;
import bankapp.loan.product.web.request.LoanProductRequest;
import bankapp.loan.product.web.request.RepaymentMethodRequest;
import bankapp.loan.product.web.response.InterestRateTypeInfoResponse;
import bankapp.loan.web.response.LoanApplicationCompleteResponse;
import bankapp.loan.product.web.response.LoanProductInfoResponse;
import bankapp.loan.product.web.response.RepaymentMethodInfoResponse;
import bankapp.member.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/temp-admin")
public class TempAdminController {

    private final RepaymentMethodService repaymentMethodService;
    private final InterestRateTypeService interestRateTypeService;
    private final LoanProductService loanProductService;
    private final CreditLoanProductService creditLoanProductService;
    private final LoanApplicationService loanApplicationService;
    private final LoanContractService loanContractService;
    private final AccountCheckService accountCheckService;
    private final AccountService accountService;
    private final RepaymentScheduleService repaymentScheduleService;

    // todo : 상환 로직 테스트를 위한 임시 , 테스트 끝나고 지울 것
    private final RepaymentBatchService repaymentBatchService;

    public TempAdminController(RepaymentMethodService repaymentMethodService,
                               InterestRateTypeService interestRateTypeService,
                               LoanProductService loanProductService,
                               CreditLoanProductService creditLoanProductService,
                               LoanApplicationService loanApplicationService,
                               LoanContractService loanContractService,
                               AccountCheckService accountCheckService,
                               AccountService accountService,
                               RepaymentScheduleService repaymentScheduleService,
                               RepaymentBatchService repaymentBatchService) {
        this.repaymentMethodService = repaymentMethodService;
        this.interestRateTypeService = interestRateTypeService;
        this.loanProductService = loanProductService;
        this.creditLoanProductService = creditLoanProductService;
        this.loanApplicationService = loanApplicationService;
        this.loanContractService = loanContractService;
        this.accountCheckService = accountCheckService;
        this.accountService = accountService;
        this.repaymentScheduleService = repaymentScheduleService;
        this.repaymentBatchService = repaymentBatchService;
    }






    // 대출 신청 관리 페이지
    @GetMapping("loan-applications")
    public String showLoanApplications(Model model) {

        List<LoanApplicationCompleteResponse> loanApplicationCompleteResponses = new ArrayList<>();
        List<LoanApplication> loanApplications = loanApplicationService.getAppliedApplications();

        for(LoanApplication loanApplication : loanApplications){
            loanApplicationCompleteResponses.add(LoanApplicationCompleteResponse.from(loanApplication));
        }

        model.addAttribute("loanApplicationCompleteResponses", loanApplicationCompleteResponses);
        return "loan/temp-admin/loanApplications";

    }

    // 거절
    @PostMapping("/loan-applications/{id}/reject")
    public String rejectLoanApplication(@PathVariable("id") Long applicationId) {

        try {
            loanApplicationService.rejectApplication(applicationId);
        } catch (Exception e) {
            log.error("대출 거절 처리 중 오류 발생", e);
            // 필요 시 에러 페이지나 플래시 메시지 처리
        }

        return "redirect:/temp-admin/loan-applications";
    }

    // 승인 (프로젝트할때 가장 핵심적인 부분 , 실제로 설명 , 어필할 부분)
    @PostMapping("/loan-applications/{id}/approve")
    public String approveLoanApplication(@PathVariable("id") Long applicationId) {

        try{

            // todo : 일단은 분리해뒀는데 사실은 트랜잭션으로 묶여야 함 (승인-계약서작성,계좌개설-입금,스케줄러작성)

            // 1. 승인
            loanApplicationService.approveApplication(applicationId);

            // 2. 신청서 찾기
            LoanApplication loanApplication = loanApplicationService.findById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 대출 신청 정보를 찾을 수 없습니다. ID: " + applicationId));

            // 3. 대출 계좌 생성 및 계약서 작성
            Member loanMember = loanApplication.getMember();
            OpenLoanAccountRequest openLoanAccountRequest = new OpenLoanAccountRequest();
            openLoanAccountRequest.setMemberId(loanMember.getMemberId());
            openLoanAccountRequest.setBalance(loanApplication.getLoanAmount());
            openLoanAccountRequest.setNickname("대출계좌");
            // todo : 일단은 primary 계좌로 상환 계좌 등록
            openLoanAccountRequest.setRepaymentAccount(accountCheckService.findPrimaryAccountByMember(loanMember));
            LoanContract loanContract = loanContractService.saveLoanContract(openLoanAccountRequest ,loanApplication);

            // 4. 입금 진행 (일단은 보통계좌로 입금한다고 가정) 1 번 계좌가 코어계좌라 가정
            AccountTransactionRequest debitTransaction = new AccountTransactionRequest(Long.parseLong("1"),loanApplication.getLoanAmount(),"대출 출금");
            accountService.debit(debitTransaction);
            AccountTransactionRequest creditTransaction = new AccountTransactionRequest(accountCheckService.findPrimaryAccountByMember(loanApplication.getMember()).getAccountId() ,loanApplication.getLoanAmount() , "대출 입금");
            accountService.credit(creditTransaction);
            // 5. 스케줄러 작성
            repaymentScheduleService.saveRepaymentSchedule(loanContract);




        }catch (Exception e){
            log.error("대출 성공 처리 중 오류 발생", e);
        }

        return "redirect:/temp-admin/loan-applications";

    }


    // todo : 상환 로직 테스트를 위한 임시 컨트롤러 (실제 배포시 , 사용금지)
    // [추가] 상환 배치 강제 실행 테스트 페이지 보여주기
    @GetMapping("/test-repayment")
    public String showTestRepaymentForm(Model model) {
        // 폼에 기본값으로 오늘 날짜를 셋팅해서 보여주면 편리합니다.
        model.addAttribute("targetDate", LocalDate.now());
        return "loan/temp-admin/testRepayment";
    }

    // [추가] 상환 배치 강제 실행 요청 처리
    @PostMapping("/test-repayment")
    public String runTestRepayment(@RequestParam("targetDate") String targetDateString) {
        try {
            LocalDate targetDate = LocalDate.parse(targetDateString); // 문자열 -> 날짜 변환
            log.info("[Test] 관리자에 의해 상환 배치가 강제 실행됩니다. (설정일: {})", targetDate);

            // 서비스의 processRepayments 메서드 호출
            repaymentBatchService.processRepayments(targetDate);

        } catch (Exception e) {
            log.error("[Test] 상환 배치 테스트 중 오류 발생", e);
            // 필요 시 에러 처리를 추가할 수 있습니다.
        }

        // 실행 후 다시 폼으로 리다이렉트 (또는 결과 페이지)
        return "redirect:/temp-admin/test-repayment?success=true";
    }




    // todo : 승인
    // 1. 대출 신청 승인 (신청에서 상태 바꾸기)

    // 2. 대출 약정 체결 (LoanContract)
    // 3. 대출 실행 (대출 계좌 생성 , 입금 진행)
    // 4. 상환 스케줄 생성 고려사항 : (대출금리 , 금리종류 , 상환방법)

    // 추가적 고려 사항 : 중도 상환시 , 연체시 , 금리인하 요구시)
    // 그리고 더 ...
    // 휴일 처리 (Business Day Convention)
    // 막달 단수 조정 (Last Payment Adjustment)
    // 변동 금리 재산정 (Variable Rate Recalculation)
    // 초회차 기산일 처리 (Broken Period Interest)


}
