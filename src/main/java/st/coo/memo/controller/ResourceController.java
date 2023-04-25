package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
public class ResourceController {

    @Resource
    private ResourceService resourceService;

    @PostMapping("/upload")
    @SaCheckLogin
    public ResponseDTO<List<UploadResourceResponse>> upload(@RequestParam("files") MultipartFile files[]) {
        return ResponseDTO.success(resourceService.upload(files));
    }

    @GetMapping("/{publicId}")
    public void get(@PathVariable("publicId") String publicId, HttpServletResponse httpServletResponse){
        resourceService.get(publicId,httpServletResponse);
    }
}
