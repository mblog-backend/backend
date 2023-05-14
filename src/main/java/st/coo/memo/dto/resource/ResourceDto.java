package st.coo.memo.dto.resource;

import lombok.Data;

@Data
public class ResourceDto {
    private String publicId;
    private String url;
    private String fileType;
    private String suffix;
    private String storageType;

}
