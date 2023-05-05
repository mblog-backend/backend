package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.jwt.JWTUtil;
import com.google.common.collect.Maps;
import com.mybatisflex.core.query.QueryCondition;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import st.coo.memo.common.BizException;
import st.coo.memo.common.LoginType;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.TokenType;
import st.coo.memo.dto.token.TokenDto;
import st.coo.memo.entity.TDevToken;
import st.coo.memo.mapper.DevTokenMapperExt;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static st.coo.memo.entity.table.Tables.T_DEV_TOKEN;

@Slf4j
@Component
public class DevTokenService {

    @Resource
    private DevTokenMapperExt devTokenMapperExt;

    @Value("${dev.token.jwt.secret:zAchuHafE7Hi2hIfRaRl}")
    private String secret;

    public List<TokenDto> list() {
        return devTokenMapperExt.selectAll().stream().map(r -> {
            TokenDto dto = new TokenDto();
            BeanUtils.copyProperties(r, dto);
            return dto;
        }).toList();
    }

    public void reset(int id) {
        TDevToken token = devTokenMapperExt.selectOneById(id);
        if (token == null) {
            throw new BizException(ResponseCode.fail, "token不存在");
        }
        TDevToken newToken = new TDevToken();
        newToken.setToken(generatePayload(token, "default"));
        devTokenMapperExt.updateByCondition(newToken, QueryCondition.create(T_DEV_TOKEN.ID, id));
    }

    public void remove(int id) {
        TDevToken token = devTokenMapperExt.selectOneById(id);
        if (token == null) {
            throw new BizException(ResponseCode.fail, "token不存在");
        }
        devTokenMapperExt.deleteById(id);
        StpUtil.logout(1,LoginType.API.name());
    }

    @PostConstruct
    public void init() {
        TDevToken token = devTokenMapperExt.selectOneByCondition(QueryCondition.create(T_DEV_TOKEN.NAME, "default"));
        if (token == null) {
            token = new TDevToken();
            token.setTokenType(TokenType.READ.name());
            token.setExpired(LocalDateTime.now().plusYears(100));
            token.setToken(generatePayload(token, "default"));
            token.setName("default");
            devTokenMapperExt.insertSelective(token);
            StpUtil.login(1, LoginType.API.name());
        }
    }

    private String generatePayload(TDevToken token, String name) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("api", true);
        param.put("name", name);
        param.put("expired", token.getExpired());
        return JWTUtil.createToken(param, secret.getBytes(StandardCharsets.UTF_8));
    }

    public void add(TokenDto tokenDto) {
        TDevToken token = new TDevToken();
        BeanUtils.copyProperties(tokenDto, token);
        token.setToken(generatePayload(token, token.getName()));
        devTokenMapperExt.insertSelective(token);
        StpUtil.login(1, LoginType.API.name());
    }
}
