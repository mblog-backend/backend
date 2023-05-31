package st.coo.memo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserDto {
    private Integer id;

    private String username;

    private String email;

    private String displayName;

    private String bio;

    private Timestamp created;

    private Timestamp updated;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "角色名,ADMIN:管理员,USER:普通用户", example = "ADMIN")

    private String role;
    private String avatarUrl;
    private String defaultVisibility;
    private String defaultEnableComment;
}
