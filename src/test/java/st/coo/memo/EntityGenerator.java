package st.coo.memo;

import com.google.common.collect.Sets;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class EntityGenerator {
    public static void main(String[] args) {
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/memo?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&autoReconnect=true&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("tester");
        dataSource.setPassword("tester");

        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置只生成哪些表
//        globalConfig.addGenerateTable("account", "account_session");
        globalConfig.setUnGenerateTables(Sets.newHashSet("databasechangelog","databasechangeloglock"));

        //设置 entity 的包名
        globalConfig.setEntityPackage("st.coo.memo.entity");

        //设置表前缀
        //globalConfig.setTablePrefix("tb_");

        //设置 entity 是否使用 Lombok
        //globalConfig.setEntityWithLombok(true);

        //是否生成 mapper 类，默认为 false
        //globalConfig.setMapperGenerateEnable(true);

        //设置 mapper 类的包名
        globalConfig.setMapperPackage("st.coo.memo.mapper");

        //可以单独配置某个列
//        ColumnConfig columnConfig = new ColumnConfig();
//        columnConfig.setColumnName("tenant_id");
//        columnConfig.setLarge(true);
//        columnConfig.setVersion(true);
//        globalConfig.addColumnConfig("account",columnConfig);


        JdbcTypeMapping.registerMapping(BigInteger.class, Long.class);

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }
}
