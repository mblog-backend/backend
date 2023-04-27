package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import st.coo.memo.common.*;
import st.coo.memo.dto.memo.*;
import st.coo.memo.entity.TMemo;
import st.coo.memo.entity.TResource;
import st.coo.memo.entity.TTag;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.*;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static st.coo.memo.entity.table.Tables.*;

@Slf4j
@Component
public class MemoService {

    private final static Splitter SPLITTER = Splitter.onPattern("[\n]");
    private final static Splitter TAG_SPLITTER = Splitter.onPattern("[(\\s+),]").omitEmptyStrings();


    @Resource
    private MemoMapperExt memoMapper;

    @Resource
    private UserMapperExt userMapper;

    @Resource
    private TagMapperExt tagMapper;

    @Resource
    private ResourceMapperExt resourceMapper;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private TransactionTemplate transactionTemplate;


    @Transactional
    public void remove(int id) {
        TMemo tMemo = memoMapper.selectOneById(id);
        if (tMemo == null) {
            return;
        }
        if (StringUtils.hasText(tMemo.getTags())) {
            List<String> tags = Splitter.on(",").splitToList(tMemo.getTags());
            for (String tag : tags) {
                tagMapper.decrementTagCount(StpUtil.getLoginIdAsInt(), tag);
            }
        }
        resourceMapper.deleteByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(id)));
        memoMapper.deleteById(id);
    }

    public void setMemoTop(int id) {
        TMemo tMemo = new TMemo();
        tMemo.setId(id);
        tMemo.setTop("Y");
        memoMapper.update(tMemo, true);
    }

    private String replaceFirstLine(String content, List<String> tags) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        List<String> lines = Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().splitToList(content));
        String firstLine = lines.get(0);
        if (!StringUtils.hasText(firstLine)) {
            return "";
        }
        for (String tag : tags) {
            firstLine = firstLine.replaceFirst(tag + "[,(\\s+)]?", "");
        }
        if (!StringUtils.hasLength(firstLine)) {
            lines.remove(0);
        } else {
            lines.set(0, firstLine);
        }
        return Joiner.on("\n").join(lines);
    }

    public void save(SaveMemoRequest saveMemoRequest) {
        List<String> tags = parseTags(saveMemoRequest.getContent());
        String content = saveMemoRequest.getContent();
        TMemo tMemo = new TMemo();
        tMemo.setTop(BooleanUtils.toString(saveMemoRequest.isTop(), "Y", "N"));
        tMemo.setUserId(StpUtil.getLoginIdAsInt());
        tMemo.setTags(Joiner.on(",").join(tags) + (tags.size() > 0 ? "," : ""));
        if (saveMemoRequest.getVisibility() != null) {
            tMemo.setVisibility(saveMemoRequest.getVisibility().name());
        }
        tMemo.setContent(replaceFirstLine(content, tags).trim());

        List<TTag> existsTagList = tagMapper.selectListByQuery(QueryWrapper.create().
                and(T_TAG.NAME.in(tags)).
                and(T_TAG.USER_ID.eq(StpUtil.getLoginIdAsInt())));

        if (!CollectionUtils.isEmpty(existsTagList)) {
            tags.removeAll(existsTagList.stream().map(TTag::getName).toList());
        }

        transactionTemplate.execute(status -> {
            Assert.isTrue(memoMapper.insertSelective(tMemo) == 1, "新增memo异常");
            for (String name : tags) {
                TTag tag = new TTag();
                tag.setName(name);
                tag.setUserId(StpUtil.getLoginIdAsInt());
                tag.setMemoCount(1);
                Assert.isTrue(tagMapper.insertSelective(tag) == 1, "保存tag异常");
            }
            for (TTag tag : existsTagList) {
                Assert.isTrue(tagMapper.incrementTagCount(StpUtil.getLoginIdAsInt(), tag.getName()) == 1, "更新tag计数异常");
            }
            if (!CollectionUtils.isEmpty(saveMemoRequest.getPublicIds())) {
                for (String publicId : saveMemoRequest.getPublicIds()) {
                    TResource resource = new TResource();
                    resource.setMemoId(tMemo.getId());
                    Assert.isTrue(resourceMapper.updateByQuery(resource, QueryWrapper.create()
                            .and(T_RESOURCE.MEMO_ID.eq(0)).and(T_RESOURCE.PUBLIC_ID.eq(publicId))) == 1, "更新resource异常");
                }
            }
            return true;
        });
    }


    public void update(SaveMemoRequest updateMemoRequest) {
        TMemo existMemo = memoMapper.selectOneById(updateMemoRequest.getId());
        if (existMemo == null) {
            throw new BizException(ResponseCode.fail, "memo不存在");
        }
        String oldTags = existMemo.getTags();
        String content = updateMemoRequest.getContent();
        String[] lines = content.split("\n");
        TMemo tMemo = new TMemo();
        tMemo.setId(existMemo.getId());
        List<String> tags = parseTags(updateMemoRequest.getContent());
        tMemo.setTop(BooleanUtils.toString(updateMemoRequest.isTop(), "Y", "N"));
        tMemo.setTags(Joiner.on(",").join(tags) + (tags.size() > 0 ? "," : ""));
        tMemo.setContent(replaceFirstLine(content, tags).trim());
        if (updateMemoRequest.getVisibility() != null) {
            tMemo.setVisibility(updateMemoRequest.getVisibility().name());
        }
        List<TTag> existsTagList;
        List<String> oldTagList = new ArrayList<>();
        if (StringUtils.hasText(oldTags)) {
            oldTagList = Splitter.on(",").omitEmptyStrings().splitToList(oldTags);
        }
        if (!CollectionUtils.isEmpty(tags)) {
            existsTagList = tagMapper.selectListByQuery(QueryWrapper.create().
                    and(T_TAG.NAME.in(tags)).
                    and(T_TAG.USER_ID.eq(StpUtil.getLoginIdAsInt())));
            tags.removeAll(existsTagList.stream().map(TTag::getName).toList());
        } else {
            existsTagList = Lists.newArrayList();
        }

        List<String> finalOldTagList = oldTagList;
        transactionTemplate.execute(status -> {
            Assert.isTrue(memoMapper.update(tMemo, true) == 1, "保存memo异常");
            for (String name : tags) {
                TTag tag = new TTag();
                tag.setName(name);
                tag.setUserId(StpUtil.getLoginIdAsInt());
                tag.setMemoCount(1);
                Assert.isTrue(tagMapper.insertSelective(tag) == 1, "保存tag异常");
            }
            for (String tag : finalOldTagList) {
                Assert.isTrue(tagMapper.decrementTagCount(StpUtil.getLoginIdAsInt(), tag) == 1, "保存tag异常");
            }
            for (TTag tag : existsTagList) {
                Assert.isTrue(tagMapper.incrementTagCount(StpUtil.getLoginIdAsInt(), tag.getName()) == 1, "保存tag异常");
            }

            resourceMapper.clearMemoResource(tMemo.getId());
            if (!CollectionUtils.isEmpty(updateMemoRequest.getPublicIds())) {
                for (String publicId : updateMemoRequest.getPublicIds()) {
                    TResource resource = new TResource();
                    resource.setMemoId(tMemo.getId());
                    Assert.isTrue(resourceMapper.updateByQuery(resource, QueryWrapper.create()
                            .and(T_RESOURCE.MEMO_ID.eq(0)).and(T_RESOURCE.PUBLIC_ID.eq(publicId))) == 1, "更新resource异常");
                }
            }

            return true;
        });
    }

    private List<String> parseTags(String content) {
        List<String> list = SPLITTER.splitToList(content);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        String firstLine = list.get(0);

        List<String> result = TAG_SPLITTER.splitToList(firstLine);
        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        return result.stream().filter(r -> r.startsWith("#") && r.length() > 1).collect(Collectors.toList());
    }


    @Transactional
    public ListMemoResponse listNormal(ListMemoRequest listMemoRequest) {
        boolean isLogin = StpUtil.isLogin();
        QueryWrapper wrapper = QueryWrapper.create();
        if (StringUtils.hasText(listMemoRequest.getTag())) {
            listMemoRequest.setTag(listMemoRequest.getTag() + ",");
        } else {
            listMemoRequest.setTag(null);
        }
        wrapper.and(T_MEMO.STATUS.eq(MemoStatus.NORMAL.name()).and(
                T_MEMO.TAGS.like(listMemoRequest.getTag())).and(T_MEMO.USER_ID.eq(listMemoRequest.getUserId()).when(listMemoRequest.getUserId() > 0)));
        if (listMemoRequest.getVisibility() != null) {
            wrapper.and(T_MEMO.VISIBILITY.eq(listMemoRequest.getVisibility().name()));
        }

        if (isLogin) {
            wrapper.and(T_MEMO.VISIBILITY.in(Lists.newArrayList(Visibility.PUBLIC.name(), Visibility.PROTECT.name()))
                    .or(T_MEMO.USER_ID.eq(StpUtil.getLoginIdAsInt()).and(T_MEMO.VISIBILITY.eq(Visibility.PRIVATE.name())))
            );
        } else {
            wrapper.and(T_MEMO.VISIBILITY.eq(Visibility.PUBLIC.name()));
        }
        wrapper.orderBy("created desc");
        return wrapList(listMemoRequest, wrapper);
    }

    public ListMemoResponse listArchived(ListMemoRequest listMemoRequest) {
        QueryWrapper wrapper = QueryWrapper.create()
                .and(T_MEMO.USER_ID.eq(StpUtil.getLoginIdAsInt()))
                .and(T_MEMO.STATUS.eq(MemoStatus.ARCHIVED.name()))
                .and(T_MEMO.TAGS.like(listMemoRequest.getTag() + ","));
        wrapper.orderBy("created desc");
        return wrapList(listMemoRequest, wrapper);
    }

    private ListMemoResponse wrapList(ListMemoRequest listMemoRequest, QueryWrapper wrapper) {
        Page<TMemo> page = memoMapper.paginate(listMemoRequest.getPage(), listMemoRequest.getSize(), wrapper);
        ListMemoResponse listMemoResponse = new ListMemoResponse();
        listMemoResponse.setTotal(page.getTotalRow());
        listMemoResponse.setTotalPage(page.getTotalPage());
        listMemoResponse.setItems(page.getRecords().stream().map(this::convertToDto).collect(Collectors.toList()));
        return listMemoResponse;
    }


    public MemoDto get(int id) {
        boolean isLogin = StpUtil.isLogin();
        QueryWrapper queryWrapper = QueryWrapper.create().and(T_MEMO.ID.eq(id));
        if (isLogin) {
            queryWrapper.and(T_MEMO.VISIBILITY.in(Lists.newArrayList(Visibility.PUBLIC.name(), Visibility.PROTECT.name()))
                    .or(T_MEMO.USER_ID.eq(StpUtil.getLoginIdAsInt()).and(T_MEMO.VISIBILITY.eq(Visibility.PRIVATE.name()))));
        } else {
            queryWrapper.and(T_MEMO.VISIBILITY.in(Lists.newArrayList(Visibility.PUBLIC.name())));
        }
        TMemo tMemo = memoMapper.selectOneByQuery(queryWrapper);
        return convertToDto(tMemo);
    }

    public MemoDto convertToDto(TMemo memo) {
        if (memo == null) {
            return null;
        }
        MemoDto tMemo = new MemoDto();
        BeanUtils.copyProperties(memo, tMemo);
        TUser user = userMapper.selectOneById(memo.getUserId());
        tMemo.setAuthorName(user.getDisplayName());
        tMemo.setAuthorRole(user.getRole());
        tMemo.setEmail(user.getEmail());
        tMemo.setBio(user.getBio());
        tMemo.setAuthorId(user.getId());
        String domain = sysConfigService.getString(SysConfigConstant.DOMAIN);
        List<TResource> resources = resourceMapper.selectListByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(memo.getId())));
        tMemo.setResources(resources.stream().map(r -> {
            MemoDto.ResourceItem item = new MemoDto.ResourceItem();
            if (Objects.equals(r.getStorageType(), StorageType.LOCAL.name())) {
                item.setUrl(domain + "/api" + r.getExternalLink());
            } else {
                item.setUrl(r.getExternalLink());
            }
            item.setPublicId(r.getPublicId());
            item.setFileType(r.getFileType());
            return item;
        }).toList());
        return tMemo;
    }


    public StatisticsResponse statistics(StatisticsRequest request) {
        ZoneId zoneId = ZoneId.systemDefault();

        StatisticsResponse statisticsResponse = new StatisticsResponse();
        YearMonth myYearMonth = YearMonth.now();

        if (request.getBegin() == null) {
            LocalDate firstDay = myYearMonth.atDay(1);
            ZonedDateTime zdt = firstDay.atStartOfDay(zoneId);
            request.setBegin(Date.from(zdt.toInstant()));
        }
        if (request.getEnd() == null) {
            LocalDate firstDay = myYearMonth.plusMonths(1).atDay(1);
            ZonedDateTime lastDayZdt = firstDay.atStartOfDay(zoneId);
            request.setEnd(Date.from(lastDayZdt.toInstant()));
        }


        int loginId = StpUtil.getLoginIdAsInt();
        TUser user = userMapper.selectOneById(loginId);
        long totalMemos = memoMapper.selectCountByQuery(QueryWrapper.create().and(T_MEMO.USER_ID.eq(loginId)));
        long totalDays = Duration.between(user.getCreated().toLocalDateTime(), LocalDateTime.now()).toDays();
        long totalTags = tagMapper.selectCountByQuery(QueryWrapper.create().and(T_TAG.USER_ID.eq(loginId)));

        statisticsResponse.setTotalMemos(totalMemos);
        statisticsResponse.setTotalTags(totalTags);
        statisticsResponse.setTotalDays(totalDays);

        List<Row> rows = Db.selectListBySql("select date(created) as day,count(1) as count from t_memo where " +
                        "user_id = ? and created between ? and ? group by date(created) order by date(created) desc",
                loginId, request.getBegin(), request.getEnd());
        statisticsResponse.setItems(rows.stream().map(r -> {
            StatisticsResponse.Item item = new StatisticsResponse.Item();
            item.setDate(r.getString("day"));
            item.setTotal(r.getLong("count"));
            return item;
        }).collect(Collectors.toList()));
        return statisticsResponse;
    }
}
