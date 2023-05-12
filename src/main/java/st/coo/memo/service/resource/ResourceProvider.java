package st.coo.memo.service.resource;

import st.coo.memo.dto.resource.UploadResourceResponse;

public interface ResourceProvider {
    UploadResourceResponse upload(String filePath, String publicId);
}