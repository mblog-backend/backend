package st.coo.memo.mapper;

import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import st.coo.memo.entity.TResource;

public interface ResourceMapperExt extends BaseMapper<TResource> {

    @Update("update t_resource set memo_id = 0 where memo_id = #{memoId} ")
    int clearMemoResource(@Param("memoId") int memoId);

}
