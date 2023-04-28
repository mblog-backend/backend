package st.coo.memo.service.resource;

import cn.hutool.core.io.file.FileNameUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import st.coo.memo.common.StorageType;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.service.SysConfigService;

import java.util.Objects;

@Component
@Slf4j
public class LocalResourceProvider implements ResourceProvider {

    @Resource
    private SysConfigService sysConfigService;


    @Override
    public StorageType type() {
        return StorageType.LOCAL;
    }


    @Override
    public String upload(String filePath) {
        String publicId = FileNameUtil.getPrefix(filePath);
        return "/api/resource/"+publicId;
    }
}
