package st.coo.memo.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import st.coo.memo.entity.TComment;

public interface CommentMapperExt extends BaseMapper<TComment> {
    long countMemoByUser(int userId);

    long countMemoByMentioned(@Param("userId") int userId, @Param("profile") String profile);
}
