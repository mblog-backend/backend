package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemoRelationRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "MemoId",example = "1")
    private int memoId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "类型,LIKE:点赞",example = "LIKE")
    private String type;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "操作类型,ADD:增加,REMOVE:删除",example = "ADD")
    private String operateType;

}
