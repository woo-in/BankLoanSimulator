package bankapp.loan.product.service;


import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.web.request.RepaymentMethodRequest;
import java.util.List;


/**
 * 대출 상환 방식  관리
 */
public interface RepaymentMethodService {

    /**
     * 새로운 상환 방식을 시스템에 등록
     * 요청 DTO를 엔티티로 변환하여 저장
     *
     * @param repaymentMethodRequest 저장할 상환 방식 정보가 담긴 요청 DTO
     */
    void saveRepayment(RepaymentMethodRequest repaymentMethodRequest);

    /**
     * 기본이 되는 상환 방식을 시스템에 저장
     */
    void saveDefaultRepayment();



    /**
     * 시스템에 등록된 모든 상환 방식 목록을 조회
     * @return 전체 상환 방식 리스트
     */
    List<RepaymentMethod> findAllMethods();


    /**
     * 주어진 ID 목록에 해당하는 상환 방식들을 조회합니다.
     * @param ids 조회할 상환 방식의 ID 리스트
     * @return ID에 매칭되는 상환 방식 엔티티 리스트
     */
    List<RepaymentMethod> findAllById(List<Long> ids);


    /**
     * methodName 에 해당하는 상환 방식들을 조회합니다.
     * @param methodName 상환방식 이름
     * @return ID에 매칭되는 상환 방식 엔티티
     */
    RepaymentMethod findByMethodName(String methodName);

}
