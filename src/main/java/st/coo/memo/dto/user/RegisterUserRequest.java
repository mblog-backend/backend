package st.coo.memo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotEmpty(message = "username cannot be null")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "登录名", example = "tom")
    private String username;

    @NotEmpty(message = "password cannot be null")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "密码", example = "a123456")
    private String password;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "昵称",description = "不填默认取username")
    private String displayName;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "email")
    private String email;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "个人介绍,支持markdown")
    private String bio;
}
