package st.coo.memo.common;

import java.util.Arrays;
import java.util.Objects;

public enum StorageType {
    LOCAL,
    QINIU,
    ALIYUN,
    TENCENT;

    public static StorageType get(String value) {
        return Arrays.stream(StorageType.values()).filter(r -> Objects.equals(r.name(), value)).findFirst().orElseThrow();
    }
}
