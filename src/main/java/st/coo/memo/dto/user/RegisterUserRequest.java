package st.coo.memo.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotEmpty(message = "username cannot be null")
    private String username;
    @NotEmpty(message = "password cannot be null")
    private String password;

    private String displayName;
    private String email;
    private String bio;
}
