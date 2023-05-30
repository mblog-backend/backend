package st.coo.memo.service.resource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.resource.UploadResourceResponse;
import st.coo.memo.service.SysConfigService;

import java.io.File;
import java.net.URL;
import java.util.Map;

@Slf4j
@Component
public class AWSS3ResourceProvider implements ResourceProvider {


    @Resource
    private SysConfigService sysConfigService;


    @Override
    public UploadResourceResponse upload(String filePath, String publicId) {
        String param = sysConfigService.getString(SysConfigConstant.AWSS3_PARAM);
        Map<String, String> map = new Gson().fromJson(param, new TypeToken<Map<String, String>>() {
        }.getType());
        String accessKey = MapUtils.getString(map, "accessKey");
        String secretKey = MapUtils.getString(map, "secretKey");
        String bucket = MapUtils.getString(map, "bucket");
        String domain = MapUtils.getString(map, "domain");
        String prefix = MapUtils.getString(map, "prefix");
        String suffix = MapUtils.getString(map, "suffix");
        String region = MapUtils.getString(map, "region");

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3Client.builder()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();


        String key = publicId;
        if (StringUtils.isNotEmpty(prefix)) {
            key = prefix + "/" + publicId;
        }
        s3Client.putObject(new PutObjectRequest(bucket, key, new File(filePath))
                .withCannedAcl(CannedAccessControlList.PublicRead));
        UploadResourceResponse uploadResourceResponse = new UploadResourceResponse();
        if (StringUtils.isNotEmpty(domain)) {
            uploadResourceResponse.setUrl(domain + "/" + key);
        } else {
            uploadResourceResponse.setUrl(String.format("https://s3.%s.amazonaws.com/%s/%s", region, bucket, key));
        }
        log.info("上传到s3 成功:{}", uploadResourceResponse.getUrl());
        uploadResourceResponse.setSuffix(suffix);
        uploadResourceResponse.setPublicId(publicId);
        return uploadResourceResponse;
    }
}
