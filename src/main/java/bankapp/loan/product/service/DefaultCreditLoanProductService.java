package bankapp.loan.product.service;

import bankapp.loan.exceptions.*;
import bankapp.loan.product.model.InterestRateType;
import bankapp.loan.product.model.LoanProductInterestRateTypeOption;
import bankapp.loan.product.model.LoanProductRepaymentOption;
import bankapp.loan.product.model.RepaymentMethod;
import bankapp.loan.product.model.CreditLoanProduct;
import bankapp.loan.product.repository.LoanProductInterestRateTypeOptionRepository;
import bankapp.loan.product.repository.LoanProductRepaymentOptionRepository;
import bankapp.loan.product.repository.CreditLoanProductRepository;
import bankapp.loan.web.request.LoanProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
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

}
