package st.coo.memo.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class QueryCommentListRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "第几页,从1开始", example = "1")
    private int page;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "页大小", example = "20")
    private int size;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "MemoID", example = "1")
    private int memoId;

}
