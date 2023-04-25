package st.coo.memo.common;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDTO<T> {

    private T data;
    private int code;
    private String msg;

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(data, 0, "");
    }

    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>(null, 0, "");
    }

    public static <T> ResponseDTO<T> fail(BizException bizException) {
        return new ResponseDTO<>(null, bizException.getCode(), bizException.getMsg());
    }

    public static <T> ResponseDTO<T> fail(ResponseCode responseCode) {
        return new ResponseDTO<>(null, responseCode.getCode(), responseCode.getMsg());
    }

    public static <T> ResponseDTO<T> fail(int code, String msg) {
        return new ResponseDTO<>(null, code, msg);
    }
}
