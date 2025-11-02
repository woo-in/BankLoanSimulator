package bankapp.loan.model.common.product;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Data;

@Embeddable
@Data
public class LoanProductDetail {

    @Lob
    private String productDescription;

    @Lob
    private String productSubject;

    protected LoanProductDetail() {}

    public LoanProductDetail(String productDescription, String productSubject) {
        this.productDescription = productDescription;
        this.productSubject = productSubject;
    }
}
