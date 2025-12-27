package bankapp.loan.product.service;

import bankapp.loan.product.model.LoanProduct;
import java.util.List;


/**
 * 전체 대출 상품 관리
 */
public interface LoanProductService {




    /**
     * 전체 대출 상품 목록을 조회합니다.
     * @return 전체 대출 상품 리스트
     */
    List<LoanProduct> findAllTypes();




    /**
     * slug 를 바탕으로 대출 상품 엔티티 반환
     * @param slug 식별 슬러그
     * @return 대출 상품 엔티티
     */
    public LoanProduct findByLoanProductSlug(String slug);



}
