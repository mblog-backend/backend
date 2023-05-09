package st.coo.memo.dto.memo;

import lombok.Data;
import st.coo.memo.common.MemoStatus;
import st.coo.memo.common.Visibility;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
public class ListMemoRequest {
    private int page = 1;
    private int size = 20;
    private String tag;
    private Visibility visibility;
    private int userId;
    private Date begin;
    private Date end;

}
