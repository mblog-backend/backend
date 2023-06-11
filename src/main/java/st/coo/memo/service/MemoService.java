package st.coo.memo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import st.coo.memo.common.*;
import st.coo.memo.dto.memo.*;
import st.coo.memo.dto.resource.ResourceDto;
import st.coo.memo.entity.*;
import st.coo.memo.mapper.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private UserMemoRelationMapperExt userMemoRelationMapperExt;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private CommentMapperExt commentMapperExt;

    @Value("${DB_TYPE:}")
    private String dbType;

    @Value("${official.square.url}")
    private String officialSquareUrl;


    @Resource
    private HttpClient httpClient;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Transactional
    public void remove(int id) {
        TMemo tMemo = memoMapper.selectOneById(id);
        if (tMemo == null) {
            return;
        }
        if (!StpUtil.getStpLogic().hasRole("ADMIN") && !Objects.equals(tMemo.getUserId(), StpUtil.getLoginIdAsInt())) {
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
        commentMapperExt.deleteByQuery(QueryWrapper.create().and(T_COMMENT.MEMO_ID.eq(id)));

        TUser user = userMapper.selectOneById(StpUtil.getLoginIdAsInt());

        threadPoolTaskExecutor.execute(() -> {
            pushOfficialSquare(tMemo,user,MemoType.REMOVE);
        });
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

    public Integer save(SaveMemoRequest saveMemoRequest) {
        checkContentAndResource(saveMemoRequest.getContent(), saveMemoRequest.getPublicIds());
        List<String> tags = parseTags(saveMemoRequest.getContent());
        String content = saveMemoRequest.getContent();
        TMemo tMemo = new TMemo();
        tMemo.setUserId(StpUtil.getLoginIdAsInt());
        tMemo.setTags(Joiner.on(",").join(tags) + (tags.size() > 0 ? "," : ""));
        if (saveMemoRequest.getVisibility() != null) {
            tMemo.setVisibility(saveMemoRequest.getVisibility().name());
        }
        tMemo.setEnableComment(saveMemoRequest.isEnableComment() ? 1 : 0);
        tMemo.setContent(replaceFirstLine(content, tags).trim());
        tMemo.setCreated(new Timestamp(System.currentTimeMillis()));
        tMemo.setUpdated(new Timestamp(System.currentTimeMillis()));
        tMemo.setSource(saveMemoRequest.getSource());
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
        TUser user = userMapper.selectOneById(StpUtil.getLoginIdAsInt());

        threadPoolTaskExecutor.execute(() -> {
            pushOfficialSquare(tMemo, user, MemoType.SAVE);
            notifyWebhook(tMemo);
        });

        return tMemo.getId();
    }


    private void pushOfficialSquare(TMemo memo, TUser user, MemoType memoType) {
        boolean pushOfficialSquare = sysConfigService.getBoolean(SysConfigConstant.PUSH_OFFICIAL_SQUARE);
        if (pushOfficialSquare && Objects.equals(memo.getVisibility(), Visibility.PUBLIC.name())) {
            String url = officialSquareUrl;
            String token = sysConfigService.getString(SysConfigConstant.WEB_HOOK_TOKEN);
            if (Objects.equals(memoType, MemoType.REMOVE)) {
                url = url + "/api/memo/remove";
            } else {
                url = url + "/api/memo/push";
            }
            HttpPost request = new HttpPost(url);

            if (StringUtils.hasText(token)) {
                request.setHeader("token", token);
            }

            request.setHeader("content-type", "application/json;charset=utf8");

            String backendUrl = sysConfigService.getString(SysConfigConstant.DOMAIN);

            List<TResource> list = resourceMapper.selectListByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(memo.getId())));
            Gson gson = new Gson();
            String domain = sysConfigService.getString(SysConfigConstant.DOMAIN);

            Map<String, Object> map = Maps.newHashMap();
            map.put("content", memo.getContent());
            map.put("tags", memo.getTags());
            map.put("publishTime", memo.getCreated());
            map.put("author", user.getDisplayName());
            map.put("website", backendUrl);
            map.put("memoId", memo.getId());
            map.put("avatarUrl", user.getAvatarUrl());
            map.put("userId", user.getId());
            map.put("resources", list.stream().map(r -> convertToResourceDto(domain, r)).toList());
            String body = gson.toJson(map);
            log.info("发送webhook到{},body:{}", url, body);
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                request.setEntity(new StringEntity(body));
                HttpResponse httpResponse = httpClient.execute(request);
                String response = EntityUtils.toString(httpResponse.getEntity());
                log.info("发送webhook成功,返回码:{},body:{},耗时:{}ms", httpResponse.getStatusLine().getStatusCode(),response, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } catch (IOException e) {
                log.error("发送webhook异常", e);
            }
        }
    }


    public void notifyWebhook(TMemo memo) {
        String url = sysConfigService.getString(SysConfigConstant.WEB_HOOK_URL);
        String token = sysConfigService.getString(SysConfigConstant.WEB_HOOK_TOKEN);

        if (Objects.equals(memo.getVisibility(), Visibility.PUBLIC.name()) && StringUtils.hasText(url)) {
            HttpPost request = new HttpPost(url);

            if (StringUtils.hasText(token)) {
                request.setHeader("token", token);
            }

            request.setHeader("content-type", "application/json;charset=utf8");

            TUser user = userMapper.selectOneById(memo.getUserId());
            String backendUrl = sysConfigService.getString(SysConfigConstant.DOMAIN);

            List<TResource> list = resourceMapper.selectListByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(memo.getId())));
            List<String> resources = list.stream().map(ele -> "%s/api/resource/%s".formatted(backendUrl, ele.getPublicId())).toList();
            Gson gson = new Gson();

            Map<String, Object> map = Maps.newHashMap();
            map.put("content", memo.getContent());
            map.put("tags", memo.getTags());
            map.put("created", memo.getCreated());
            map.put("authorName", user.getDisplayName());
            map.put("resources", resources);
            String body = gson.toJson(map);
            log.info("发送webhook到{},body:{}", url, body);
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                request.setEntity(new StringEntity(body));
                HttpResponse httpResponse = httpClient.execute(request);
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                log.info("发送webhook成功,返回码:{},耗时:{}ms", httpResponse.getStatusLine().getStatusCode(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } catch (IOException e) {
                log.error("发送webhook异常", e);
            }
        }
    }


    public Integer update(SaveMemoRequest updateMemoRequest) {
        checkContentAndResource(updateMemoRequest.getContent(), updateMemoRequest.getPublicIds());
        TMemo existMemo = memoMapper.selectOneById(updateMemoRequest.getId());
        if (existMemo == null) {
            throw new BizException(ResponseCode.fail, "memo不存在");
        }
        String oldTags = existMemo.getTags();
        String content = updateMemoRequest.getContent();
        TMemo tMemo = new TMemo();
        tMemo.setId(existMemo.getId());
        List<String> tags = parseTags(updateMemoRequest.getContent());
        tMemo.setTags(Joiner.on(",").join(tags) + (tags.size() > 0 ? "," : ""));
        tMemo.setContent(replaceFirstLine(content, tags).trim());
        tMemo.setEnableComment(updateMemoRequest.isEnableComment() ? 1 : 0);
        tMemo.setUpdated(new Timestamp(System.currentTimeMillis()));
        if (updateMemoRequest.getVisibility() != null) {
            tMemo.setVisibility(updateMemoRequest.getVisibility().name());
        }
        tMemo.setCreated(existMemo.getCreated());
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
        TUser user = userMapper.selectOneById(StpUtil.getLoginIdAsInt());

        threadPoolTaskExecutor.execute(() -> {
            pushOfficialSquare(tMemo, user, MemoType.SAVE);
        });

        return existMemo.getId();
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
        listMemoRequest.setDbType(dbType);
        long total = memoMapper.countMemos(listMemoRequest);
        List<MemoDto> list = Lists.newArrayList();
        if (total > 0) {
            list = memoMapper.listMemos(listMemoRequest);
            for (MemoDto memo : list) {
                memo.setUnApprovedCommentCount(commentMapperExt.selectCountByQuery(
                        QueryWrapper.create()
                                .and(T_COMMENT.MEMO_ID.eq(memo.getId()))
                                .and(T_COMMENT.USER_ID.lt(0)
                                        .and(T_COMMENT.APPROVED.eq(0)))));
            }
        }
        ListMemoResponse response = new ListMemoResponse();
        response.setTotal(total);
        response.setItems(list);
        response.setTotalPage(total % listMemoRequest.getSize() == 0 ? total / listMemoRequest.getSize() : total / listMemoRequest.getSize() + 1);

        if (isLogin && listMemoRequest.isCommented() && listMemoRequest.isMentioned()) {
            TUser tUser = new TUser();
            tUser.setLastClickedMentioned(Timestamp.valueOf(LocalDateTime.now()));
            userMapper.updateByQuery(tUser, true, QueryWrapper.create().and(T_USER.ID.eq(StpUtil.getLoginIdAsInt())));
        }
        return response;
    }


    public MemoDto get(int id, boolean count) {
        boolean isLogin = StpUtil.isLogin();
        QueryWrapper queryWrapper = QueryWrapper.create().and(T_MEMO.ID.eq(id));
        if (isLogin) {
            queryWrapper.and(T_MEMO.VISIBILITY.in(Lists.newArrayList(Visibility.PUBLIC.name(), Visibility.PROTECT.name()))
                    .or(T_MEMO.USER_ID.eq(StpUtil.getLoginIdAsInt()).and(T_MEMO.VISIBILITY.eq(Visibility.PRIVATE.name()))));
        } else {
            queryWrapper.and(T_MEMO.VISIBILITY.in(Lists.newArrayList(Visibility.PUBLIC.name())));
        }
        TMemo tMemo = memoMapper.selectOneByQuery(queryWrapper);
        if (tMemo != null && count) {
            memoMapper.addViewCount(id);
        }
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
        tMemo.setUnApprovedCommentCount(commentMapperExt.selectCountByQuery(QueryWrapper.create().and(T_COMMENT.MEMO_ID.eq(memo.getId())).and(T_COMMENT.USER_ID.lt(0).and(T_COMMENT.APPROVED.eq(0)))));
        String domain = sysConfigService.getString(SysConfigConstant.DOMAIN);
        List<TResource> resources = resourceMapper.selectListByQuery(QueryWrapper.create().and(T_RESOURCE.MEMO_ID.eq(memo.getId())));
        tMemo.setResources(resources.stream().map(r -> convertToResourceDto(domain, r)).toList());
        return tMemo;
    }

    private static ResourceDto convertToResourceDto(String domain, TResource r) {
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

        int userId;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsInt();
        } else {
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

        List<Row> rows = Lists.newArrayList();
        if (Objects.equals(dbType, "-sqlite")) {
            rows = Db.selectListBySql("select date(created/1000,'unixepoch') as day,count(1) as count from t_memo where " +
                            "user_id = ? and created between ? and ? group by date(created/1000,'unixepoch') order by date(created/1000,'unixepoch') desc",
                    userId, request.getBegin(), request.getEnd());
        } else {
            rows = Db.selectListBySql("select date(created) as day,count(1) as count from t_memo where " +
                            "user_id = ? and created between ? and ? group by date(created) order by date(created) desc",
                    userId, request.getBegin(), request.getEnd());
        }
        statisticsResponse.setItems(rows.stream().map(r -> {
            StatisticsResponse.Item item = new StatisticsResponse.Item();
            item.setDate(r.getString("day"));
            item.setTotal(r.getLong("count"));
            return item;
        }).collect(Collectors.toList()));
        return statisticsResponse;
    }


    @Transactional
    public void makeRelation(MemoRelationRequest request) {
        boolean openLike = sysConfigService.getBoolean(SysConfigConstant.OPEN_LIKE);
        if (!openLike) {
            throw new BizException(ResponseCode.fail, "禁止点赞");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .and(T_USER_MEMO_RELATION.MEMO_ID.eq(request.getMemoId()))
                .and(T_USER_MEMO_RELATION.USER_ID.eq(StpUtil.getLoginIdAsInt()))
                .and(T_USER_MEMO_RELATION.FAV_TYPE.eq(request.getType()));

        if (Objects.equals(request.getOperateType(), "ADD")) {
            TUserMemoRelation relation = new TUserMemoRelation();
            relation.setMemoId(request.getMemoId());
            relation.setUserId(StpUtil.getLoginIdAsInt());
            relation.setFavType(request.getType());
            long count = userMemoRelationMapperExt.selectCountByQuery(queryWrapper);
            if (count > 0) {
                throw new BizException(ResponseCode.fail, "数据已存在");
            }
            Assert.isTrue(memoMapper.addLikeCount(request.getMemoId()) == 1, "更新like数量异常");
            relation.setCreated(new Timestamp(System.currentTimeMillis()));
            userMemoRelationMapperExt.insertSelective(relation);
        } else if (Objects.equals(request.getOperateType(), "REMOVE")) {
            Assert.isTrue(userMemoRelationMapperExt.deleteByQuery(queryWrapper) == 1, "删除like数据异常");
            Assert.isTrue(memoMapper.removeLikeCount(request.getMemoId()) == 1, "更新like数量异常");
        }
    }


}
