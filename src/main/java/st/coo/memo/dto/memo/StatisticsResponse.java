package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StatisticsResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计memo数量")
    private long totalMemos;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "注册天数")
    private long totalDays;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "总计tag数量")
    private long totalTags;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "按天统计memo数量")
    private List<Item> items;

    @Data
    public static class Item{
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "日期")
        private String date;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, title = "memo数量")
        private long total;
    }
}
