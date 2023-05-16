package st.coo.memo.dto.comment;

import lombok.Data;

import java.util.List;

@Data
public class QueryCommentListResponse {
    private long total;
    private long totalPage;
    private List<CommentDto> list;

}
