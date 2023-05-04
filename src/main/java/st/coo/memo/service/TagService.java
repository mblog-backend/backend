package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import st.coo.memo.dto.tag.TagDto;
import st.coo.memo.dto.tag.TagUpdateDto;
import st.coo.memo.entity.TMemo;
import st.coo.memo.entity.TTag;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.TagMapperExt;

import java.util.List;

import static st.coo.memo.entity.table.Tables.T_MEMO;
import static st.coo.memo.entity.table.Tables.T_TAG;

@Component
@Slf4j
public class TagService {

    @Resource
    private TagMapperExt tagMapper;
    @Resource
    private MemoMapperExt memoMapperExt;

    public List<TagDto> list() {
        return tagMapper.selectAll().stream().map(this::convertToDto).toList();
    }

    public List<TagDto> top10Tags() {
        List<TTag> rows = tagMapper.selectListByQuery(QueryWrapper.create()
                .orderBy("memo_count desc").limit(10));
        return rows.stream().map(this::convertToDto).toList();
    }

    private TagDto convertToDto(TTag tag) {
        TagDto dto = new TagDto();
        BeanUtils.copyProperties(tag, dto);
        dto.setCount(tag.getMemoCount());
        return dto;
    }

    public void remove(int id) {
        int userId = StpUtil.getLoginIdAsInt();
        tagMapper.deleteByQuery(QueryWrapper.create().and(T_TAG.USER_ID.eq(userId).and(T_TAG.ID.eq(id))).and(T_TAG.MEMO_COUNT.eq(0)));
    }

    @Transactional
    public void save(List<TagUpdateDto> list) {
        for (TagUpdateDto dto : list) {
            TTag tag = new TTag();
            tag.setName(dto.getName());

            TTag oldTag = tagMapper.selectOneById(dto.getId());
            int row = tagMapper.updateByQuery(tag, true, QueryWrapper.create().and(T_TAG.ID.eq(dto.getId())));
            Assert.isTrue(row == 1,"更新tag异常");

            List<TMemo> memos = memoMapperExt.selectListByQuery(QueryWrapper.create().and(T_MEMO.TAGS.like(oldTag.getName() + ",")));
            for (TMemo memo : memos) {
                TMemo newMemo = new TMemo();
                String tags = memo.getTags();
                newMemo.setTags(tags.replaceFirst(oldTag.getName() + ",", dto.getName() + ","));
                int num = memoMapperExt.updateByQuery(newMemo, true, QueryWrapper.create().and(T_MEMO.ID.eq(memo.getId())));
                Assert.isTrue(num == 1,"更新memo异常");
            }
        }
    }
}
