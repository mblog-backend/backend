package st.coo.memo.dto.memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ListMemoResponse {

    private List<MemoDto> items;

    @Schema(title = "总条数")
    private long total;
    @Schema(title = "总页数")
    private long totalPage;


}
