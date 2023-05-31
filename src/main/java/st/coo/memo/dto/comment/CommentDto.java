package st.coo.memo.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CommentDto {

    @Schema(title = "评论ID")
    private int id;
    @Schema(title = "memo ID")
    private int memoId;
    @Schema(title = "评论人名称")
    private String userName;
    @Schema(title = "评论人ID")
    private int userId;
    @Schema(title = "评论时间")
    private Timestamp created;
    @Schema(title = "评论修改名称")
    private Timestamp updated;
    @Schema(title = "评论内容,支持markdown")
    private String content;
    @Schema(title = "提到的人,逗号分割")
    private String mentioned;
    @Schema(title = "提到的人的USERID,逗号分割")
    private String mentionedUserId;
    @Schema(title = "匿名人的邮箱")
    private String email;
    @Schema(title = "匿名人的链接")
    private String link;
    @Schema(title = "是否审核通过")
    private int approved;
}
