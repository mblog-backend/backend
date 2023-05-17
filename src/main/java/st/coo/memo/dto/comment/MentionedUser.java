package st.coo.memo.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MentionedUser {
    private int id;
    private String name;
}
