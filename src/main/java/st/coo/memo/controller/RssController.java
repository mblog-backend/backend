package st.coo.memo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.coo.memo.service.RssService;

@RestController
@RequestMapping("/rss")
@Tag(name = "RSS管理", description = "RSS管理")
public class RssController {

    @Resource
    private RssService rssService;

    @GetMapping
    @Operation(summary = "生成RSS", description = "生成RSS")
    public void rss(HttpServletResponse httpServletResponse){
        rssService.rss(httpServletResponse);
    }
}
