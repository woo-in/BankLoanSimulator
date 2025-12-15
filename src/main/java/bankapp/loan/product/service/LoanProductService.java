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

}
