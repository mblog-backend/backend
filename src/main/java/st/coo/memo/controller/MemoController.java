package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.memo.*;
import st.coo.memo.service.MemoService;

@Slf4j
@RestController
@RequestMapping("/api/memo")
@Tag(name = "Memo管理", description = "Memo管理")
public class MemoController {

    @Resource
    private MemoService memoService;

    @PostMapping("/save")
    @SaCheckLogin
    @Operation(summary = "发布memo", description = "发布memo")
    public ResponseDTO<Void> create(@RequestBody @Validated SaveMemoRequest saveMemoRequest) {
        memoService.save(saveMemoRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/update")
    @SaCheckLogin
    @Operation(summary = "修改memo", description = "修改memo")
    public ResponseDTO<Void> update(@RequestBody @Validated SaveMemoRequest updateMemoRequest) {
        memoService.update(updateMemoRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/remove")
    @SaCheckLogin
    @Operation(summary = "删除memo", description = "只能删除自己的memo,管理员可以删除所有人的")
    public ResponseDTO<Void> remove(@Parameter(required = true,description = "Memo ID") @RequestParam("id") int id) {
        memoService.remove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/setPriority")
    @SaCheckLogin
    @Operation(summary = "置顶/取消置顶 Memo", description = "置顶/取消置顶 Memo")
    public ResponseDTO<Void> setTop(@Parameter(required = true,description = "Memo ID")  @RequestParam("id") int id,
                                    @Parameter(required = true,description = "true:置顶,false:取消置顶") @RequestParam("set") boolean set) {
        memoService.setMemoPriority(id,set);
        return ResponseDTO.success();
    }

    @PostMapping("/{id}")
    @Operation(summary = "获取单条Memo详情", description = "获取单条Memo详情")
    public ResponseDTO<MemoDto> get(@PathVariable("id") int id,@RequestParam(name = "count",defaultValue = "false") boolean count) {
        return ResponseDTO.success(memoService.get(id,count));
    }

    @PostMapping("/list")
    @Operation(summary = "获取Memo列表", description = "获取Memo列表")
    public ResponseDTO<ListMemoResponse> list(@RequestBody @Validated ListMemoRequest listMemoRequest) {
        return ResponseDTO.success(memoService.listNormal(listMemoRequest));
    }

    @PostMapping("/statistics")
    @Operation(summary = "Memo统计", description = "Memo统计")
    public ResponseDTO<StatisticsResponse> statistics(@RequestBody @Validated StatisticsRequest statisticsRequest) {
        return ResponseDTO.success(memoService.statistics(statisticsRequest));
    }

    @PostMapping("/relation")
    @SaCheckLogin
    @Operation(summary = "点赞Memo", description = "点赞Memo")
    public ResponseDTO<Void> relation(@RequestBody MemoRelationRequest request ){
        memoService.makeRelation(request);
        return ResponseDTO.success();
    }


}
