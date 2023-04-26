package st.coo.memo.service;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.entity.TSysConfig;
import st.coo.memo.mapper.TSysConfigMapper;

import java.util.List;

import static st.coo.memo.entity.table.Tables.T_SYS_CONFIG;

@Slf4j
@Component
public class SysConfigService {

    @Resource
    private TSysConfigMapper sysConfigMapper;

    public void save(SaveSysConfigRequest saveSysConfigRequest) {
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
