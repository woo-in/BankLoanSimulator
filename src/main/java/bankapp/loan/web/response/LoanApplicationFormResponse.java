package bankapp.loan.web.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class LoanApplicationFormResponse {

    // 1. 상품 정보 (표시용)
    private String loanProductSlug;
    private String loanProductName;
    private String productDescription;

    // 2. 금리 정보 (표시용)
    private BigDecimal finalInterestRate; // (rateInfo 객체의 정보)

    // 3. 금액/기간 선택지 (컨트롤러에서 생성한 리스트)
    private List<BigDecimal> availableAmounts;
    private List<Integer> availableTerms;

    // 4. 상환/금리 옵션 선택지 (List<String>이 아닌 List<OptionDto>)
    private List<FormOptionDto> repaymentOptions;
    private List<FormOptionDto> interestRateTypeOptions;

    /**
     * <select> 폼의 <option>을 만들기 위한 내부 DTO
     */
    @Data
    @Builder
    public static class FormOptionDto {
        private Long id;       // <option th:value="..."></option>에 사용 (예: 3)
        private String name;   // <option th:text="..."></option>에 사용 (예: "원금균등상환")
    }
}
