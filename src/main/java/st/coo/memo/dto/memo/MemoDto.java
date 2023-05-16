package st.coo.memo.dto.memo;

import lombok.Data;
import st.coo.memo.dto.resource.ResourceDto;

import java.sql.Timestamp;
import java.util.List;

@Data
public class MemoDto {
    private Integer id;

    private Integer userId;

    private String content;

    private String tags;

    private String visibility;

    private String status;

    private Timestamp created;

    private Timestamp updated;

    private String authorName;
    private String authorRole;
    private String email;
    private String bio;
    private int priority;
    private int commentCount;
    private int likeCount;
    private int enableComment;
    private int viewCount;
    private int liked;

    private List<ResourceDto>resources;


}
