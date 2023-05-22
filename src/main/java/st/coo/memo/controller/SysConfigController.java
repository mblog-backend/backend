package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "系统设置管理", description = "系统设置管理")
public class SysConfigController {


    @Resource
    private SysConfigService sysConfigService;

    @PostMapping("/save")
    @SaCheckRole("ADMIN")
    @Operation(summary = "保存系统设置", description = "只有管理员才能调用")
    public ResponseDTO<Void> save(@RequestBody SaveSysConfigRequest saveSysConfigRequest) {
        sysConfigService.save(saveSysConfigRequest);
        return ResponseDTO.success();
    }

    @GetMapping("/get")
    @SaCheckRole("ADMIN")
    @Operation(summary = "获取所有的系统设置", description = "只有管理员才能调用")
    public ResponseDTO<List<SysConfigDto>> get() {
        return ResponseDTO.success(sysConfigService.getAll());
    }

    @GetMapping("/")
    @Operation(summary = "获取前端需要的系统设置", description = "任何人都可以调用")
    public ResponseDTO<List<SysConfigDto>> getConfig() {
        List<String> keys = Lists.newArrayList(SysConfigConstant.OPEN_REGISTER,
                SysConfigConstant.WEBSITE_TITLE,
                SysConfigConstant.OPEN_COMMENT,
                SysConfigConstant.OPEN_LIKE,
                SysConfigConstant.MEMO_MAX_LENGTH,
                SysConfigConstant.INDEX_WIDTH
                );
        return ResponseDTO.success(sysConfigService.getAll(keys));
    }
}
