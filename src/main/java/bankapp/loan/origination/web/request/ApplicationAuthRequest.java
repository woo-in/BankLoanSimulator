package bankapp.loan.origination.web.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApplicationAuthRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
