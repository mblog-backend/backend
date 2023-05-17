package st.coo.memo.dto.comment;

import lombok.Data;

import java.util.List;

@Data
public class SaveCommentRequest {
    private String content;
    private int memoId;

}
