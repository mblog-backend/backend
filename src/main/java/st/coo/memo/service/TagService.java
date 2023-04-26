package st.coo.memo.service;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.dto.tag.TagDto;
import st.coo.memo.entity.TTag;
import st.coo.memo.mapper.TagMapperExt;

import java.util.List;

@Component
@Slf4j
public class TagService {

    @Resource
    private TagMapperExt tagMapper;


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
}
