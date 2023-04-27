package st.coo.memo.dto.user;

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

    private String role;
    private String avatarUrl;
}
