package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.dto.comment.SaveCommentRequest;
import st.coo.memo.entity.TComment;
import st.coo.memo.entity.TMemo;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.CommentMapperExt;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.UserMapperExt;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final Pattern pattern = Pattern.compile("(@.*?)\\s+",Pattern.MULTILINE);

    public void addComment(SaveCommentRequest saveCommentRequest){
        TUser user = userMapperExt.selectOneById(StpUtil.getLoginIdAsInt());
        TMemo memo = memoMapperExt.selectOneById(saveCommentRequest.getMemoId());

        if (!Objects.equals(memo.getEnableComment(),1)){
            throw new BizException(ResponseCode.fail,"禁止评论");
        }

        String content = saveCommentRequest.getContent();
        Matcher matcher = pattern.matcher(content);
        List<String> mentioned = Lists.newArrayList();
        while (matcher.find()) {
            String username = matcher.group().trim();
            if (StringUtils.isNotEmpty(username)){
                mentioned.add(username);
            }
        }

        transactionTemplate.executeWithoutResult(s->{
            TComment comment = new TComment();
            comment.setContent(content);
            comment.setMemoId(saveCommentRequest.getMemoId());
            comment.setUserId(user.getId());
            comment.setUserName(user.getDisplayName());
            comment.setMentioned(Joiner.on(",").join(mentioned));

            Assert.isTrue(memoMapperExt.addCommentCount(memo.getId()) == 1,"更新评论数量异常");
            Assert.isTrue(commentMapperExt.insertSelective(comment) == 1,"写入评论异常");
        });
    }
}