package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import st.coo.memo.dto.resource.ResourceDto;

import java.sql.Timestamp;
import java.util.List;

@Data
public class MemoDto {
    @Schema(title = "memo ID")
    private Integer id;

    @Schema(title = "用户ID")
    private Integer userId;

    @Schema(title = "内容")
    private String content;

    @Schema(title = "标签",description = "多个逗号分割")
    private String tags;

    @Schema(title = "可见性")
    private String visibility;

    @Schema(title = "状态:NORMAL:正常")
    private String status;

    @Schema(title = "创建时间")
    private Timestamp created;

    @Schema(title = "更新时间")
    private Timestamp updated;

    @Schema(title = "作者名称")
    private String authorName;

    @Schema(title = "作者角色")
    private String authorRole;

    @Schema(title = "作者email")
    private String email;

    @Schema(title = "作者个人简介")
    private String bio;

    @Schema(title = "优先级")
    private int priority;

    @Schema(title = "评论数量")
    private int commentCount;

    @Schema(title = "未审核评论数量")
    private long unApprovedCommentCount;

    @Schema(title = "点赞数量")
    private int likeCount;

    @Schema(title = "是否开启评论")
    private int enableComment;

    @Schema(title = "查看数量")
    private int viewCount;

    @Schema(title = "当前登录用户是否点赞")
    private int liked;

    @Schema(title = "资源列表")
    private List<ResourceDto>resources;


    @Schema(title = "来源")
    private String source;
}
