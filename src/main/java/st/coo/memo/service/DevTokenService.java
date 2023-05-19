package st.coo.memo.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.collect.Lists;
import com.mybatisflex.core.query.QueryCondition;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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

    private final static long apiExpiredSeconds = 100L * 365 * 24 * 60 * 60;

    @Resource
    private DevTokenMapperExt devTokenMapperExt;


    public TokenDto get() {
        int userId = StpUtil.getLoginIdAsInt();
        List<TokenDto> list = devTokenMapperExt.selectListByCondition(QueryCondition.create(T_DEV_TOKEN.NAME, "default")
                .and(T_DEV_TOKEN.USER_ID.eq(userId))).stream().map(r -> {
            TokenDto dto = new TokenDto();
            BeanUtils.copyProperties(r, dto);
            return dto;
        }).toList();
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public void reset(int id) {
        int userId = StpUtil.getLoginIdAsInt();
        TDevToken token = devTokenMapperExt.selectOneByCondition(QueryCondition.create(T_DEV_TOKEN.NAME, "default").and(T_DEV_TOKEN.USER_ID.eq(userId)));
        if (token == null) {
            throw new BizException(ResponseCode.fail, "token不存在");
        }
        StpUtil.login(userId, new SaLoginModel().setDevice(LoginType.API.name()).setTimeout(apiExpiredSeconds));
        TDevToken newToken = new TDevToken();
        newToken.setToken(StpUtil.getTokenInfo().getTokenValue());
        devTokenMapperExt.updateByCondition(newToken, QueryCondition.create(T_DEV_TOKEN.ID, id));
    }


    public void enable() {
        int id = StpUtil.getLoginIdAsInt();
        TDevToken token = devTokenMapperExt.selectOneByCondition(QueryCondition.create(
                T_DEV_TOKEN.NAME, "default").and(T_DEV_TOKEN.USER_ID.eq(id)));
        if (token == null) {
            StpUtil.login(id, new SaLoginModel()
                    .setDevice(LoginType.API.name())
                    .setTimeout(apiExpiredSeconds)
                    .setIsWriteHeader(false)
                    .setIsLastingCookie(false)
            );
            token = new TDevToken();
            token.setToken(StpUtil.getTokenInfo().getTokenValue());
            token.setName("default");
            token.setUserId(id);
            devTokenMapperExt.insertSelective(token);
        }
    }

    public void disable() {
        int id = StpUtil.getLoginIdAsInt();
        devTokenMapperExt.deleteByCondition(QueryCondition.create(T_DEV_TOKEN.NAME, "default").and(T_DEV_TOKEN.USER_ID.eq(id)));
    }
}
