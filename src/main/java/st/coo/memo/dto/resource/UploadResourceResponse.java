package st.coo.memo.dto.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UploadResourceResponse {
    @Schema(title = "资源ID")
    private String publicId;
    @Schema(title = "资源访问地址")
    private String url;
    @Schema(title = "资源文件类型")
    private String suffix;
    @Schema(title = "存储类型,LOCAL:本地存储,QINIU:七牛云存储")
    private String storageType;
}
