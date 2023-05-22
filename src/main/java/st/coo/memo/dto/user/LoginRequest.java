package st.coo.memo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema
public class LoginRequest {
    @NotEmpty(message = "username cannot be null")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "登录名", example = "tom")
    private String username;

    @NotEmpty(message = "password cannot be null")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "密码", example = "a123456")
    private String password;
}
