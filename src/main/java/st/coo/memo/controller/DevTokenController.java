package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.token.TokenDto;
import st.coo.memo.service.DevTokenService;

@RestController
@RequestMapping("/api/token")
@SaCheckLogin
@Tag(name = "开发者管理", description = "开发者管理")
public class DevTokenController {

    @Resource
    private DevTokenService devTokenService;

    @GetMapping("/")
    @Operation(summary = "查询当前登录用户的API token", description = "查询当前登录用户的API token")
    public ResponseDTO<TokenDto> list(){
        return ResponseDTO.success(devTokenService.get());
    }

    @PostMapping("/reset")
    @Operation(summary = "重置当前登录用户的API token", description = "只能重置自己的")
    public ResponseDTO<Void> reset(@Parameter(required = true,description = "token id",example = "1") @RequestParam("id") int id){
        devTokenService.reset(id);
        return ResponseDTO.success();
    }
    @PostMapping("/enable")
    @Operation(summary = "启用API token", description = "启用API token")
    public ResponseDTO<Void> enable(){
        devTokenService.enable();
        return ResponseDTO.success();
    }

    @PostMapping("/disable")
    @Operation(summary = "禁用API token", description = "禁用API token")
    public ResponseDTO<Void> disable(){
        devTokenService.disable();
        return ResponseDTO.success();
    }
}
