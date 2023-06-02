package st.coo.memo.common;

import com.mybatisflex.core.audit.AuditManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Configuration
public class MyBatisFlexConfig {

    private static final Logger logger = LoggerFactory.getLogger("mybatis-flex-sql");

    public MyBatisFlexConfig() {
//        //开启审计功能
//        AuditManager.setAuditEnable(true);
//
//        //设置 SQL 审计收集器
//        AuditManager.setMessageCollector(auditMessage ->
//                logger.info("{},{}ms,hit count:{}", auditMessage.getFullSql(), auditMessage.getElapsedTime(),auditMessage.getQueryCount())
//        );
    }

}
