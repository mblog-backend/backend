package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.memo.MemoStatisticsDto;
import st.coo.memo.dto.user.*;
import st.coo.memo.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册用户", description = "需要系统设置里开发注册")
    public ResponseDTO<Void> register(@RequestBody @Validated RegisterUserRequest registerUserRequest) {
        userService.register(registerUserRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/update")
    @SaCheckLogin()
    @Operation(summary = "更新用户", description = "更新用户")
    public ResponseDTO<Void> update(@RequestBody @Validated UpdateUserRequest updateUserRequest) {
        userService.update(updateUserRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "获取用户详情", description = "获取用户详情")
    public ResponseDTO<UserDto> get(@PathVariable("id") int id) {
        return ResponseDTO.success(userService.get(id));
    }

    @PostMapping("/current")
    @Operation(summary = "获取当前登录用户详情", description = "获取当前登录用户详情")
    public ResponseDTO<UserDto> current() {
        return ResponseDTO.success(userService.current());
    }


    @PostMapping("/list")
    @SaCheckRole(value = "ADMIN")
    @Operation(summary = "获取用户列表", description = "只有管理才能调用")
    public ResponseDTO<List<UserDto>> list() {
        return ResponseDTO.success(userService.list());
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "登录")
    public ResponseDTO<LoginResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        return ResponseDTO.success(userService.login(loginRequest));
    }

    @PostMapping("/logout")
    @SaCheckLogin
    @Operation(summary = "登出", description = "登出")
    public ResponseDTO<Void> logout() {
        userService.logout();
        return ResponseDTO.success();
    }

    @PostMapping("/listNames")
    @SaCheckLogin()
    @Operation(summary = "获取用户名称列表", description = "前端评论@时使用")
    public ResponseDTO<List<String>> listNames() {
        return ResponseDTO.success(userService.listNames());
    }


    @PostMapping("/statistics")
    @Operation(summary = "获取当前登录用户统计", description = "获取当前登录用户统计")
    public ResponseDTO<MemoStatisticsDto> getMentionedCount() {
        return ResponseDTO.success(userService.statistics());
    }

}
