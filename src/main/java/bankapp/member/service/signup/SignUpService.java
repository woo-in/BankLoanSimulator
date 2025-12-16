package bankapp.member.service.signup;

import bankapp.member.exceptions.DuplicateUsernameException;
import bankapp.member.exceptions.PasswordMismatchException;
import bankapp.member.model.Member;
import bankapp.member.request.signup.SignUpRequest;

/**
 * 신규 회원 가입 비즈니스 로직의 명세를 정의합니다.
 */
public interface SignUpService {

    /**
     * 사용자가 입력한 정보를 바탕으로 회원 가입을 처리합니다.
     * 이 과정에서 아이디 중복 검사, 비밀번호 일치 여부 확인 유효성 검사를 수행합니다.
     *
     * @param signUpRequest 회원 가입에 필요한 사용자 정보를 담은 DTO
     * @return 성공적으로 생성된 회원 엔티티
     * @throws DuplicateUsernameException 입력된 사용자 아이디(username)가 이미 존재할 경우
     * @throws PasswordMismatchException 비밀번호와 비밀번호 확인 필드가 일치하지 않을 경우
     */
    Member signUp(SignUpRequest signUpRequest) throws DuplicateUsernameException, PasswordMismatchException;

    /**
     * [System] 은행 코어(자금) 관리용 멤버 생성
     * <p>
     * 초기 데이터 셋업 시 호출되며, 이미 존재할 경우 해당 멤버를 반환합니다.
     * 일반적인 유효성 검사를 거치지 않고 시스템 정책에 따라 생성됩니다.
     *
     * @return 생성되거나 조회된 코어 멤버 엔티티
     */
    Member createCoreBankMember();


}
