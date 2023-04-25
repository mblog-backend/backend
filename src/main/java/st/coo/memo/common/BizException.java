package st.coo.memo.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BizException extends RuntimeException{
    private int code;
    private String msg;

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
    }

    public BizException(ResponseCode responseCode,String msg) {
        super(msg);
        this.code = responseCode.getCode();
        this.msg = msg;
    }
}
