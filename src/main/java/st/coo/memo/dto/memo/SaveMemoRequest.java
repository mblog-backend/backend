package st.coo.memo.dto.memo;

import lombok.Data;
import st.coo.memo.common.Visibility;

import java.util.List;

@Data
public class SaveMemoRequest {
    private int id;
    private String content;
    private List<String> publicIds;
    private Visibility visibility;
    private Integer priority;
    private boolean enableComment;
}
