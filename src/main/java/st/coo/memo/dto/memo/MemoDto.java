package st.coo.memo.dto.memo;

import lombok.Data;

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
    private Integer authorId;

    private List<ResourceItem>resources;

    @Data
    public static class ResourceItem{
        private String publicId;
        private String url;
        private String fileType;
    }
}
