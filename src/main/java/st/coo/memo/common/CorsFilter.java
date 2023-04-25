package st.coo.memo.common;

import com.google.common.base.Splitter;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import st.coo.memo.service.SysConfigService;

import java.io.IOException;

@Component
@Slf4j
@WebFilter(urlPatterns = {"/*"}, filterName = "corsFilter")
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("启动跨域过滤器");
    }

    @Resource
    private SysConfigService sysConfigService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;

        String domains = sysConfigService.getString(SysConfigConstant.CORS_DOMAIN_LIST);
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String origin = httpServletRequest.getHeader("Origin");
        if (StringUtils.isNotEmpty(domains) &&
                StringUtils.isNotEmpty(origin) &&
                Splitter.on(",").omitEmptyStrings().splitToList(domains).contains(origin)) {
            // 手动设置响应头解决跨域访问
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
            // 设置过期时间
            response.setHeader("Access-Control-Max-Age", "86400");
            response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, uuid");
            // 支持 HTTP 1.1
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // 支持 HTTP 1.0. response.setHeader("Expires", "0");
            response.setHeader("Pragma", "no-cache");
        }

        // 编码
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, resp);
    }

    @Override
    public void destroy() {
        log.info("销毁跨域过滤器");
    }

}
