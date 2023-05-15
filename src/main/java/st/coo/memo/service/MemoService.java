package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import st.coo.memo.common.*;
import st.coo.memo.dto.memo.*;
import st.coo.memo.dto.resource.ResourceDto;
import st.coo.memo.entity.TMemo;
import st.coo.memo.entity.TResource;
import st.coo.memo.entity.TTag;
import st.coo.memo.entity.TUser;
import st.coo.memo.mapper.MemoMapperExt;
import st.coo.memo.mapper.ResourceMapperExt;
import st.coo.memo.mapper.TagMapperExt;
import st.coo.memo.mapper.UserMapperExt;

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
        if (!Objects.equals(tMemo.getUserId(), StpUtil.getLoginIdAsInt())) {
            throw new BizException(ResponseCode.fail, "不能删除其他人的记录");
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

    public void setMemoPriority(int id, boolean set) {
        TMemo tMemo = memoMapper.selectOneById(id);
        if (tMemo == null) {
            return;
        }
        if (!StpUtil.getRoleList().contains("ADMIN") && !Objects.equals(tMemo.getUserId(), StpUtil.getLoginIdAsInt())) {
            throw new BizException(ResponseCode.fail, "不能操作其他人的记录");
        }
        if (set) {
            memoMapper.setPriority(id);
        } else {
            memoMapper.unSetPriority(id);
        }

    }

    private String replaceFirstLine(String content, List<String> tags) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        List<String> lines = Lists.newArrayList(Splitter.on("\n").splitToList(content));
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

    private void checkContentAndResource(String content, List<String> resourceId) {
        if (!StringUtils.hasText(content) && CollectionUtils.isEmpty(resourceId)) {
            throw new BizException(ResponseCode.fail, "内容和图片都为空");
        }
    }

    public void save(SaveMemoRequest saveMemoRequest) {
        checkContentAndResource(saveMemoRequest.getContent(), saveMemoRequest.getPublicIds());
        List<String> tags = parseTags(saveMemoRequest.getContent());
        String content = saveMemoRequest.getContent();
        TMemo tMemo = new TMemo();
        tMemo.setUserId(StpUtil.getLoginIdAsInt());
        tMemo.setTags(Joiner.on(",").join(tags) + (tags.size() > 0 ? "," : ""));
        if (saveMemoRequest.getVisibility() != null) {
            tMemo.setVisibility(saveMemoRequest.getVisibility().name());
        }
        tMemo.setContent(replaceFirstLine(content, tags).trim());

        List<TTag> existsTagList = tags.size() == 0 ? Lists.newArrayList() : tagMapper.selectListByQuery(QueryWrapper.create().
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
        checkContentAndResource(updateMemoRequest.getContent(), updateMemoRequest.getPublicIds());
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
        tMemo.setPriority(updateMemoRequest.getPriority());
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
        if (!StringUtils.hasText(content)) {
            return Lists.newArrayList();
        }
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
        listMemoRequest.setLogin(isLogin);
        if (isLogin) {
            listMemoRequest.setCurrentUserId(StpUtil.getLoginIdAsInt());
        }
        log.debug(new Gson().toJson(listMemoRequest));
        long total = memoMapper.countMemos(listMemoRequest);
        List<MemoDto> list = Lists.newArrayList();
        if (total > 0) {
            list = memoMapper.listMemos(listMemoRequest);
        }
        ListMemoResponse response = new ListMemoResponse();
        response.setTotal(total);
        response.setItems(list);
        response.setTotalPage(total % listMemoRequest.getSize() == 0 ? total / listMemoRequest.getSize() : total / listMemoRequest.getSize() + 1);
        return response;
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
        String domain = sysConfigService.getString(SysConfigConstant.DOMAIN);
        List<TResource> resources = resourceMapper.selectListByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(memo.getId())));
        tMemo.setResources(resources.stream().map(r -> {
            ResourceDto item = new ResourceDto();
            if (Objects.equals(r.getStorageType(), StorageType.LOCAL.name())) {
                item.setUrl(domain + r.getExternalLink());
            } else {
                item.setUrl(r.getExternalLink());
            }
            item.setSuffix(r.getSuffix());
            item.setPublicId(r.getPublicId());
            item.setFileType(r.getFileType());
            return item;
        }).toList());
        return tMemo;
    }


    public StatisticsResponse statistics(StatisticsRequest request) {
        ZoneId zoneId = ZoneId.systemDefault();

        StatisticsResponse statisticsResponse = new StatisticsResponse();

        if (request.getBegin() == null) {
            LocalDate firstDay = LocalDate.now().plusDays(-50);
            ZonedDateTime zdt = firstDay.atStartOfDay(zoneId);
            request.setBegin(Date.from(zdt.toInstant()));
        }
        if (request.getEnd() == null) {
            LocalDate firstDay = LocalDate.now().plusDays(1);
            ZonedDateTime lastDayZdt = firstDay.atStartOfDay(zoneId);
            request.setEnd(Date.from(lastDayZdt.toInstant()));
        }

        int userId ;
        if (StpUtil.isLogin()){
            userId = StpUtil.getLoginIdAsInt();
        }else{
            TUser admin = userMapper.selectOneByQuery(QueryWrapper.create().and(T_USER.ROLE.eq("ADMIN")));
            userId = admin.getId();
        }
        TUser user = userMapper.selectOneById(userId);
        long totalMemos = memoMapper.selectCountByQuery(QueryWrapper.create().and(T_MEMO.USER_ID.eq(userId)));
        long totalDays = Duration.between(user.getCreated().toLocalDateTime(), LocalDateTime.now()).toDays();
        long totalTags = tagMapper.selectCountByQuery(QueryWrapper.create().and(T_TAG.USER_ID.eq(userId)));

        statisticsResponse.setTotalMemos(totalMemos);
        statisticsResponse.setTotalTags(totalTags);
        statisticsResponse.setTotalDays(totalDays);

        List<Row> rows = Db.selectListBySql("select date(created) as day,count(1) as count from t_memo where " +
                        "user_id = ? and created between ? and ? group by date(created) order by date(created) desc",
                userId, request.getBegin(), request.getEnd());
        statisticsResponse.setItems(rows.stream().map(r -> {
            StatisticsResponse.Item item = new StatisticsResponse.Item();
            item.setDate(r.getString("day"));
            item.setTotal(r.getLong("count"));
            return item;
        }).collect(Collectors.toList()));
        return statisticsResponse;
    }
}
