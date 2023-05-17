package st.coo.memo.dto.memo;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemoStatisticsDto {
    private long total;
    private long liked;
    private long mentioned;
    private long commented;
    private long unreadMentioned;
}
