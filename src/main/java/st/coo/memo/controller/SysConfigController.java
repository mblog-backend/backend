package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.service.SysConfigService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sysConfig")
@SaCheckRole("ADMIN")
public class SysConfigController {


    @Resource
    private SysConfigService sysConfigService;

    @PostMapping("/save")
    public ResponseDTO<Void> save(@RequestBody SaveSysConfigRequest saveSysConfigRequest) {
        sysConfigService.save(saveSysConfigRequest);
        return ResponseDTO.success();
    }

    @GetMapping("/get")
    public ResponseDTO<List<SysConfigDto>> get() {
        return ResponseDTO.success(sysConfigService.getAll());
    }
}
