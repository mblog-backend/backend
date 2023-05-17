package st.coo.memo.mapper;

import com.mybatisflex.core.BaseMapper;
import st.coo.memo.entity.TComment;

public interface CommentMapperExt extends BaseMapper<TComment> {
    long countMemoByUser(int userId);
    long countMemoByMentioned(int userId);
}
