package st.coo.memo.service.resource;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.StorageType;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.resource.UploadResourceResponse;
import st.coo.memo.entity.TResource;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.ResourceMapperExt;
import st.coo.memo.service.SysConfigService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ResourceService implements ApplicationContextAware {

    @Resource
    private SysConfigService sysConfigService;

    private ApplicationContext applicationContext;

    @Value("${upload.storage.path}")
    private String tempPath;

    @Resource
    private ResourceMapperExt resourceMapper;

    @Resource
    private MemoMapperExt memoMapper;
    private final static Map<StorageType, Class<? extends ResourceProvider>> RESOURCE_PROVIDER_MAP = Maps.newHashMap();

    public ResourceService() {
        RESOURCE_PROVIDER_MAP.put(StorageType.LOCAL, LocalResourceProvider.class);
        RESOURCE_PROVIDER_MAP.put(StorageType.QINIU, QiNiuResourceProvider.class);
    }

    public List<UploadResourceResponse> upload(MultipartFile[] multipartFiles) {
        String value = sysConfigService.getString(SysConfigConstant.STORAGE_TYPE);
        StorageType storageType = StorageType.get(value);
        Class<? extends ResourceProvider> cls = RESOURCE_PROVIDER_MAP.get(storageType);
        ResourceProvider provider = applicationContext.getBean(cls);
        List<UploadResourceResponse> result = Lists.newArrayList();
        for (MultipartFile multipartFile : multipartFiles) {
            result.add(upload(multipartFile, storageType, provider));
        }
        return result;
    }

    private UploadResourceResponse upload(MultipartFile multipartFile, StorageType storageType, ResourceProvider provider) {
        String publicId = DateFormatUtils.format(new Date(),"YYYMMDDHHmmss")+RandomStringUtils.randomAlphabetic(20);
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = publicId + "." + FileNameUtil.getSuffix(originalFilename);
        String parentDir = DateFormatUtils.format(new Date(), "yyyyMMdd");
        String targetPath = tempPath + File.separator + parentDir + File.separator + fileName;
        byte[] content;
        String fileType;
        try {
            FileUtil.mkParentDirs(targetPath);
            content = multipartFile.getBytes();
            FileOutputStream target = new FileOutputStream(targetPath);
            FileCopyUtils.copy(multipartFile.getInputStream(), target);
            fileType = Files.probeContentType(new File(targetPath).toPath());
        } catch (Exception e) {
            log.error("upload resource error", e);
            throw new BizException(ResponseCode.fail, "上传文件异常:" + e.getLocalizedMessage());
        }
        UploadResourceResponse uploadResourceResponse = provider.upload(targetPath,publicId);

        TResource tResource = new TResource();
        tResource.setPublicId(publicId);
        tResource.setFileType(fileType);
        tResource.setFileName(originalFilename);
        tResource.setSuffix(uploadResourceResponse.getSuffix());
        tResource.setFileHash(DigestUtils.md5DigestAsHex(content));
        tResource.setSize(multipartFile.getSize());
        tResource.setMemoId(0);
        tResource.setInternalPath(targetPath);
        tResource.setExternalLink(uploadResourceResponse.getUrl());
        tResource.setStorageType(storageType.name());
        tResource.setUserId(StpUtil.getLoginIdAsInt());
        resourceMapper.insertSelective(tResource);
        uploadResourceResponse.setStorageType(storageType.name());
        return uploadResourceResponse;
    }

    public void get(String publicId, HttpServletResponse httpServletResponse) {
        TResource tResource = resourceMapper.selectOneById(publicId);
        if (tResource == null) {
            throw new BizException(ResponseCode.fail, "resource不存在");
        }
        if (Objects.equals(tResource.getStorageType(), StorageType.LOCAL.name())) {
            File file = new File(tResource.getInternalPath());
            httpServletResponse.setContentType(tResource.getFileType());
            try {
                FileCopyUtils.copy(new FileInputStream(file), httpServletResponse.getOutputStream());
            } catch (IOException e) {
                log.error("get resource {} error", publicId, e);
                throw new BizException(ResponseCode.fail, "获取resource异常");
            }
            return;
        }
        httpServletResponse.setStatus(302);
        httpServletResponse.setHeader("Location", tResource.getExternalLink());
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
