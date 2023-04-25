package st.coo.memo.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;


@Table(value = "t_tag")
public class TTag implements Serializable {

    
    @Id
    private String name;

    
    @Id
    private Integer userId;

    
    private Timestamp created;

    
    private Timestamp updated;

    
    private Integer memoCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    public Integer getMemoCount() {
        return memoCount;
    }

    public void setMemoCount(Integer memoCount) {
        this.memoCount = memoCount;
    }

}
