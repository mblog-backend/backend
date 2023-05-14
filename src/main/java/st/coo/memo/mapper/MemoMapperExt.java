package st.coo.memo.mapper;

import com.mybatisflex.core.BaseMapper;
import st.coo.memo.dto.memo.ListMemoRequest;
import st.coo.memo.dto.memo.MemoDto;
import st.coo.memo.entity.TMemo;

import java.util.List;

public interface MemoMapperExt extends BaseMapper<TMemo> {

    List<MemoDto> listMemos(ListMemoRequest request);
    long countMemos(ListMemoRequest request);
    int setPriority(int id);
    int unSetPriority(int id);

}
