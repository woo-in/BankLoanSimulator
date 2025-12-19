//package bankapp.infra.client.bok;
//
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//class BokApiClientTest {
//
//    private MockWebServer mockWebServer;
//    private BokApiClient bokApiClient;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        // 1. 가짜 서버 생성
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//
//        // 2. 가짜 서버의 URL로 WebClient를 사용하는 BokApiClient 생성
//        // (Spring @Autowired 없이 순수 단위 테스트로 구성하여 속도가 빠름)
//        String baseUrl = mockWebServer.url("/").toString();
//        WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(baseUrl);
//
//        bokApiClient = new BokApiClient(webClientBuilder);
//
//        // Private Field인 requestUrl, authKey 등을 Reflection이나 Setter로 주입할 수도 있지만,
//        // init() 메서드에서 baseUrl을 설정하는 구조이므로, 생성자 주입 시점에 baseUrl을 박아넣은 WebClient를 넘기는 방식으로 우회 테스트
//        // 또는 Spring의 ReflectionTestUtils를 사용하여 필드 주입
//        org.springframework.test.util.ReflectionTestUtils.setField(bokApiClient, "requestUrl", baseUrl);
//        org.springframework.test.util.ReflectionTestUtils.setField(bokApiClient, "authKey", "TEST_KEY");
//        org.springframework.test.util.ReflectionTestUtils.setField(bokApiClient, "webClient", webClientBuilder.build());
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//    @Test
//    @DisplayName("월별 금리 조회: JSON 응답이 DTO 리스트로 정확히 매핑되는지 검증")
//    void fetchMonthlyInterestRates_MappingTest() {
//        // given
//        // 사용자님이 제공하신 실제 JSON 응답 예시 (일부 데이터만 발췌)
//        String mockJsonResponse = """
//            {
//              "StatisticSearch": {
//                "list_total_count": 3,
//                "row": [
//                  {
//                    "STAT_CODE": "121Y006",
//                    "STAT_NAME": "1.3.3.2.1. 예금은행 대출금리(신규취급액 기준)",
//                    "ITEM_CODE1": "BECBLA03051",
//                    "ITEM_NAME1": "일반신용대출",
//                    "UNIT_NAME": "연리%",
//                    "TIME": "202001",
//                    "DATA_VALUE": "3.83"
//                  },
//                  {
//                    "STAT_CODE": "121Y006",
//                    "ITEM_CODE1": "BECBLA03051",
//                    "ITEM_NAME1": "일반신용대출",
//                    "TIME": "202002",
//                    "DATA_VALUE": "3.7"
//                  },
//                  {
//                    "STAT_CODE": "121Y006",
//                    "ITEM_CODE1": "BECBLA03051",
//                    "ITEM_NAME1": "일반신용대출",
//                    "TIME": "202412",
//                    "DATA_VALUE": "6.15"
//                  }
//                ]
//              }
//            }
//            """;
//
//        // 가짜 서버가 위 JSON을 리턴하도록 설정
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(mockJsonResponse)
//                .addHeader("Content-Type", "application/json"));
//
//        // when
//        Mono<List<BokInterestRateDto.SearchData>> resultMono =
//                bokApiClient.fetchMonthlyInterestRates("kr", "202001", "202412", "BECBLA03051");
//
//        // then
//        StepVerifier.create(resultMono)
//                .assertNext(dataList -> {
//                    // 1. 리스트 크기 검증
//                    assertEquals(3, dataList.size());
//
//                    // 2. 첫 번째 데이터 매핑 검증
//                    BokInterestRateDto.SearchData firstData = dataList.get(0);
//                    assertEquals("121Y006", firstData.getStatCode());
//                    assertEquals("BECBLA03051", firstData.getItemCode1());
//                    assertEquals("202001", firstData.getTime());
//                    assertEquals("3.83", firstData.getDataValue());
//
//                    // 3. 마지막 데이터 매핑 검증 (2024년 12월 데이터)
//                    BokInterestRateDto.SearchData lastData = dataList.get(dataList.size() - 1);
//                    assertEquals("202412", lastData.getTime());
//                    assertEquals("6.15", lastData.getDataValue());
//
//                    System.out.println(">>> DTO 매핑 테스트 성공! 데이터 개수: " + dataList.size());
//                })
//                .verifyComplete();
//    }
//}