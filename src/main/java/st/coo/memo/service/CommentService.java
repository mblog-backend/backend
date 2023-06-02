package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.comment.*;
import st.coo.memo.entity.TComment;
import st.coo.memo.entity.TMemo;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.CommentMapperExt;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.UserMapperExt;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static st.coo.memo.entity.table.Tables.T_COMMENT;
import static st.coo.memo.entity.table.Tables.T_USER;

@Slf4j
@Component
public class CommentService {

    @Resource
    private CommentMapperExt commentMapperExt;

    @Resource
    private MemoMapperExt memoMapperExt;
    @Resource
    private UserMapperExt userMapperExt;

    @Resource
    private TransactionTemplate transactionTemplate;

    private final Pattern pattern = Pattern.compile("(@.*?)\\s+", Pattern.MULTILINE);

    @Resource
    private SysConfigService sysConfigService;

    public void addComment(SaveCommentRequest saveCommentRequest) {
        TMemo memo = memoMapperExt.selectOneById(saveCommentRequest.getMemoId());
        boolean openComment = sysConfigService.getBoolean(SysConfigConstant.OPEN_COMMENT);

        if (!openComment || !Objects.equals(memo.getEnableComment(), 1)) {
            throw new BizException(ResponseCode.fail, "禁止评论");
        }

        int userId = -1;
        String authorName = saveCommentRequest.getUsername();

        if (StpUtil.isLogin()) {
            TUser user = userMapperExt.selectOneById(StpUtil.getLoginIdAsInt());
            userId = user.getId();
            authorName = user.getDisplayName();
        } else {
            boolean anonymousComment = sysConfigService.getBoolean(SysConfigConstant.ANONYMOUS_COMMENT);
            if (!anonymousComment) {
                throw new BizException(ResponseCode.fail, "不支持匿名评论");
            }
        }

        boolean commentApproved = sysConfigService.getBoolean(SysConfigConstant.COMMENT_APPROVED);

        String content = saveCommentRequest.getContent();
        Matcher matcher = pattern.matcher(content);
        List<MentionedUser> mentioned = Lists.newArrayList();
        while (matcher.find()) {
            String username = matcher.group().trim();
            if (StringUtils.isNotEmpty(username)) {
                username = username.substring(1);
                TUser mentionedUser = userMapperExt.selectOneByQuery(QueryWrapper.create().and(T_USER.DISPLAY_NAME.eq(username)));
                if (mentionedUser != null) {
                    mentioned.add(new MentionedUser(mentionedUser.getId(), mentionedUser.getDisplayName()));
                }
            }
        }
        String names = Joiner.on(",").join(mentioned.stream().map(MentionedUser::getName).collect(Collectors.toList()));
        String ids = Joiner.on(",#").join(mentioned.stream().map(MentionedUser::getId).collect(Collectors.toList()));
        TComment comment = new TComment();
        comment.setContent(content);
        comment.setMemoId(saveCommentRequest.getMemoId());
        comment.setUserId(userId);
        comment.setUserName(authorName);
        comment.setMentioned(names);
        comment.setMentionedUserId(StringUtils.isEmpty(ids) ? "" : "#" + ids + ",");
        comment.setCreated(new Timestamp(System.currentTimeMillis()));
        comment.setUpdated(new Timestamp(System.currentTimeMillis()));
        if (!StpUtil.isLogin()) {
            comment.setEmail(saveCommentRequest.getEmail());
            comment.setLink(saveCommentRequest.getLink());
            if (commentApproved) {
                comment.setApproved(0);
            }else{
                comment.setApproved(1);
            }
        }

        transactionTemplate.executeWithoutResult(s -> {
            Assert.isTrue(memoMapperExt.addCommentCount(memo.getId()) == 1, "更新评论数量异常");
            Assert.isTrue(commentMapperExt.insertSelective(comment) == 1, "写入评论异常");
        });
    }


    public void removeComment(int id) {
        TUser user = userMapperExt.selectOneById(StpUtil.getLoginIdAsInt());
        TComment comment = commentMapperExt.selectOneById(id);
        TMemo memo = memoMapperExt.selectOneById(comment.getMemoId());
        if (Objects.equals(user.getRole(), "ADMIN") || !Objects.equals(memo.getUserId(), user.getId())) {
            throw new BizException(ResponseCode.fail, "只能删除自己发的memo的评论");
        }
        commentMapperExt.deleteById(id);
    }


    public QueryCommentListResponse query(QueryCommentListRequest request) {
        Page<TComment> paginate = commentMapperExt.paginate(request.getPage(), request.getSize(), QueryWrapper.create()
                .and(T_COMMENT.MEMO_ID.eq(request.getMemoId()))
                .and(T_COMMENT.USER_ID.gt(0).or(T_COMMENT.USER_ID.lt(0).and(T_COMMENT.APPROVED.eq(1))).when(!StpUtil.hasRole("ADMIN")))
                .orderBy("created"));
        QueryCommentListResponse response = new QueryCommentListResponse();
        response.setList(paginate.getRecords().stream().map(r -> {
            CommentDto commentDto = new CommentDto();
            BeanUtils.copyProperties(r, commentDto);
            return commentDto;
        }).toList());
        response.setTotal(paginate.getTotalRow());
        response.setTotalPage(paginate.getTotalPage());
        return response;
    }

    public void singleApprove(int commentId) {
        TComment comment = new TComment();
        comment.setApproved(1);
        commentMapperExt.updateByQuery(comment, true, QueryWrapper.create().and(T_COMMENT.ID.eq(commentId)).and(T_COMMENT.USER_ID.lt(0)));
    }

    public void memoApprove(int memoId) {
        TComment comment = new TComment();
        comment.setApproved(1);
        commentMapperExt.updateByQuery(comment, true, QueryWrapper.create().and(T_COMMENT.MEMO_ID.eq(memoId)).and(T_COMMENT.USER_ID.lt(0)));
    }


}
