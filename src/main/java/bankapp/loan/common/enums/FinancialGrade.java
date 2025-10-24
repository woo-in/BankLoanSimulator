package bankapp.loan.common.enums;



public enum FinancialGrade {

    // 내부 재무 등급 (Financial Grade)

    /** * 최우수. 자산/소득이 부채/지출 대비 매우 안정적.
     */
    SECURE,

    /** * 우수. 안정적인 재무 상태.
     */
    STABLE,

    /** * 보통. 일반적인 수준.
     */
    STANDARD,

    /** * 주의. 부채 또는 고정지출 비중이 다소 높음.
     * (가산 금리가 높게 책정되거나 한도가 낮아질 수 있음)
     */
    CAUTION,

    /** * 위험. DSR(총부채원리금상환비율)이 높거나 소득 대비 부채가 과다.
     * (대출이 거절될 수 있음)
     */
    RISK


}
