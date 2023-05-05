package st.coo.memo.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryCondition;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.common.BizException;
import st.coo.memo.common.LoginType;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.dto.token.TokenDto;
import st.coo.memo.entity.TDevToken;
import st.coo.memo.mapper.DevTokenMapperExt;

import java.util.List;

import static st.coo.memo.entity.table.Tables.T_DEV_TOKEN;

@Slf4j
@Component
public class DevTokenService {

    private final static long apiExpiredSeconds = 100 * 365*24*60*60;

    @Resource
    private DevTokenMapperExt devTokenMapperExt;

    public List<TokenDto> list() {
        return devTokenMapperExt.selectAll().stream().map(r -> {
            TokenDto dto = new TokenDto();
            BeanUtils.copyProperties(r, dto);
            return dto;
        }).toList();
    }

    public void reset() {
        TDevToken token = devTokenMapperExt.selectOneById(1);
        if (token == null) {
            throw new BizException(ResponseCode.fail, "token不存在");
        }
        StpUtil.logout(1, LoginType.API.name());
        StpUtil.login(1,new SaLoginModel().setDevice(LoginType.API.name()).setTimeout(apiExpiredSeconds));
        TDevToken newToken = new TDevToken();
        newToken.setToken(StpUtil.getTokenInfo().getTokenValue());
        devTokenMapperExt.updateByCondition(newToken, QueryCondition.create(T_DEV_TOKEN.ID, 1));
    }


    @PostConstruct
    public void init() {
        TDevToken token = devTokenMapperExt.selectOneByCondition(QueryCondition.create(T_DEV_TOKEN.NAME, "default"));
        if (token == null) {
            StpUtil.login(1,new SaLoginModel().setDevice(LoginType.API.name()).setTimeout(apiExpiredSeconds));
            token = new TDevToken();
            token.setToken(StpUtil.getTokenInfo().getTokenValue());
            token.setName("default");
            devTokenMapperExt.insertSelective(token);
        }
    }
}
