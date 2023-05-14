package st.coo.memo.dto.resource;

import lombok.Data;

@Data
public class UploadResourceResponse {
    private String publicId;
    private String url;
    private String suffix;
    private String storageType;
}
