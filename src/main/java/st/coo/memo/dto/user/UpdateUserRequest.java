package st.coo.memo.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String email;
    private String bio;
}
