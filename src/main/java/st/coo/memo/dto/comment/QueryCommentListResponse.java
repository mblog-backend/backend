package st.coo.memo.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class QueryCommentListResponse {
    @Schema(title = "总条数")
    private long total;
    @Schema(title = "总页数")
    private long totalPage;
    @Schema(title = "评论列表")
    private List<CommentDto> list;

}
