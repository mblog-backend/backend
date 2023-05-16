package st.coo.memo.dto.memo;

import lombok.Data;

@Data
public class MemoRelationRequest {
    private int memoId;
    private String type;
    private String operateType;

}
