package st.coo.memo.service.resource;

import cn.hutool.core.io.file.FileNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import st.coo.memo.dto.resource.UploadResourceResponse;

@Component
@Slf4j
public class LocalResourceProvider implements ResourceProvider {


    @Override
    public UploadResourceResponse upload(String filePath, String publicId) {
        UploadResourceResponse response = new UploadResourceResponse();
        response.setUrl("/api/resource/" + publicId);
        response.setPublicId(publicId);
        response.setSuffix("");
        return response;
    }
}
