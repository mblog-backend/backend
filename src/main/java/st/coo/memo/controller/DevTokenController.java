package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.token.TokenDto;
import st.coo.memo.service.DevTokenService;

import java.util.List;

@RestController
@RequestMapping("/api/token")
@SaCheckRole("ADMIN")
public class DevTokenController {

    @Resource
    private DevTokenService devTokenService;

    @GetMapping("/list")
    public ResponseDTO<List<TokenDto>> list(){
        return ResponseDTO.success(devTokenService.list());
    }

    @PostMapping("/reset")
    public ResponseDTO<Void> reset(){
        devTokenService.reset();
        return ResponseDTO.success();
    }
    @PostMapping("/enable")
    public ResponseDTO<Void> enable(){
        devTokenService.enable();
        return ResponseDTO.success();
    }
}
