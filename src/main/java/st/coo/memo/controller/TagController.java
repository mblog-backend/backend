package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.tag.SaveTagRequest;
import st.coo.memo.dto.tag.TagDto;
import st.coo.memo.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @PostMapping("/list")
    @SaCheckLogin
    public ResponseDTO<List<TagDto>> list() {
        return ResponseDTO.success(tagService.list());
    }

    @PostMapping("/top10")
    @SaCheckLogin
    public ResponseDTO<List<TagDto>> top10() {
        return ResponseDTO.success(tagService.top10Tags());
    }

    @PostMapping("/remove")
    @SaCheckRole("ADMIN")
    public ResponseDTO<Void> remove(@RequestParam("id") int id) {
        tagService.remove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/save")
    @SaCheckRole("ADMIN")
    public ResponseDTO<Void> save(@RequestBody SaveTagRequest request) {
        tagService.save(request);
        return ResponseDTO.success();
    }

}
