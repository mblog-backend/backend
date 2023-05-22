package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.comment.QueryCommentListRequest;
import st.coo.memo.dto.comment.QueryCommentListResponse;
import st.coo.memo.dto.comment.SaveCommentRequest;
import st.coo.memo.service.CommentService;

@Tag(name = "评论管理", description = "评论管理")
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    @SaCheckLogin
    @Operation(summary = "发表评论", description = "发表评论")
    public ResponseDTO<Void> addComment(@RequestBody SaveCommentRequest saveCommentRequest) {
        commentService.addComment(saveCommentRequest);
        return ResponseDTO.success();
    }

    @PostMapping("/remove")
    @SaCheckLogin
    @Operation(summary = "删除评论", description = "自己只能删除自己的,管理员能删除所有人的")
    public ResponseDTO<Void> remove(@Parameter(required = true,description = "评论的ID",example = "1") @RequestParam("id") int id) {
        commentService.removeComment(id);
        return ResponseDTO.success();
    }


    @PostMapping("/query")
    @Operation(summary = "评论查询", description = "支持分页查询")
    public ResponseDTO<QueryCommentListResponse> query(@RequestBody QueryCommentListRequest request) {
        return ResponseDTO.success(commentService.query(request));
    }
}
