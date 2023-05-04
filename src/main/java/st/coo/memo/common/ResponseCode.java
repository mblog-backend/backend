package st.coo.memo.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResponseCode {
    success(0, ""),
    param_error(1,"param_error"),
    fail(2,"fail"),
    need_login(3,"please login first"),
    file_size_limit_exceeded(4,"file size limit exceeded"),
    system_exception(99,"system_exception")
    ;

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
