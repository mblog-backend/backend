package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.sysConfig.SaveSysConfigRequest;
import st.coo.memo.dto.sysConfig.SysConfigDto;
import st.coo.memo.service.SysConfigService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sysConfig")
public class SysConfigController {


    @Resource
    private SysConfigService sysConfigService;

    @PostMapping("/save")
    @SaCheckRole("ADMIN")
    public ResponseDTO<Void> save(@RequestBody SaveSysConfigRequest saveSysConfigRequest) {
        sysConfigService.save(saveSysConfigRequest);
        return ResponseDTO.success();
    }

    @GetMapping("/get")
    @SaCheckRole("ADMIN")
    public ResponseDTO<List<SysConfigDto>> get() {
        return ResponseDTO.success(sysConfigService.getAll());
    }

    @GetMapping("/")
    public ResponseDTO<List<SysConfigDto>> getConfig() {
        List<String> keys = Lists.newArrayList(SysConfigConstant.OPEN_REGISTER,SysConfigConstant.WEBSITE_TITLE);
        return ResponseDTO.success(sysConfigService.getAll(keys));
    }
}
