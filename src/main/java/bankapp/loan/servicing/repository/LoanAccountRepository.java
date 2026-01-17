package bankapp.loan.servicing.repository;

import bankapp.loan.servicing.model.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    // [PENDING, PLANNED, MERGED, OVERDUE, CRITICAL_OVERDUE, ACCELERATED]
    // X -> don't care
    // 0 -> 하나도 없어야 함
    // 1 -> 오직 하나 있어야 함

    /**
     * [NORMAL 전환 후보 검색]
     * <p>
     * 조건:
     * 1. 현재 상태가 <b>NORMAL이 아님</b>
     * 2. 스케줄 조건: [X, X, 0, 0, 0, 0]
     * - 비정상 스케줄(MERGED, OVERDUE, CRITICAL_OVERDUE, ACCELERATED)이 없어야 함
     * </p>
     */
    @Query("SELECT l FROM LoanAccount l " +
            "JOIN l.repaymentSchedules rs " +
            "WHERE l.loanStatus <> 'NORMAL' " +
            "GROUP BY l " +
            "HAVING SUM(CASE WHEN rs.status IN ('MERGED', 'OVERDUE', 'CRITICAL_OVERDUE', 'ACCELERATED') THEN 1 ELSE 0 END) = 0")
    List<LoanAccount> findCandidatesForNormalStatus();

    /**
     * [DELINQUENT 전환 후보 검색]
     * <p>
     * 조건:
     * 1. 현재 상태가 <b>DELINQUENT가 아님</b>
     * 2. 스케줄 조건: [X, X, 0, 1, 0, 0]
     * - OVERDUE = 1
     * - MERGED, CRITICAL_OVERDUE, ACCELERATED = 0
     * </p>
     */
    @Query("SELECT l FROM LoanAccount l " +
            "JOIN l.repaymentSchedules rs " +
            "WHERE l.loanStatus <> 'DELINQUENT' " +
            "GROUP BY l " +
            "HAVING SUM(CASE WHEN rs.status = 'OVERDUE' THEN 1 ELSE 0 END) = 1 " +
            "AND SUM(CASE WHEN rs.status IN ('MERGED', 'CRITICAL_OVERDUE', 'ACCELERATED') THEN 1 ELSE 0 END) = 0")
    List<LoanAccount> findCandidatesForDelinquentStatus();

    /**
     * [ACCELERATION_NOTICE 전환 후보 검색]
     * <p>
     * 조건:
     * 1. 현재 상태가 <b>ACCELERATION_NOTICE가 아님</b>
     * 2. 스케줄 조건: [X, X, 0, 1, 1, 0]
     * - CRITICAL_OVERDUE = 1
     * - OVERDUE = 1
     * - MERGED, ACCELERATED = 0
     * </p>
     */
    @Query("SELECT l FROM LoanAccount l " +
            "JOIN l.repaymentSchedules rs " +
            "WHERE l.loanStatus <> 'ACCELERATION_NOTICE' " +
            "GROUP BY l " +
            "HAVING SUM(CASE WHEN rs.status = 'CRITICAL_OVERDUE' THEN 1 ELSE 0 END) = 1 " +
            "AND SUM(CASE WHEN rs.status = 'OVERDUE' THEN 1 ELSE 0 END) = 1 " +
            "AND SUM(CASE WHEN rs.status IN ('MERGED', 'ACCELERATED') THEN 1 ELSE 0 END) = 0")
    List<LoanAccount> findCandidatesForAccelerationNoticeStatus();

    /**
     * [ACCELERATION 전환 후보 검색]
     * <p>
     * 조건:
     * 1. 현재 상태가 <b>ACCELERATION이 아님</b>
     * 2. 스케줄 조건: [0, 0, X, 0, 0, 1]
     * - ACCELERATED = 1
     * - PENDING, PLANNED, OVERDUE, CRITICAL_OVERDUE = 0
     * </p>
     */
    @Query("SELECT l FROM LoanAccount l " +
            "JOIN l.repaymentSchedules rs " +
            "WHERE l.loanStatus <> 'ACCELERATION' " +
            "GROUP BY l " +
            "HAVING SUM(CASE WHEN rs.status = 'ACCELERATED' THEN 1 ELSE 0 END) = 1 " +
            "AND SUM(CASE WHEN rs.status IN ('PENDING', 'PLANNED', 'OVERDUE', 'CRITICAL_OVERDUE') THEN 1 ELSE 0 END) = 0")
    List<LoanAccount> findCandidatesForAccelerationStatus();

    /**
     * [TERMINATED 전환 후보 검색]
     * <p>
     * 조건:
     * 1. 현재 상태가 <b>TERMINATED가 아님</b>
     * 2. 스케줄 조건: [0, 0, X, 0, 0, 0]
     * - 미결/진행중 스케줄(PENDING, PLANNED, OVERDUE, CRITICAL_OVERDUE, ACCELERATED)이 없어야 함
     * </p>
     */
    @Query("SELECT l FROM LoanAccount l " +
            "JOIN l.repaymentSchedules rs " +
            "WHERE l.loanStatus <> 'TERMINATED' " +
            "GROUP BY l " +
            "HAVING SUM(CASE WHEN rs.status IN ('PENDING', 'PLANNED', 'OVERDUE', 'CRITICAL_OVERDUE', 'ACCELERATED') THEN 1 ELSE 0 END) = 0")
    List<LoanAccount> findCandidatesForTerminatedStatus();

}