package st.coo.memo.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;


@Table(value = "t_comment")
public class TComment implements Serializable {

    
    @Id(keyType = KeyType.Auto)
    private Integer id;

    
    private Integer memoId;

    
    private String content;

    
    private Integer userId;

    
    private String userName;

    
    private String mentioned;

    
    private Timestamp created;

    
    private Timestamp updated;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMemoId() {
        return memoId;
    }

    public void setMemoId(Integer memoId) {
        this.memoId = memoId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMentioned() {
        return mentioned;
    }

    public void setMentioned(String mentioned) {
        this.mentioned = mentioned;
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

}
