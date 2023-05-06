package st.coo.memo.common;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSON;
import com.google.gson.Gson;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

import static st.coo.memo.entity.table.Tables.T_DEV_TOKEN;

@Configuration
@Slf4j
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(auth -> {
            log.info("isLogin:{},StpUtil.getLoginType():{}",StpUtil.isLogin(),StpUtil.getLoginType());
            if (StpUtil.isLogin() && Objects.equals(StpUtil.getLoginDevice(), LoginType.API.name())) {

                long count = Db.selectCountByQuery("t_dev_token", QueryWrapper.create().and(T_DEV_TOKEN.TOKEN.eq(StpUtil.getTokenValue())));
                if (count == 0  ) {
                    ResponseDTO<Void> responseDTO = ResponseDTO.fail(ResponseCode.need_login.getCode(),"api token已失效");
                    Gson gson = new Gson();
                    SaRouter.back(gson.toJson(responseDTO));
                }

            }
        })).addPathPatterns("/**");
    }


    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }
}
