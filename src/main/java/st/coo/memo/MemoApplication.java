package st.coo.memo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("st.coo.memo.mapper")
public class MemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoApplication.class, args);
    }

}
