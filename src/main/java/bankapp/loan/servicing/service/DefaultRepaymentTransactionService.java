package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.servicing.model.LoanRepaymentTransaction;
import bankapp.loan.servicing.repository.RepaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRepaymentTransactionService implements RepaymentTransactionService {

    private final RepaymentTransactionRepository repaymentTransactionRepository;

    /**
     * 💰 [NORMAL 상황] 전용 - 정상 상환 거래 내역을 생성 및 저장합니다. (가장 마지막 호출)
     * 연체 항목에 대한 충당은 모두 0으로 기록합니다.
     */
    @Transactional
    public LoanRepaymentTransaction createNormalTransaction(
            LoanAccount loanAccount,
            BigDecimal transactionAmount,
            BigDecimal paidScheduledInterestAmount,
            BigDecimal paidScheduledPrincipalAmount
    ) {
        // 빌더 패턴을 사용하여 객체 생성 (연체 항목은 모두 ZERO)
        LoanRepaymentTransaction transaction = LoanRepaymentTransaction.builder()
                .transactionAmount(transactionAmount)
                .transactionDate(LocalDateTime.now())

                // 연체 항목은 0으로 기록
                .paidPenaltyAmount(BigDecimal.ZERO)
                .paidOverdueInterestAmount(BigDecimal.ZERO)
                .paidOverduePrincipalAmount(BigDecimal.ZERO)

                // 정상 항목 기록
                .paidScheduledInterestAmount(paidScheduledInterestAmount)
                .paidScheduledPrincipalAmount(paidScheduledPrincipalAmount)

                // 거래 후 잔액 스냅샷
                .loanBalanceAfterTransaction(loanAccount.getBalance())
                .build();

        transaction.setLoanAccount(loanAccount);

        log.info("정상 상환 거래 기록 생성: ID {} for LoanAccount {}",
                transaction.getTransactionId(), loanAccount.getAccountId());

        return repaymentTransactionRepository.save(transaction);
    }

    /**
     * 🚨 [연체 상황] 전용 - 연체 포함 상환 거래 내역을 생성 및 저장합니다.
     * 충당 우선순위(Penalty, Overdue Interest/Principal)에 따라 기록합니다.
     */
    @Transactional
    public LoanRepaymentTransaction createDelinquentTransaction(
            LoanAccount loanAccount,
            BigDecimal transactionAmount,
            BigDecimal paidPenaltyAmount,
            BigDecimal paidOverdueInterestAmount,
            BigDecimal paidOverduePrincipalAmount,
            BigDecimal paidScheduledInterestAmount,
            BigDecimal paidScheduledPrincipalAmount
    ) {
        // 빌더 패턴을 사용하여 객체 생성
        LoanRepaymentTransaction transaction = LoanRepaymentTransaction.builder()
                .transactionAmount(transactionAmount)
                .transactionDate(LocalDateTime.now())

                // 연체 항목 기록
                .paidPenaltyAmount(paidPenaltyAmount)
                .paidOverdueInterestAmount(paidOverdueInterestAmount)
                .paidOverduePrincipalAmount(paidOverduePrincipalAmount)

                // 정상 항목 기록 (연체가 모두 해소된 후 남은 금액이 충당될 수 있음)
                .paidScheduledInterestAmount(paidScheduledInterestAmount)
                .paidScheduledPrincipalAmount(paidScheduledPrincipalAmount)

                // 거래 후 잔액 스냅샷
                .loanBalanceAfterTransaction(loanAccount.getBalance())
                .build();

        transaction.setLoanAccount(loanAccount);

        log.info("연체 포함 상환 거래 기록 생성: ID {} for LoanAccount {}",
                transaction.getTransactionId(), loanAccount.getAccountId());

        return repaymentTransactionRepository.save(transaction);
    }

}