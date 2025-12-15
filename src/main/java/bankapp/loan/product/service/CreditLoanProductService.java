package bankapp.loan.product.service;

import bankapp.loan.exceptions.InvalidInterestRateTypeId;
import bankapp.loan.exceptions.InvalidLoanType;
import bankapp.loan.exceptions.InvalidRepaymentMethodId;
import bankapp.loan.exceptions.LoanProductNotFoundException;
import bankapp.loan.product.model.CreditLoanProduct;
import bankapp.loan.web.request.LoanProductRequest;

import java.math.BigDecimal;
import java.util.List;


/**
 * 신용 대출 상품 관리
 */
public interface CreditLoanProductService {


    /**
     * 모든 신용 대출 상품 목록을 조회합니다.
     * @return 전체 신용 대출 상품 리스트
     */
    List<CreditLoanProduct> findAllCreditLoanProducts();


    /**
     * 신규 신용 대출 상품을 등록합니다.
     * 전달받은 요청 정보를 바탕으로 상품 엔티티를 생성하여 저장하고,
     * 해당 상품에 연결된 상환 방식(Repayment Methods)과 금리 유형(Interest Rate Types) 옵션을
     * 일괄적으로 저장합니다. 모든 작업은 하나의 트랜잭션 내에서 처리됩니다.
     *
     * @param loanProductRequest 상품 기본 정보와 상환/금리 옵션 ID 목록이 포함된 DTO
     * @throws InvalidLoanType 대출 유형이 CREDIT 이 아닌 경우
     * @throws InvalidRepaymentMethodId 존재하지 않거나 유효하지 않은 상환 방식 ID가 포함된 경우
     * @throws InvalidInterestRateTypeId 존재하지 않거나 유효하지 않은 금리 유형 ID가 포함된 경우
     */
    void saveCreditLoanProduct(LoanProductRequest loanProductRequest) throws InvalidLoanType , InvalidRepaymentMethodId , InvalidInterestRateTypeId;


    /**
     * 상품 슬러그(Slug)를 기준으로 신용 대출 상품을 단건 조회합니다.
     *
     * @param loanProductSlug 조회할 대출 상품의 고유 슬러그 (URL 식별자)
     * @return 조회된 신용 대출 상품 엔티티
     * @throws LoanProductNotFoundException 해당 슬러그를 가진 대출 상품이 존재하지 않는 경우
     */
    CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug) throws LoanProductNotFoundException;



    /**
     * 상품 슬러그(Slug)를 기준으로 신용 대출 상품의 상품 가산 금리(Product Spread)를 조회합니다.
     * <p>
     * 엔티티 전체를 조회하지 않고, 금리 계산에 필요한 가산 금리 정보만 추출하여 반환합니다.
     *
     * @param loanProductSlug 조회할 대출 상품의 고유 슬러그 (URL 식별자)
     * @return 해당 상품에 설정된 기본 가산 금리 (Default Spread)
     * @throws LoanProductNotFoundException 해당 슬러그를 가진 대출 상품이 존재하지 않는 경우
     */
    BigDecimal findCreditLoanProductSpreadByLoanProductSlug(String loanProductSlug) throws LoanProductNotFoundException;


}

