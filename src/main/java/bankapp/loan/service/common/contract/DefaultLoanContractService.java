package bankapp.loan.service.common.contract;

import bankapp.account.model.account.LoanAccount;
import bankapp.account.request.open.OpenLoanAccountRequest;
import bankapp.account.service.open.loan.OpenLoanAccountService;
import bankapp.loan.model.common.application.LoanApplication;
import bankapp.loan.model.common.contract.ContractStatus;
import bankapp.loan.model.common.contract.LoanContract;
import bankapp.loan.repository.common.contract.LoanContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DefaultLoanContractService implements LoanContractService{

    private final LoanContractRepository loanContractRepository;
    private final OpenLoanAccountService openLoanAccountService;

    @Autowired
    public DefaultLoanContractService(LoanContractRepository loanContractRepository,
                                      OpenLoanAccountService openLoanAccountService) {
        this.loanContractRepository = loanContractRepository;
        this.openLoanAccountService = openLoanAccountService;
    }

    @Override
    @Transactional
    public LoanContract saveLoanContract(OpenLoanAccountRequest openLoanAccountRequest ,
                                         LoanApplication loanApplication){

        // 계약서 작성 , 반환 (동시에 계좌도 개설)
        LoanAccount loanAccount = openLoanAccountService.openLoanAccount(openLoanAccountRequest);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maturityDate = now.plusMonths(loanApplication.getLoanTerm());

        // 3. LoanApplication -> LoanContract 변환 (Builder 패턴 사용)
        LoanContract loanContract = LoanContract.builder()
                .loanApplication(loanApplication)                 // 신청서 연결
                .member(loanApplication.getMember())              // 회원 정보
                .loanProduct(loanApplication.getLoanProduct())    // 상품 정보

                // 금리 정보 매핑
                .appliedBaseRate(loanApplication.getAppliedBaseRate())          //
                .appliedProductSpread(loanApplication.getAppliedProductSpread())//
                .appliedCreditSpread(loanApplication.getAppliedCreditSpread())  //

                // 상환 및 금리 유형
                .repaymentMethod(loanApplication.getRepaymentMethod())      //
                .interestRateType(loanApplication.getInterestRateType())    //

                // 대출 조건
                .loanAmount(loanApplication.getLoanAmount())      //
                .loanTerm(loanApplication.getLoanTerm())          //

                // 계약 관리 정보 설정
                .contractDate(now)
                .maturityDate(maturityDate)
                .status(ContractStatus.ACTIVE)                    // 초기 상태 활성화
                .contractVersion(1)                               // 최초 계약 버전 1
                .build();


        loanContract.setLoanAccount(loanAccount);
        return loanContractRepository.save(loanContract);
    }






}
