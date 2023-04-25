package st.coo.memo.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @NotEmpty(message = "username不能为空")
    private String username;
    @NotEmpty(message = "password不能为空")
    private String password;
}
