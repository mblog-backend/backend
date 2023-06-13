package st.coo.memo.service;

import cn.dev33.satoken.secure.SaBase64Util;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.entity.TSysConfig;
import st.coo.memo.mapper.SysConfigMapperExt;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static st.coo.memo.entity.table.Tables.T_SYS_CONFIG;

@Slf4j
@Component
public class SysConfigService {

    @Resource
    private SysConfigMapperExt sysConfigMapper;

    @Value("${official.square.url}")
    private String officialSquareUrl;

    @Resource
    private HttpClient httpClient;


    @PostConstruct
    public void init(){
        TSysConfig sysConfig = new TSysConfig();
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        sysConfig.setValue(SaBase64Util.encodeBytesToString(key));
        sysConfigMapper.updateByQuery(sysConfig,QueryWrapper.create().and(T_SYS_CONFIG.KEY.eq(SysConfigConstant.WEB_HOOK_TOKEN))
                .and(T_SYS_CONFIG.VALUE.isNull().or(T_SYS_CONFIG.VALUE.eq(""))));
    }

    public void save(SaveSysConfigRequest saveSysConfigRequest) {

        Optional<SysConfigDto> push2OfficialSquare = saveSysConfigRequest.getItems().stream()
                .filter(r -> Objects.equals(r.getKey(), SysConfigConstant.PUSH_OFFICIAL_SQUARE) && Objects.equals("true", r.getValue())).findFirst();

        String token = getString(SysConfigConstant.WEB_HOOK_TOKEN);

        if (push2OfficialSquare.isPresent()){
            String url = officialSquareUrl+"/api/token/add";
            Map<String, Object> map = Maps.newHashMap();
            map.put("token", token);
            String body = new Gson().toJson(map);
            log.info("注册token {},body:{}", url, body);
            Stopwatch stopwatch = Stopwatch.createStarted();
            HttpPost request = new HttpPost(url);
            request.setHeader("content-type", "application/json;charset=utf8");
            try {
                request.setEntity(new StringEntity(body));
                HttpResponse httpResponse = httpClient.execute(request);
                String response = EntityUtils.toString(httpResponse.getEntity());
                log.info("注册token,返回码:{},body:{},耗时:{}ms", httpResponse.getStatusLine().getStatusCode(),response, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } catch (IOException e) {
                log.error("注册token异常", e);
                throw new BizException(ResponseCode.fail,"连接广场异常,请查看后台日志");
            }
        }

        for (SysConfigDto item : saveSysConfigRequest.getItems()) {
            TSysConfig sysConfig = new TSysConfig();
            BeanUtils.copyProperties(item, sysConfig);
            sysConfigMapper.update(sysConfig);
        }
    }

    public List<SysConfigDto> getAll() {
        List<TSysConfig> list = sysConfigMapper.selectAll();
        return list.stream().map(r -> {
            SysConfigDto dto = new SysConfigDto();
            BeanUtils.copyProperties(r, dto);
            if (StringUtils.isEmpty(dto.getValue())) {
                dto.setValue(r.getDefaultValue());
            }
            return dto;
        }).toList();
    }

    public List<SysConfigDto> getAll(List<String> keys) {
        List<TSysConfig> list = sysConfigMapper.selectListByQuery(QueryWrapper.create().and(T_SYS_CONFIG.KEY.in(keys)));
        return list.stream().map(r -> {
            SysConfigDto dto = new SysConfigDto();
            BeanUtils.copyProperties(r, dto);
            if (StringUtils.isEmpty(dto.getValue())) {
                dto.setValue(r.getDefaultValue());
            }
            return dto;
        }).toList();
    }

    public boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) return false;
        return BooleanUtils.toBoolean(value);
    }

    public long getNumber(String key) {
        String value = getString(key);
        if (value == null) return 0;
        return Long.parseLong(value);
    }

    public String getString(String key) {
        TSysConfig sysConfig = sysConfigMapper.selectOneById(key);
        if (sysConfig == null) {
            return null;
        }
        return StringUtils.defaultString(sysConfig.getValue(), sysConfig.getDefaultValue());
    }
}
