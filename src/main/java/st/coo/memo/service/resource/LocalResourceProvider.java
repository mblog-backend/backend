package st.coo.memo.service.resource;

import cn.hutool.core.io.file.FileNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import st.coo.memo.common.StorageType;

@Component
@Slf4j
public class LocalResourceProvider implements ResourceProvider {


    @Override
    public StorageType type() {
        return StorageType.LOCAL;
    }


    @Override
    public String upload(String filePath) {
        String publicId = FileNameUtil.getPrefix(filePath);
        return "/resource/"+publicId;
    }
}
