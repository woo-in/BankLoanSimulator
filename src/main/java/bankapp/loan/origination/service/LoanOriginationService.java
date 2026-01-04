package bankapp.loan.origination.service;

import bankapp.loan.exceptions.InvalidLoanProduct;
import bankapp.loan.exceptions.InvalidPendingLoan;
import bankapp.loan.origination.model.PendingLoanApplicationStatus;
import bankapp.loan.origination.web.request.ApplicationAuthRequest;
import bankapp.loan.origination.web.request.ApplicationRequest;
import bankapp.loan.origination.web.request.FinancialInfoRequest;
import bankapp.loan.origination.web.response.ApplicationResponse;
import bankapp.loan.origination.web.response.ExistingLoanResponse;
import bankapp.loan.origination.web.response.InterestRateInfoResponse;
import bankapp.loan.underwriting.service.LoanContractService;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.exceptions.MemberNotFoundException;
import bankapp.member.model.Member;
import java.math.BigDecimal;
import java.util.List;

public interface LoanOriginationService {


    /**
     * 대출 가심사(Pending) 시작 (DRAFT 생성)
     * 필수 값을 초기화하고 DRAFT 상태로 저장
     * @param member      신청 회원
     * @param productSlug 대출 상품 Slug
     * @param userInfoRequest 유저 재산 정보 DTO
     * @param allExistingLoans 유저 총 대출 현황 리스트
     * @return 생성된 대출 진행 식별 키
     */
    Long startOrigination(Member member,
                                 String productSlug,
                                 FinancialInfoRequest userInfoRequest,
                                 List<ExistingLoanResponse> allExistingLoans) throws MemberNotFoundException , InvalidLoanProduct;


    /**
     * 사용자가 선택한 대출 조건(금액, 기간, 상환방식, 금리유형)을 현재 대출 진행에 반영
     *
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @param request                  사용자가 입력한 확정 대출 조건 정보
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     */
    void selectLoanTerms(Long pendingLoanApplicationId, ApplicationRequest request) throws InvalidPendingLoan;



    /**
     * 대출 가심사(Pending) 종료 , 신청서 작성
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @param applicationAuthRequest 인증 요청
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     * @throws IncorrectPasswordException 비밀번호 불일치
     */
    void completeOrigination(Long pendingLoanApplicationId , ApplicationAuthRequest applicationAuthRequest) throws InvalidPendingLoan , IncorrectPasswordException;


    /**
     * 현재 대출 진행 상태를 반환 합니다.
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     * @return 현재 대출 진행 상태
     */
    PendingLoanApplicationStatus getApplicationStatus(Long pendingLoanApplicationId)throws InvalidPendingLoan;


    /**
     * 회원의 내부 대출(DB 조회)과 외부 대출(사용자 입력 JSON)을 통합하여
     * 전체 대출 목록을 반환합니다.
     * <p>
     * DSR(총부채원리금상환비율) 계산 및 총 부채 산출을 위해 사용됩니다.
     * 외부 대출 데이터 파싱 중 오류가 발생할 경우, 로그를 남기고 내부 대출 목록만 반환하여
     * 전체 프로세스가 중단되지 않도록 처리합니다.
     * </p>
     * @param member  대출을 신청하는 회원 (내부 대출 조회 기준)
     * @param request 사용자가 입력한 재무 정보 요청 객체 (타행 대출 JSON 포함)
     * @return 내부 대출과 외부 대출이 합쳐진 통합 대출 목록 {@code List<ExistingLoanResponse>}
     * @see LoanContractService#findAllContractResponsesByMember(Member)
     */
    List<ExistingLoanResponse> getIntegratedLoanList(Member member, FinancialInfoRequest request);


    /**
     * 대출 목록 DTO 를 받아 , 총 부채를 반환
     * @param loans  대출 목록
     * @return 총 부채
     */
    BigDecimal calculateTotalDebt(List<ExistingLoanResponse> loans);


    /**
     * 임시 금리 정보를 계산하여 반환
     * @param productSlug  대출 상품 슬러그
     * @param userInfoRequest 유저 재산 정보 DTO
     * @param allExistingLoans 유저 총 대출 현황 리스트
     * @return 임시 금리 정보
     */
    InterestRateInfoResponse calculateInterestRate(String productSlug,
                                                          FinancialInfoRequest userInfoRequest,
                                                          List<ExistingLoanResponse> allExistingLoans);


    /**
     * 키를 바탕으로 임시 금리 정보를 계산하여 반환
     * @param productSlug  대출 상품 슬러그
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     * @return 임시 금리 정보
     */
    InterestRateInfoResponse calculateInterestRate(String productSlug,Long pendingLoanApplicationId) throws InvalidPendingLoan;


    /**
     * 키를 바탕으로 신청 정보 반환
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     * @return 신청 정보
     */
    ApplicationResponse getApplicationResponse(Long pendingLoanApplicationId) throws InvalidPendingLoan;


    /**
     * 키를 바탕으로 리스크 감면 금리 반환
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     * @return 리스크 감면 금리
     */
    BigDecimal getSelectionSpread(Long pendingLoanApplicationId) throws InvalidPendingLoan;



    /**
     * 키를 바탕으로 신청 당시 금리 정보 저장
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     */
    void setRate(Long pendingLoanApplicationId) throws InvalidPendingLoan;


    /**
     * 키를 바탕으로 신청 당시 최종 Dsr 정보 저장
     * @param pendingLoanApplicationId 대출 진행 식별 키
     * @throws InvalidPendingLoan 유효하지 않은 신청 ID일 경우 발생
     */
    void setDsr(Long pendingLoanApplicationId)  throws InvalidPendingLoan;



}
