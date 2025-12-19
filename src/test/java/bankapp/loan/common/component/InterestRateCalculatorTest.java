package bankapp.loan.common.component;

import bankapp.infra.client.bok.BokApiClient;
import bankapp.infra.client.bok.BokInterestRateDto;
import bankapp.loan.origination.component.LoanInquiryScorer;
import bankapp.loan.product.service.CreditLoanProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestRateCalculatorTest {

    @Mock
    private BokApiClient bokApiClient;

    // InterestRateCalculator 생성자에 필요한 다른 의존성 Mocking (테스트 실행을 위해 필요)
    @Mock
    private CreditLoanProductService creditLoanProductService;
    @Mock
    private LoanInquiryScorer loanInquiryScorer;

    @InjectMocks
    private InterestRateCalculator interestRateCalculator;

    @Test
    @DisplayName("정상 케이스: (최고-현재)가 2.0%일 때 -> 하한/상한 사이이므로 그대로 2.0% 반환")
    void calculateStressRate_Normal() {
        // given
        // 데이터: [3.0, 5.0, 3.0]
        // 최고(A): 5.0
        // 현재(B): 3.0 (리스트 마지막)
        // 차이: 2.0 -> (1.5 <= 2.0 <= 3.0) 범위 내
        List<BokInterestRateDto.SearchData> mockData = createMockData("3.0", "5.0", "3.0");

        when(bokApiClient.fetchMonthlyInterestRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockData));

        // when
        BigDecimal result = interestRateCalculator.calculateStressRate();

        // then
        // 가중치 적용 없이 원본 차이값 반환
        assertEquals(new BigDecimal("2.0"), result);
    }

    @Test
    @DisplayName("하한 적용: 차이가 0.5%일 때 -> 하한선인 1.5% 반환")
    void calculateStressRate_Floor() {
        // given
        // 데이터: [4.0, 4.5, 4.0]
        // 최고: 4.5
        // 현재: 4.0
        // 차이: 0.5 -> 1.5보다 작으므로 하한 적용
        List<BokInterestRateDto.SearchData> mockData = createMockData("4.0", "4.5", "4.0");

        when(bokApiClient.fetchMonthlyInterestRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockData));

        // when
        BigDecimal result = interestRateCalculator.calculateStressRate();

        // then
        assertEquals(new BigDecimal("1.5"), result);
    }

    @Test
    @DisplayName("상한 적용: 차이가 4.0%일 때 -> 상한선인 3.0% 반환")
    void calculateStressRate_Ceiling() {
        // given
        // 데이터: [3.0, 7.0, 3.0]
        // 최고: 7.0
        // 현재: 3.0
        // 차이: 4.0 -> 3.0보다 크므로 상한 적용
        List<BokInterestRateDto.SearchData> mockData = createMockData("3.0", "7.0", "3.0");

        when(bokApiClient.fetchMonthlyInterestRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(mockData));

        // when
        BigDecimal result = interestRateCalculator.calculateStressRate();

        // then
        assertEquals(new BigDecimal("3.0"), result);
    }

    @Test
    @DisplayName("예외 발생 시: 안전값 0.75 반환")
    void calculateStressRate_Exception() {
        // given
        when(bokApiClient.fetchMonthlyInterestRates(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("API Connection Error"));

        // when
        BigDecimal result = interestRateCalculator.calculateStressRate();

        // then
        // 코드에 하드코딩된 안전값 반환 확인
        assertEquals(new BigDecimal("0.75"), result);
    }

    @Test
    @DisplayName("데이터 없음(Empty/Null): 안전값 0.75 반환")
    void calculateStressRate_EmptyData() {
        // given
        when(bokApiClient.fetchMonthlyInterestRates(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(Collections.emptyList()));

        // when
        BigDecimal result = interestRateCalculator.calculateStressRate();

        // then
        assertEquals(new BigDecimal("0.75"), result);
    }

    // --- Helper Method ---
    private List<BokInterestRateDto.SearchData> createMockData(String... rates) {
        return Arrays.stream(rates)
                .map(rate -> {
                    BokInterestRateDto.SearchData data = new BokInterestRateDto.SearchData();
                    data.setDataValue(rate);
                    return data;
                })
                .toList();
    }
}