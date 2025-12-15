package bankapp.loan.product.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("CREDIT")
@SuperBuilder
@NoArgsConstructor
public class CreditLoanProduct extends LoanProduct { }
