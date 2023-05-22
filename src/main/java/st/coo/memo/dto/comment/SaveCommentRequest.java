package st.coo.memo.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class SaveCommentRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,title = "评论内容,支持markdown",example = "### hello world!")
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,title = "memo ID",example = "1")
    private int memoId;

}
