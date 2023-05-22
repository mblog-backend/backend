package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.resource.UploadResourceResponse;
import st.coo.memo.service.resource.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@Tag(name = "资源管理", description = "资源管理")
public class ResourceController {

    @Resource
    private ResourceService resourceService;

    @PostMapping("/upload")
    @SaCheckLogin
    @Operation(summary = "上传资源", description = "上传资源")
    public ResponseDTO<List<UploadResourceResponse>> upload(@RequestParam("files") MultipartFile files[]) {
        return ResponseDTO.success(resourceService.upload(files));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "访问资源", description = "访问资源")
    public void get(@PathVariable("publicId") String publicId, HttpServletResponse httpServletResponse){
        resourceService.get(publicId,httpServletResponse);
    }
}
