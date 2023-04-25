package st.coo.memo.dto.memo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StatisticsResponse {
    private long totalMemos;
    private long totalDays;
    private long totalTags;
    private List<Item> items;

    @Data
    public static class Item{
        private String date;
        private long total;
    }
}
