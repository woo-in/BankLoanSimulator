package bankapp.loan.servicing.service.core;

import bankapp.loan.servicing.model.LoanAccount;
import bankapp.loan.exceptions.ActiveLoanContractNotFoundException;
import bankapp.loan.servicing.dto.RepaymentAllocationInfo;
import bankapp.loan.servicing.model.RepaymentTransaction;
import bankapp.loan.servicing.repository.RepaymentTransactionRepository;
import bankapp.loan.underwriting.model.ContractStatus;
import bankapp.loan.underwriting.model.LoanContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
public class DefaultRepaymentTransactionService implements RepaymentTransactionService {

    private final RepaymentTransactionRepository repaymentTransactionRepository;

    @Autowired
    public DefaultRepaymentTransactionService(RepaymentTransactionRepository repaymentTransactionRepository){
        this.repaymentTransactionRepository = repaymentTransactionRepository;
    }

    /**
     * 상환 트랜잭션 기록 (INSERT ONLY)
     * - 계산된 상환 배분 정보(DTO)를 받아 DB에 이력으로 저장
     *
     * @param loanAccount 대출 계좌
     * @param allocationInfo 상환 배분 정보 (계산 결과)
     */
    @Transactional
    public void recordRepaymentTransaction(LoanAccount loanAccount, RepaymentAllocationInfo allocationInfo) {


        LoanContract activeContract = getActiveContract(loanAccount);

        RepaymentTransaction transaction = RepaymentTransaction.builder()
                .loanAccount(loanAccount)
                .loanContract(activeContract)
                .transactionDate(allocationInfo.getTransactionDate())
                .totalRepaymentAmount(allocationInfo.getTotalRepaymentAmount())
                .principalAmount(allocationInfo.getPrincipalAmount())
                .interestAmount(allocationInfo.getInterestAmount())
                .delinquentAmount(allocationInfo.getDelinquentAmount())
                .accelerationPenaltyAmount(allocationInfo.getAccelerationPenaltyAmount())
                .loanBalanceAfterTransaction(allocationInfo.getLoanBalanceAfterTransaction())
                .build();

        transaction.setLoanAccount(loanAccount);
        transaction.setLoanContract(activeContract);
        repaymentTransactionRepository.save(transaction);
    }



    /**
     * 내부 헬퍼 메서드: 활성 상태의 대출 계약 조회
     * IllegalStateException 대신 커스텀 예외 사용
     */
    private LoanContract getActiveContract(LoanAccount loanAccount) {
        return loanAccount.getLoanContracts().stream()
                .filter(c -> c.getContractStatus() == ContractStatus.ACTIVE)
                // 최신 버전 우선 (혹시 모를 데이터 꼬임 방지)
                .max(Comparator.comparingInt(LoanContract::getContractVersion))
                .orElseThrow(() -> new ActiveLoanContractNotFoundException(
                        "활성 상태의 대출 계약(LoanContract)이 존재하지 않습니다. Account ID: " + loanAccount.getAccountId()));
    }
}
