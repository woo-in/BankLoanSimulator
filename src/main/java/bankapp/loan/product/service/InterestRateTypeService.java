package bankapp.loan.product.service;


import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.web.request.InterestRateTypeRequest;

import java.util.List;


/**
 * 금리 유형(Interest Rate Type) 관리
 */
public interface InterestRateTypeService {


    /**
     * 새로운 금리 유형을 시스템에 등록(저장)합니다.
     *
     * @param interestRateTypeRequest 저장할 금리 유형 엔티티
     */
    void saveInterestRateType(InterestRateTypeRequest interestRateTypeRequest) ;


    /**
     * 기본이 되는 금리 종류를 시스템에 저장
     */
    void saveDefaultInterestRateType();

    /**
     * 시스템에 등록된 모든 금리 유형 목록을 조회합니다.
     *
     * @return 전체 금리 유형 리스트
     */
    List<InterestRateType> findAllTypes();

    /**
     * 주어진 ID 목록에 해당하는 금리 유형들을 조회합니다.
     * 상품 등록 시 선택된 여러 금리 옵션들이 실제 존재하는지 확인하거나,
     * 해당 객체들을 가져와서 연관 관계를 맺을 때 주로 사용됩니다.
     *
     * @param ids 조회할 금리 유형의 ID 리스트
     * @return ID에 매칭되는 금리 유형 리스트 (존재하지 않는 ID는 제외됨)
     */
    List<InterestRateType> findAllById(List<Long> ids);
}
