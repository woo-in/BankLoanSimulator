package bankapp.loan.servicing.model;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.model.account.LoanStatus;
import bankapp.loan.underwriting.model.LoanContract;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanStatusHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_contract_id")
    private LoanContract loanContract;


    // 상태 변화 기록

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus;

    @Column(nullable = false , updatable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer durationDays;

    // 레코드 정보

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    /**
     * 대출 계좌 설정 및 양방향 관계 맺기
     */
    public void setLoanAccount(LoanAccount loanAccount) {
        // 1. 기존 관계 제거
        if (this.loanAccount != null) {
            this.loanAccount.getLoanStatusHistories().remove(this);
        }
        // 2. 새로운 관계 설정
        this.loanAccount = loanAccount;
        // 3. 반대쪽 리스트에 추가
        if (loanAccount != null && !loanAccount.getLoanStatusHistories().contains(this)) {
            loanAccount.getLoanStatusHistories().add(this);
        }
    }

    /**
     * 대출 계약 설정 및 양방향 관계 맺기
     */
    public void setLoanContract(LoanContract loanContract) {
        // 1. 기존 관계 제거
        if (this.loanContract != null) {
            this.loanContract.getLoanStatusHistories().remove(this);
        }
        // 2. 새로운 관계 설정
        this.loanContract = loanContract;
        // 3. 반대쪽 리스트에 추가
        if (loanContract != null && !loanContract.getLoanStatusHistories().contains(this)) {
            loanContract.getLoanStatusHistories().add(this);
        }
    }

}


