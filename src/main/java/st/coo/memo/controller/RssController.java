package st.coo.memo.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.coo.memo.service.RssService;

@RestController
@RequestMapping("/rss")
public class RssController {

    @Resource
    private RssService rssService;

    @GetMapping
    public void rss(HttpServletResponse httpServletResponse){
        rssService.rss(httpServletResponse);
    }
}
