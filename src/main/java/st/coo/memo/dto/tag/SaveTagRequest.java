package st.coo.memo.dto.tag;

import lombok.Data;

import java.util.List;

@Data
public class SaveTagRequest {
    private List<TagUpdateDto> list;
}
