package st.coo.memo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.coo.memo.common.ResponseDTO;
import st.coo.memo.dto.comment.SaveCommentRequest;
import st.coo.memo.service.CommentService;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    @SaCheckLogin
    public ResponseDTO<Void> addComment(@RequestBody SaveCommentRequest saveCommentRequest) {
        commentService.addComment(saveCommentRequest);
        return ResponseDTO.success();
    }

}
