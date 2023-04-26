package st.coo.memo.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.user.*;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.TUserMapper;

import java.util.List;
import java.util.stream.Collectors;

import static st.coo.memo.entity.table.Tables.T_USER;

@Slf4j
@Component
public class UserService {

    @Resource
    private TUserMapper userMapper;

    @Resource
    private SysConfigService sysConfigService;


    public void register(RegisterUserRequest registerUserRequest) {

        boolean openRegister = sysConfigService.getBoolean(SysConfigConstant.OPEN_REGISTER);
        if (!openRegister) {
            throw new BizException(ResponseCode.fail, "当前不允许注册");
        }

        TUser user = new TUser();
        user.setEmail(registerUserRequest.getEmail());
        user.setBio(registerUserRequest.getBio());
        user.setUsername(registerUserRequest.getUsername());
        user.setDisplayName(StringUtils.defaultString(registerUserRequest.getDisplayName(), registerUserRequest.getUsername()));
        user.setPasswordHash(BCrypt.hashpw(registerUserRequest.getPassword()));
        userMapper.insertSelective(user);
    }

    public void update(UpdateUserRequest updateUserRequest) {
        TUser user = new TUser();
        user.setId(StpUtil.getLoginIdAsInt());
        BeanUtils.copyProperties(updateUserRequest, user);
        userMapper.update(user, true);
    }


    public UserDto get(int id) {
        TUser user = userMapper.selectOneById(id);
        UserDto userDto = new UserDto();
        if (user == null) {
            return null;
        }
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public UserDto current() {
        TUser user = userMapper.selectOneById(StpUtil.getLoginIdAsInt());
        UserDto userDto = new UserDto();
        if (user == null) {
            return null;
        }
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }
    public List<UserDto> list() {
        List<TUser> list = userMapper.selectAll();
        return list.stream().map(user -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            return userDto;
        }).collect(Collectors.toList());
    }


    public LoginResponse login(LoginRequest loginRequest) {
        TUser user = userMapper.selectOneByQuery(QueryWrapper.create()
                .and(T_USER.USERNAME.eq(loginRequest.getUsername()))
        );

        if (user == null) {
            throw new BizException(ResponseCode.fail, "用户不存在");
        }
        if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BizException(ResponseCode.fail, "密码不正确");
        }

        LoginResponse response = new LoginResponse();
        StpUtil.login(user.getId());
        response.setUsername(loginRequest.getUsername());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        response.setToken(tokenInfo.getTokenValue());
        return response;
    }


    public void logout() {
        StpUtil.logout();
    }
}
