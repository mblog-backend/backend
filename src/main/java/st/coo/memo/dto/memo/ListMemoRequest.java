package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import st.coo.memo.common.Visibility;

import java.util.Date;

@Data
public class ListMemoRequest {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "页码,从1开始",example = "1")
    private int page = 1;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "页大小",example = "20")
    private int size = 20;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按标签搜索",example = "#life")
    private String tag;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按可见性搜索",example = "#life")
    private Visibility visibility;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按用户ID搜索",example = "#life")
    private int userId;

    @Schema(hidden = true)
    private int currentUserId;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按开始时间搜索",example = "2023-05-20T16:00:00.000Z")
    private Date begin;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按结束时间搜索",example = "2023-05-21T15:59:59.999Z")
    private Date end;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按内容搜索",example = "hello")
    private String search;

    @Schema(hidden = true)
    private boolean login;

    @Schema(hidden = true)
    private boolean liked;

    @Schema(hidden = true)
    private boolean commented;

    @Schema(hidden = true)
    private boolean mentioned;

    @Schema(hidden = true)
    private String dbType;
}
