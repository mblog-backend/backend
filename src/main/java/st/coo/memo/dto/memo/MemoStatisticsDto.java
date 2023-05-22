package st.coo.memo.dto.memo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemoStatisticsDto {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计memo数量")
    private long total;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计点赞数量")
    private long liked;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计被提到数量")
    private long mentioned;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计评论数量")
    private long commented;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计未读数量")
    private long unreadMentioned;
}
