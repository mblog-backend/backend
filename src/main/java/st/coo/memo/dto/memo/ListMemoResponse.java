package st.coo.memo.dto.memo;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ListMemoResponse {

    private List<MemoDto> items;
    private long total;


}
