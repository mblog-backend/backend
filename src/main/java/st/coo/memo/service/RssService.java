package st.coo.memo.service;

import com.google.common.base.Splitter;
import com.mybatisflex.core.query.QueryWrapper;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import st.coo.memo.common.BizException;
import st.coo.memo.common.ResponseCode;
import st.coo.memo.common.SysConfigConstant;
import st.coo.memo.dto.memo.ListMemoRequest;
import st.coo.memo.dto.memo.ListMemoResponse;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.UserMapperExt;

import java.util.List;

import static st.coo.memo.entity.table.Tables.T_USER;

@Slf4j
@Component
public class RssService {

    @Resource
    private MemoMapperExt memoMapper;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private UserMapperExt userMapper;


    private final static Splitter SPLITTER = Splitter.on(",").omitEmptyStrings();
    @Resource
    private MemoService memoService;

    public void rss(HttpServletResponse httpServletResponse) {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");

        TUser admin = userMapper.selectOneByQuery(QueryWrapper.create().and(T_USER.ROLE.eq("ADMIN")));
        if (admin == null) {
            throw new BizException(ResponseCode.fail, "管理员不存在");
        }

        feed.setTitle(sysConfigService.getString(SysConfigConstant.WEBSITE_TITLE));
        String domain = sysConfigService.getString(SysConfigConstant.DOMAIN);
        feed.setLink(domain);
        feed.setDescription(admin.getBio());

        ListMemoResponse list = memoService.listNormal(new ListMemoRequest());
        if (list != null && !CollectionUtils.isEmpty(list.getItems())) {
            feed.setEntries(list.getItems().stream().map(r -> {
                SyndEntry entry = new SyndEntryImpl();
                SyndContent content = new SyndContentImpl();
                content.setValue(r.getContent());
                entry.setTitle(StringUtils.substring(r.getContent(),0,20));
                entry.setLink(domain + "/memo/" + r.getId());
                entry.setDescription(content);
                entry.setPublishedDate(r.getCreated());
                entry.setUpdatedDate(r.getUpdated());
                entry.setAuthor(r.getAuthorName());

                List<String> tags = SPLITTER.splitToList(StringUtils.defaultString(r.getTags(),""));
                List<SyndCategory> categories = tags.stream().map(tag -> {
                    SyndCategory category = new SyndCategoryImpl();
                    category.setLabel(tag);
                    category.setName(tag);
                    return category;
                }).toList();
                entry.setCategories(categories);
                return entry;
            }).toList());
        }

        SyndFeedOutput output = new SyndFeedOutput();
        try {
            output.output(feed, httpServletResponse.getWriter());
        } catch (Exception e) {
            log.error("output feed error", e);
        }
    }
}
