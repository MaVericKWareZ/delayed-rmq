package com.maverick.delayedrmq.service;


import com.maverick.delayedrmq.config.QueueGateway;
import com.maverick.delayedrmq.config.QueueNames;
import com.maverick.delayedrmq.config.RedisUtil;
import com.maverick.delayedrmq.dto.ArticleDTO;
import com.maverick.delayedrmq.dto.UserViewsDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Slf4j
@Service
public class UserViewsService {

    private QueueGateway queueGateway;
    private RedisUtil<Long> redisUtil;
    private ArticleService articleService;

    private Map<Integer, Integer> viewsMap = new HashMap<>();
    private static final String VIEW_COUNT_KEY = "VIEW_COUNT";


    public void processUserViews(UserViewsDTO userViewsDTO) {
        Integer userId = userViewsDTO.getUserId();
        Integer articleId = userViewsDTO.getArticleId();
        Boolean hasViewedArticle = viewExistsByArticleIdAndUserId(articleId, userId);
        if (!hasViewedArticle) {
            saveUserView(articleId, userId);
            log.info("ArticleUserViewsServiceImpl.processUserViews() :: saved view for userId={} , articleId={}", userId, articleId);
            pushMessageForViewCountUpdate(articleId);
        } else {
            log.info("ArticleUserViewsServiceImpl.processUserViews() :: view exists for userId={} and articleId={} skipping message", userId, articleId);
        }
    }

    private void pushMessageForViewCountUpdate(Integer articleId) {
        String articleIdRedisKey = String.valueOf(articleId);
        Long currentViews = redisUtil.getMapAsSingleEntry(articleIdRedisKey, VIEW_COUNT_KEY);
        if (Objects.nonNull(currentViews) && currentViews > 0) {
            Long incrementedViews = currentViews + 1;
            redisUtil.putMap(articleIdRedisKey, VIEW_COUNT_KEY, incrementedViews);
        } else {
            redisUtil.putMap(articleIdRedisKey, VIEW_COUNT_KEY, 1L);
            queueGateway.pushEventToQueue(articleIdRedisKey, QueueNames.USER_VIEWS_UPDATE_BACKOFF_QUEUE);
            log.info("ArticleUserViewsServiceImpl.pushMessageForViewCountUpdate() :: pushed message for viewCount update for articleIdRedisKey={}", articleIdRedisKey);
        }
    }

    private void saveUserView(Integer articleId, Integer userId) {
        viewsMap.put(userId, articleId);
    }

    private Boolean viewExistsByArticleIdAndUserId(Integer articleId, Integer userId) {
        if (viewsMap.containsKey(userId) && articleId.equals(viewsMap.get(userId))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public void processViewsUpdate(String articleIdRedisKey) {
        Map<Object, Long> redisViewsMap = redisUtil.getMapAsAll(articleIdRedisKey);
        Long viewCountIncrement = redisViewsMap.get(VIEW_COUNT_KEY);
        if (viewCountIncrement > 0) {
            updateViewsOnArticle(articleIdRedisKey, viewCountIncrement);
        } else {
            log.error("UserViewsService.processViewsUpdate() :: current views to update is not valid for articleIdRedisKey = {}, viewCountIncrement = {}", articleIdRedisKey, viewCountIncrement);
        }
    }

    private void updateViewsOnArticle(String articleIdRedisKey, Long viewCountIncrement) {
        ArticleDTO articleDTO = articleService.getArticleByArticleId(Integer.valueOf(articleIdRedisKey));
        Long currentViews = articleDTO.getViewCount();
        Date viewsLastUpdatedAt = articleDTO.getViewsLastUpdatedAt();
        Long updatedViewCount = currentViews + viewCountIncrement;
        Date currTime = Calendar.getInstance().getTime();
        if (currTime.after(viewsLastUpdatedAt)) {
            articleService.saveOrUpdate(prepareArticleWithUpdatedViews(articleDTO, updatedViewCount, currTime));
            redisUtil.putMap(articleIdRedisKey, VIEW_COUNT_KEY, 0L);
            log.info("UserViewsServiceImpl.processViewUpdate() :: updated views for articleId={} by views={}", articleIdRedisKey, viewCountIncrement);
        }
    }

    private ArticleDTO prepareArticleWithUpdatedViews(ArticleDTO articleDTO, Long updatedViewCount, Date currTime) {
        articleDTO.setViewCount(updatedViewCount);
        articleDTO.setViewsLastUpdatedAt(currTime);
        return articleDTO;
    }
}
