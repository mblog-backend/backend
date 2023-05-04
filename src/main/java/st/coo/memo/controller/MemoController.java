package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
public class MemoController {

    @Resource
    private MemoService memoService;

    @PostMapping("/save")
    @SaCheckLogin
    public ResponseDTO<Void> create(@RequestBody @Validated SaveMemoRequest saveMemoRequest) {
        memoService.save(saveMemoRequest);

        return ResponseDTO.success();
    }

    @PostMapping("/update")
    @SaCheckLogin
    public ResponseDTO<Void> update(@RequestBody @Validated SaveMemoRequest updateMemoRequest) {
        memoService.update(updateMemoRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/remove")
    @SaCheckLogin
    public ResponseDTO<Void> remove(@RequestParam("id") int id) {
        memoService.remove(id);
        return ResponseDTO.success();
    }

    @PostMapping("/{id}")
    public ResponseDTO<MemoDto> get(@PathVariable("id") int id) {
        return ResponseDTO.success(memoService.get(id));
    }

    @PostMapping("/list")
    public ResponseDTO<ListMemoResponse> list(@RequestBody @Validated ListMemoRequest listMemoRequest) {
        return ResponseDTO.success(memoService.listNormal(listMemoRequest));
    }

    @PostMapping("/listArchived")
    @SaCheckLogin
    public ResponseDTO<ListMemoResponse> listArchived(@RequestBody @Validated ListMemoRequest listMemoRequest) {
        return ResponseDTO.success(memoService.listArchived(listMemoRequest));
    }

    @PostMapping("/statistics")
    @SaCheckLogin
    public ResponseDTO<StatisticsResponse> statistics(@RequestBody @Validated StatisticsRequest statisticsRequest) {
        return ResponseDTO.success(memoService.statistics(statisticsRequest));
    }
}
