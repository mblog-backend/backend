package st.coo.memo;

import cn.dev33.satoken.secure.BCrypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class MemoApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(BCrypt.hashpw("7mZS38b8jxuCVH9kxokW"));
    }

}
