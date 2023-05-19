package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.token.TokenDto;
import st.coo.memo.service.DevTokenService;

@RestController
@RequestMapping("/api/token")
@SaCheckLogin
public class DevTokenController {

    @Resource
    private DevTokenService devTokenService;

    @GetMapping("/")
    public ResponseDTO<TokenDto> list(){
        return ResponseDTO.success(devTokenService.get());
    }

    @PostMapping("/reset")
    public ResponseDTO<Void> reset(@RequestParam("id") int id){
        devTokenService.reset(id);
        return ResponseDTO.success();
    }
    @PostMapping("/enable")
    public ResponseDTO<Void> enable(){
        devTokenService.enable();
        return ResponseDTO.success();
    }

    @PostMapping("/disable")
    public ResponseDTO<Void> disable(){
        devTokenService.disable();
        return ResponseDTO.success();
    }
}
