package com.maverick.delayedrmq.service;

import com.maverick.delayedrmq.config.QueueGateway;
import com.maverick.delayedrmq.config.QueueNames;
import com.maverick.delayedrmq.dto.ArticleDTO;
import com.maverick.delayedrmq.dto.UserViewsDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ArticleService {

    private static List<ArticleDTO> articles = new ArrayList<>();
    private QueueGateway queueGateway;

    public ArticleDTO getArticle(Integer articleId, Integer userId) {
        queueGateway.pushEventToQueue(prepareUserViewsDTO(articleId, userId), QueueNames.USER_VIEWS_BACKOFF_QUEUE);
        log.info("Pushed event to queue for articleId={},userId={}", articleId, userId);
        return getArticleByArticleId(articleId);
    }

    public ArticleDTO getArticleByArticleId(Integer articleId) {
        return articles.stream().filter(articleDTO -> articleId.equals(articleDTO.getArticleId())).findAny().orElse(null);
    }

    private UserViewsDTO prepareUserViewsDTO(Integer articleId, Integer userId) {
        return UserViewsDTO.builder().userId(userId).articleId(articleId).build();
    }

    public void saveArticle(ArticleDTO articleDTO) {
        articleDTO.setHeading("TEST_HEADING");
        articleDTO.setImageUrl("TEST_URL");
        articleDTO.setViewCount(5L);
        articleDTO.setViewsLastUpdatedAt(new Date());
        articles.add(articleDTO);
    }

    public void saveOrUpdate(ArticleDTO articleDTO) {
        if (articles.stream().anyMatch(a -> articleDTO.getArticleId().equals(a.getArticleId()))) {
            List<ArticleDTO> updatedArticles = articles.stream().filter(a -> !a.getArticleId().equals(articleDTO.getArticleId())).collect(Collectors.toList());
            updatedArticles.add(articleDTO);
            articles = updatedArticles;
        } else {
            saveArticle(articleDTO);
        }
    }
}
