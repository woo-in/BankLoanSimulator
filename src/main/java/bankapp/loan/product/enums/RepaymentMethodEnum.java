package bankapp.loan.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepaymentMethodEnum {

    BULLET("원금만기일시상환"),
    EQUAL_PRINCIPAL_INTEREST("원리금균등분할상환"),
    EQUAL_PRINCIPAL("원금균등분할상환");

    private final String description;

}
