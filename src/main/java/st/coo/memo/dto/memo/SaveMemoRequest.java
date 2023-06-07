package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import st.coo.memo.common.Visibility;

import java.util.List;

@Data
@Schema
public class SaveMemoRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,title = "memoID",description = "修改时必填",example = "1")
    private int id;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "内容,支持markdown",example = "### hello world!")
    private String content;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "资源ID列表,调用上传资源接口获取,支持markdown",example = "[\"1\"]")
    private List<String> publicIds;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "可见性,PUBLIC:公开所有人可见,PROTECT:登录用户所见,PRIVATE:只有自己可见",example = "PUBLIC")
    private Visibility visibility;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "是否可以评论",example = "false")
    private boolean enableComment;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "来源,自定义",example = "web",defaultValue = "web")
    private String source;
}
