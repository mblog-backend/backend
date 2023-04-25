package st.coo.memo.service.resource;

import st.coo.memo.common.StorageType;

public interface ResourceProvider {
    StorageType type();
    String upload(String filePath);
}