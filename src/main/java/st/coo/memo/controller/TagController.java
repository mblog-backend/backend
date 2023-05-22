package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.tag.SaveTagRequest;
import st.coo.memo.dto.tag.TagDto;
import st.coo.memo.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
@Tag(name = "标签管理", description = "标签管理")
public class TagController {

    @Resource
    private TagService tagService;

    @PostMapping("/list")
    @SaCheckLogin
    @Operation(summary = "标签列表", description = "获取当前用户的标签列表")
    public ResponseDTO<List<TagDto>> list() {
        return ResponseDTO.success(tagService.list());
    }

    @PostMapping("/top10")
    @Operation(summary = "TOP10标签列表", description = "获取当前用户的TOP10标签列表,未登录则获取管理员的")
    public ResponseDTO<List<TagDto>> top10() {
        return ResponseDTO.success(tagService.top10Tags());
    }

    @PostMapping("/remove")
    @SaCheckLogin()
    @Operation(summary = "删除标签", description = "只能删除自己的标签,且关联的memo数量必须为0")
    public ResponseDTO<Void> remove(@RequestParam("id") int id) {
        tagService.remove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/save")
    @SaCheckLogin()
    @Operation(summary = "更新标签", description = "关联的memo的标签会同步修改")
    public ResponseDTO<Void> save(@RequestBody SaveTagRequest request) {
        tagService.save(request);
        return ResponseDTO.success();
    }

}
