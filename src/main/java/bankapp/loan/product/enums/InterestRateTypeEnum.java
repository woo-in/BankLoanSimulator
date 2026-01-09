package bankapp.loan.product.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterestRateTypeEnum {
    FIXED("고정금리"),
    VARIABLE("변동금리");

    private final String description;
}
