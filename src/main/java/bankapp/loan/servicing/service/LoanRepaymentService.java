package bankapp.loan.servicing.service;

import bankapp.account.model.account.LoanAccount;
import bankapp.loan.underwriting.model.LoanContract;

public interface LoanRepaymentService {


    /**
     * 최초 대출 계약 체결 시, 전체 대출 기간에 대한 상환 스케줄을 생성하고 저장합니다.
     *
     * <p>
     * <b>상환 시작일 산정 기준:</b><br>
     * 대출 실행일이 속한 달의 다음 달은 거치 기간으로 간주하며,
     * 실질적인 첫 상환은 <b>대출 실행일 기준 다다음 달(익익월)</b>의 납입일(Payment Day)부터 시작됩니다.
     * </p>
     *
     * <p><b>예시:</b></p>
     * <ul>
     * <li>대출 실행일: 12월 31일</li>
     * <li>납입일(Payment Day): 매월 지정일</li>
     * <li>첫 상환 시작: 1월은 건너뛰고 <b>2월</b> 납입일부터 상환 시작</li>
     * </ul>
     *
     * @param loanAccount  생성된 대출 계좌 (대출 기간, 납입일 정보 포함)
     * @param loanContract 체결된 대출 계약 정보 (금리, 상환 방식, 스프레드 등 포함)
     */
    void saveRepaymentSchedule(LoanAccount loanAccount , LoanContract loanContract);

}
