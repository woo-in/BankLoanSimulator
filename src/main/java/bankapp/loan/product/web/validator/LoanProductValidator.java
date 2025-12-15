package bankapp.loan.product.web.validator;

import bankapp.loan.product.service.CreditLoanProductService;
import bankapp.loan.web.request.LoanProductRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class LoanProductValidator implements Validator {

    private final CreditLoanProductService creditLoanProductService;


    @Autowired
    public LoanProductValidator(CreditLoanProductService creditLoanProductService) {
        this.creditLoanProductService = creditLoanProductService;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return LoanProductRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LoanProductRequest request = (LoanProductRequest) target;

        // 1. Slug 유효성 검사 (중복 체크)
        if (StringUtils.hasText(request.getLoanProductSlug())) {
            if (creditLoanProductService.existsBySlug(request.getLoanProductSlug())) {
                errors.rejectValue("loanProductSlug", "duplicate", "이미 사용 중인 Slug입니다.");
            }
        } else {
            errors.rejectValue("loanProductSlug", "required", "Slug는 필수 입력값입니다.");
        }

        // 2. 대출 금액 유효성 검사
        validateAmount(request, errors);

        // 3. 대출 기간 유효성 검사
        validateTerm(request, errors);
    }


    // todo : 검증 필요 , 단위 잘못 해도 , 그대로 입력 됨
    private void validateAmount(LoanProductRequest request, Errors errors) {
        BigDecimal min = request.getMinLoanAmount();
        BigDecimal max = request.getMaxLoanAmount();
        BigDecimal unit = request.getApplicationAmountUnit();

        if (min == null || max == null || unit == null) {
            return; // @NotNull 어노테이션으로 1차 방어 권장
        }

        // 음수 체크
        if (min.compareTo(BigDecimal.ZERO) <= 0 || max.compareTo(BigDecimal.ZERO) <= 0 || unit.compareTo(BigDecimal.ZERO) <= 0) {
            errors.rejectValue("minLoanAmount", "positive", "모든 금액은 0보다 커야 합니다.");
            return;
        }

        // 최소 > 최대 체크
        if (min.compareTo(max) > 0) {
            errors.rejectValue("minLoanAmount", "range", "최소 한도가 최대 한도보다 클 수 없습니다.");
        }

        // (선택) 단위 검사: (최대 - 최소)가 단위로 나누어 떨어지는지
         if (max.subtract(min).remainder(unit).compareTo(BigDecimal.ZERO) != 0) {
            errors.rejectValue("applicationAmountUnit", "unit", "한도 범위가 신청 단위와 맞지 않습니다.");
         }
    }
    private void validateTerm(LoanProductRequest request, Errors errors) {
        Integer min = request.getMinLoanTerm();
        Integer max = request.getMaxLoanTerm();
        Integer unit = request.getApplicationTermUnit();

        if (min == null || max == null || unit == null) {
            return;
        }

        if (min <= 0 || max <= 0 || unit <= 0) {
            errors.rejectValue("minLoanTerm", "positive", "모든 기간은 0보다 커야 합니다.");
            return;
        }

        if (min > max) {
            errors.rejectValue("minLoanTerm", "range", "최소 기간이 최대 기간보다 클 수 없습니다.");
        }
    }

}
