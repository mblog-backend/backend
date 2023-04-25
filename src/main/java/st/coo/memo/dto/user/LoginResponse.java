package st.coo.memo.dto.user;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
}
