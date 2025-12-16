package bankapp.loan.product.service;

import bankapp.loan.exceptions.*;
import bankapp.loan.product.model.*;
import bankapp.loan.product.repository.LoanProductInterestRateTypeOptionRepository;
import bankapp.loan.product.repository.LoanProductRepaymentOptionRepository;
import bankapp.loan.product.repository.CreditLoanProductRepository;
import bankapp.loan.product.web.request.LoanProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultCreditLoanProductService implements CreditLoanProductService {


    private final CreditLoanProductRepository creditLoanProductRepository;
    private final LoanProductInterestRateTypeOptionRepository loanProductInterestRateTypeOptionRepository;
    private final LoanProductRepaymentOptionRepository loanProductRepaymentOptionRepository;
    private final InterestRateTypeService interestRateTypeService;
    private final RepaymentMethodService repaymentMethodService;



    @Autowired
    public DefaultCreditLoanProductService(CreditLoanProductRepository creditLoanProductRepository,
                                           LoanProductInterestRateTypeOptionRepository loanProductInterestRateTypeOptionRepository,
                                           LoanProductRepaymentOptionRepository  loanProductRepaymentOptionRepository,
                                           InterestRateTypeService interestRateTypeService,
                                           RepaymentMethodService repaymentMethodService) {
        this.creditLoanProductRepository = creditLoanProductRepository;
        this.loanProductInterestRateTypeOptionRepository = loanProductInterestRateTypeOptionRepository;
        this.loanProductRepaymentOptionRepository = loanProductRepaymentOptionRepository;
        this.interestRateTypeService = interestRateTypeService;
        this.repaymentMethodService = repaymentMethodService;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CreditLoanProduct> findAllCreditLoanProducts() {
        return creditLoanProductRepository.findAll();
    }

    @Override
    @Transactional
    public void saveCreditLoanProduct(LoanProductRequest loanProductRequest){
        validateLoanType(loanProductRequest);
        CreditLoanProduct savedProduct = saveProductEntity(loanProductRequest);
        saveRepaymentOptions(savedProduct, loanProductRequest.getRepaymentMethodIds());
        saveInterestRateOptions(savedProduct, loanProductRequest.getInterestRateTypeIds());
    }

    @Override
    @Transactional
    public void saveDefaultCreditLoanProduct(){
        List<Long> allRateIds = getAllInterestRateTypeIds();
        List<Long> allMethodIds = getAllRepaymentMethodIds();
        List<LoanProductRequest> requests = createDefaultProductRequests(allRateIds, allMethodIds);
        saveProductsIfNotExists(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditLoanProduct findCreditLoanProductByLoanProductSlug(String loanProductSlug) {
        return creditLoanProductRepository.findByLoanProductSlug(loanProductSlug)
                .orElseThrow(() -> new LoanProductNotFoundException("해당 신용대출 상품을 찾을 수 없습니다 : " + loanProductSlug));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal findCreditLoanProductSpreadByLoanProductSlug(String loanProductSlug){
        CreditLoanProduct product = creditLoanProductRepository.findByLoanProductSlug(loanProductSlug)
                .orElseThrow(() -> new LoanProductNotFoundException("해당 신용대출 상품을 찾을 수 없습니다 : " + loanProductSlug));
        return product.getDefaultSpread();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug){
        return creditLoanProductRepository.findByLoanProductSlug(slug).isPresent();
    }







    private List<Long> getAllInterestRateTypeIds() {
        List<InterestRateType> allRates = interestRateTypeService.findAllTypes();
        List<Long> ids = new ArrayList<>();
        for (InterestRateType rate : allRates) {
            ids.add(rate.getInterestRateTypeId());
        }
        return ids;
    }
    private List<Long> getAllRepaymentMethodIds() {
        List<RepaymentMethod> allMethods = repaymentMethodService.findAllMethods();
        List<Long> ids = new ArrayList<>();
        for (RepaymentMethod method : allMethods) {
            ids.add(method.getRepaymentMethodId());
        }
        return ids;
    }
    private List<LoanProductRequest> createDefaultProductRequests(List<Long> rateIds, List<Long> methodIds) {

        List<LoanProductRequest> requests = new ArrayList<>();

        // --- 1. 직장인/일반인 ---
        requests.add(createRequest("우인 직장인 든든 신용대출", "office-worker-loan",
                "재직기간 3개월 이상 직장인을 위한 우인은행 대표 신용대출",
                new BigDecimal("300000000"), new BigDecimal("1000000"), new BigDecimal("2.50"), rateIds, methodIds));

        requests.add(createRequest("우인 급여이체 신용대출", "payroll-transfer-loan",
                "우인은행으로 급여이체 실적이 있는 고객을 위한 우대 금리 대출",
                new BigDecimal("150000000"), new BigDecimal("1000000"), new BigDecimal("2.30"), rateIds, methodIds));

        requests.add(createRequest("우인 새내기 직장인 대출", "new-employee-loan",
                "입사 3개월 미만의 사회초년생을 위한 응원 대출",
                new BigDecimal("50000000"), new BigDecimal("1000000"), new BigDecimal("3.50"), rateIds, methodIds));

        // --- 2. 전문직/공무원 ---
        requests.add(createRequest("우인 공무원 우대대출", "civil-servant-loan",
                "공무원, 교직원 등 안정적인 직군을 위한 저금리 대출",
                new BigDecimal("200000000"), new BigDecimal("1000000"), new BigDecimal("1.90"), rateIds, methodIds));

        requests.add(createRequest("우인 닥터론 (전문의)", "doctor-loan",
                "의사 및 전문의 자격증 소지자를 위한 초우량 신용대출",
                new BigDecimal("500000000"), new BigDecimal("10000000"), new BigDecimal("1.80"), rateIds, methodIds));

        requests.add(createRequest("우인 로이어론 (변호사)", "lawyer-loan",
                "변호사, 판사, 검사 등 법조인을 위한 고한도 대출",
                new BigDecimal("400000000"), new BigDecimal("5000000"), new BigDecimal("1.95"), rateIds, methodIds));

        requests.add(createRequest("우인 교직원 사랑대출", "teacher-loan",
                "사립/국공립 학교 교직원을 위한 전용 상품",
                new BigDecimal("150000000"), new BigDecimal("1000000"), new BigDecimal("2.10"), rateIds, methodIds));

        // --- 3. 사업자 ---
        requests.add(createRequest("우인 사장님 희망대출", "business-owner-hope-loan",
                "개인사업자를 위한 사업자금 지원 대출",
                new BigDecimal("100000000"), new BigDecimal("1000000"), new BigDecimal("4.20"), rateIds, methodIds));

        requests.add(createRequest("우인 프랜차이즈 가맹점 대출", "franchise-loan",
                "우수 프랜차이즈 가맹점주를 위한 운영자금 대출",
                new BigDecimal("200000000"), new BigDecimal("5000000"), new BigDecimal("3.80"), rateIds, methodIds));

        // --- 4. 소액/특수 ---
        requests.add(createRequest("우인 비상금 대출 (소액)", "emergency-loan",
                "서류 없이 모바일로 간편하게 빌리는 소액 대출 (최대 300만원)",
                new BigDecimal("3000000"), new BigDecimal("500000"), new BigDecimal("5.50"), rateIds, methodIds));

        requests.add(createRequest("우인 성실상환자 우대대출", "good-credit-loan",
                "우인은행 대출을 성실하게 상환한 이력이 있는 고객 전용",
                new BigDecimal("30000000"), new BigDecimal("1000000"), new BigDecimal("2.80"), rateIds, methodIds));

        // --- 5. 정책/중금리 ---
        requests.add(createRequest("우인 사잇돌 중금리 대출", "mid-range-loan",
                "신용점수가 다소 낮아도 가능한 정부지원 중금리 대출",
                new BigDecimal("20000000"), new BigDecimal("100000"), new BigDecimal("7.50"), rateIds, methodIds));

        requests.add(createRequest("우인 청년 도약 신용대출", "youth-jump-loan",
                "만 34세 이하 청년을 위한 생활안정자금",
                new BigDecimal("10000000"), new BigDecimal("100000"), new BigDecimal("4.50"), rateIds, methodIds));

        requests.add(createRequest("우인 군인 생활안정자금", "military-support-loan",
                "직업군인 및 군무원을 위한 생활안정 대출",
                new BigDecimal("50000000"), new BigDecimal("1000000"), new BigDecimal("3.20"), rateIds, methodIds));

        return requests;
    }
    private void saveProductsIfNotExists(List<LoanProductRequest> requests) {
        for (LoanProductRequest req : requests) {
            if (!existsBySlug(req.getLoanProductSlug())) {
                saveCreditLoanProduct(req);
            }
        }
    }
    private void validateLoanType(LoanProductRequest request) {
        if (!"CREDIT".equals(request.getLoanType())) {
            throw new InvalidLoanType("Invalid loan type: Expected 'CREDIT' but received '" + request.getLoanType() + "'.");
        }
    }
    private CreditLoanProduct saveProductEntity(LoanProductRequest request) {
        CreditLoanProduct product = (CreditLoanProduct) request.toEntity();
        return creditLoanProductRepository.save(product);
    }
    private void saveRepaymentOptions(CreditLoanProduct product, List<Long> methodIds) {
        List<RepaymentMethod> methods = repaymentMethodService.findAllById(methodIds);

        if (methods.size() != methodIds.size()) {
            throw new InvalidRepaymentMethodId("유효하지 않은 상환 방법 ID가 포함되어 있습니다.");
        }

        List<LoanProductRepaymentOption> options = methods.stream()
                .map(method -> {
                    LoanProductRepaymentOption option = new LoanProductRepaymentOption();
                    option.setLoanProduct(product);
                    option.setRepaymentMethod(method);
                    return option;
                })
                .collect(Collectors.toList());

        loanProductRepaymentOptionRepository.saveAll(options);
    }
    private void saveInterestRateOptions(CreditLoanProduct product, List<Long> typeIds) {
        List<InterestRateType> types = interestRateTypeService.findAllById(typeIds);

        if (types.size() != typeIds.size()) {
            throw new InvalidInterestRateTypeId("유효하지 않은 금리 유형 ID가 포함되어 있습니다.");
        }

        List<LoanProductInterestRateTypeOption> options = types.stream()
                .map(type -> {
                    LoanProductInterestRateTypeOption option = new LoanProductInterestRateTypeOption();
                    option.setLoanProduct(product);
                    option.setInterestRateType(type);
                    return option;
                })
                .collect(Collectors.toList());

        loanProductInterestRateTypeOptionRepository.saveAll(options);
    }
    private LoanProductRequest createRequest(String name, String slug, String desc,
                                             BigDecimal maxAmount, BigDecimal minAmount, BigDecimal spread,
                                             List<Long> rateIds, List<Long> methodIds) {
        LoanProductRequest req = new LoanProductRequest();
        req.setLoanProductName(name);
        req.setLoanProductSlug(slug);
        req.setLoanProductDescription(desc);
        req.setLoanType("CREDIT");
        req.setStatus(ProductStatus.ACTIVE);

        // 한도 설정 (10만원 단위)
        req.setMaxLoanAmount(maxAmount);
        req.setMinLoanAmount(minAmount);
        req.setApplicationAmountUnit(new BigDecimal("100000"));

        // 기간 설정 (12개월 단위 적용)
        req.setMinLoanTerm(12);
        req.setMaxLoanTerm(60);
        req.setApplicationTermUnit(12); // UI에서 1년 단위 선택


        // 비상금 대출 같은 경우 1년 고정인 경우가 많으므로 예외 처리
        if (slug.contains("emergency")) {
            req.setMinLoanTerm(12);
            req.setMaxLoanTerm(12);
        }

        // 금리
        req.setDefaultSpread(spread);

        // 옵션 연결
        req.setInterestRateTypeIds(rateIds);
        req.setRepaymentMethodIds(methodIds);

        return req;
    }

}
