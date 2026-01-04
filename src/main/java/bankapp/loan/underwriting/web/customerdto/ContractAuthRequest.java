package bankapp.loan.underwriting.web.customerdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContractAuthRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
