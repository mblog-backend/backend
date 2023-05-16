package st.coo.memo.dto.comment;

import lombok.Data;

import java.util.List;

@Data
public class QueryCommentListRequest {
    private int page;
    private int size;
    private int memoId;

}
