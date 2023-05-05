package st.coo.memo.dto.token;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenDto {
    private int id;
    private String name;
    private String token;
    private String tokenType;
    private LocalDateTime expired;
}
