package bankapp.infra.client.bok;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class BokInterestRateDto {

    @JsonProperty("StatisticSearch")
    private StatisticSearch statisticSearch;

    @Data
    @NoArgsConstructor
    public static class StatisticSearch {
        @JsonProperty("list_total_count")
        private int listTotalCount;

        @JsonProperty("row")
        private List<SearchData> row;
    }

    @Data
    @NoArgsConstructor
    public static class SearchData {
        // =========================================================
        // [Group 1] (fetchDayInterestRate)
        // =========================================================
        @JsonProperty("STAT_NAME")
        private String statisticName; // 통계명

        @JsonProperty("ITEM_NAME1")
        private String itemName;      // 항목명 (기존 코드에서 사용 중)

        @JsonProperty("UNIT_NAME")
        private String unitName;      // 단위

        @JsonProperty("TIME")
        private String time;          // 시점 (일별/월별 공통)

        @JsonProperty("DATA_VALUE")
        private String dataValue;     // 금리 값 (기존 코드에서 사용 중)

        // =========================================================
        // [Group 2] (fetchMonthlyInterestRates)
        // =========================================================
        @JsonProperty("STAT_CODE")
        private String statCode;      // 통계표 코드 (e.g. 121Y006)

        @JsonProperty("ITEM_CODE1")
        private String itemCode1;     // 항목 코드 1 (e.g. BECBLA03051)

        @JsonProperty("ITEM_CODE2")
        private String itemCode2;     // 항목 코드 2 (추가 확장 대비)

        @JsonProperty("ITEM_NAME2")
        private String itemName2;     // 항목명 2
    }
}