package st.coo.memo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "昵称",description = "不填默认取username")
    private String displayName;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "email")
    private String email;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, title = "个人介绍,支持markdown")
    private String bio;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "头像URL")
    private String avatarUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "密码", example = "a123456")
    private String password;
}
