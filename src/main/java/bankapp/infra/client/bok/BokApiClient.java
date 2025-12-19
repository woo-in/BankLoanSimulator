package bankapp.infra.client.bok;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class BokApiClient {

    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    @Value("${external.api.koreaBank.auth-key}")
    private String authKey;

    @Value("${external.api.koreaBank.requestUrl}")
    private String requestUrl;


    @Autowired
    public BokApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        // 이 시점에는 requestUrl 필드에 값이 정상적으로 주입되어 있습니다.
        this.webClient = webClientBuilder.baseUrl(requestUrl).build();
    }




    /**
     * 한국은행 경제통계시스템 (817Y002 : 1.3.2.1 시장금리(일별)) 를 조회합니다.
     * @param lang 언어 (kr/en)
     * @param searchDate 통계 서칭 날짜
     * @param detailCode 세부 코드
     * 예시
     * CD(91일): 010502000
     * KORIBOR(3개월): 010150000
     * KORIBOR(6개월): 010151000
     * KORIBOR(12개월): 010152000
     * 국고채(3년): 010200000
     * 회사채(3년, AA-): 010300000
     * @return API 응답을 Mono<BokInterestRateDto.SearchData> 형태로 반환
     */
    public Mono<BokInterestRateDto.SearchData> fetchDayInterestRate(String lang,
                                                                    LocalDate searchDate,
                                                                    String detailCode) {

        final String serviceName = "StatisticSearch";
        final String format = "json";
        final int reqStartCount = 1;
        final int reqEndCount = 1;
        final String code = "817Y002";
        final String period = "D";
        String formattedStartDate = searchDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String formattedEndDate = searchDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));


        return this.webClient.get()
                .uri("/{serviceName}/{authKey}/{format}/{lang}/{reqStartCount}/{reqEndCount}/{code}/{period}/{formattedStartDate}/{formattedEndDate}/{detailCode}",
                        // 2. 위 placeholder에 순서대로 값을 채워 넣습니다.
                        serviceName, authKey, format, lang, reqStartCount, reqEndCount, code , period , formattedStartDate , formattedEndDate,detailCode)
                .retrieve()
                .bodyToMono(BokInterestRateDto.class)
                .flatMap(response -> { // 2. flatMap으로 내부 데이터 안전하게 추출
                    return Optional.ofNullable(response.getStatisticSearch())
                            .map(BokInterestRateDto.StatisticSearch::getRow)
                            .filter(row -> !row.isEmpty())
                            .map(row -> Mono.just(row.get(0))) // 3. row 리스트의 첫 번째 데이터를 Mono로 감싸서 반환
                            .orElse(Mono.empty()); // 4. 데이터가 없으면 빈 Mono 반환
                });

    }


    /**
     * 한국은행 경제통계시스템 (121Y006 : 1.3.2.1 예금은행 대출금리(신규취급액 기준)) 월별 데이터를 조회합니다.
     * 스트레스 DSR 산출을 위해 과거 5년치 데이터를 가져올 때 사용합니다.
     *
     * @param lang 언어 (kr/en)
     * @param startMonth 조회 시작 월 (yyyyMM)
     * @param endMonth   조회 종료 월 (yyyyMM)
     * @param detailCode 세부 코드 (예: BECBLA03051 - 가계일반신용대출)
     * @return 금리 데이터 리스트 (Mono<List<BokInterestRateDto.SearchData>>)
     */
    public Mono<List<BokInterestRateDto.SearchData>> fetchMonthlyInterestRates(String lang,
                                                                               String startMonth,
                                                                               String endMonth,
                                                                               String detailCode) {

        final String serviceName = "StatisticSearch";
        final String format = "json";
        final int reqStartCount = 1;
        final int reqEndCount = 100;   // 5년치(60개) 데이터를 가져와야 하므로 넉넉하게 설정
        final String code = "121Y006"; // 통계표: 예금은행 대출금리(신규취급액 기준)
        final String period = "M";     // 주기: 월별

        return this.webClient.get()
                .uri("/{serviceName}/{authKey}/{format}/{lang}/{reqStartCount}/{reqEndCount}/{code}/{period}/{startMonth}/{endMonth}/{detailCode}",
                        serviceName, authKey, format, lang, reqStartCount, reqEndCount, code, period, startMonth, endMonth, detailCode)
                .retrieve()
                .bodyToMono(BokInterestRateDto.class)
                .flatMap(response -> {
                    // 리스트 전체를 반환해야 하므로 row.get(0) 대신 row 전체를 반환합니다.
                    return Optional.ofNullable(response.getStatisticSearch())
                            .map(BokInterestRateDto.StatisticSearch::getRow)
                            .map(Mono::just)
                            .orElse(Mono.empty());
                });
    }


}
