package st.coo.memo.dto.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema
public class SaveSysConfigRequest {
    @NotEmpty(message = "items must not be null")
    @Schema
    private List<SysConfigDto> items;
}
