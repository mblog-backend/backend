package st.coo.memo.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TagUpdateDto {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "标签ID")
    private int id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,description = "标签名称")
    private String name;
}
