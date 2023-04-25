package st.coo.memo.dto.memo;

import lombok.Data;

import java.util.Date;

@Data
public class StatisticsRequest {
    private Date begin;
    private Date end;
}
