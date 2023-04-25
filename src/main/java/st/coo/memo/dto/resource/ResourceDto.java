package st.coo.memo.dto.resource;

import lombok.Data;

@Data
public class ResourceDto {
    private String publicId;
    private String fileType;
    private String fileName;
    private String fileHash;
    private long fileSize;
    private String internalPath;

}
