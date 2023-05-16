package st.coo.memo.dto.comment;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CommentDto {

    private int id;
    private int memoId;
    private String userName;
    private int userId;
    private Timestamp created;
    private Timestamp updated;
    private String content;
    private List<String> mentioned;
}
