package st.coo.memo.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import st.coo.memo.entity.TTag;

public interface TagMapperExt extends BaseMapper<TTag> {

    @Update("update t_tag set memo_count = memo_count +1 where user_id = #{userId} and name = #{name}")
    int incrementTagCount(@Param("userId")int userId,@Param("name")String name);

    @Update("update t_tag set memo_count = memo_count -1 where user_id = #{userId} and name = #{name} and memo_count >= 1")
    int decrementTagCount(@Param("userId")int userId,@Param("name")String name);

}
