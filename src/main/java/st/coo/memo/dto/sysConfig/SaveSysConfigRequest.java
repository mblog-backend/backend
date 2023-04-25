package st.coo.memo.dto.sysConfig;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaveSysConfigRequest {
    @NotEmpty(message = "items must not be null")
    private List<SysConfigDto> items;
}
