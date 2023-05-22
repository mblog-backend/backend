package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema
public class StatisticsRequest {
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按开始时间搜索",example = "2023-05-20T16:00:00.000Z")
    private Date begin;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,description = "按结束时间搜索",example = "2023-05-21T15:59:59.999Z")
    private Date end;
}
