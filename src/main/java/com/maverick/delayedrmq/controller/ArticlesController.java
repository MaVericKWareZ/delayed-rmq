package com.maverick.delayedrmq.controller;

import com.maverick.delayedrmq.config.Constants;
import com.maverick.delayedrmq.dto.ArticleDTO;
import com.maverick.delayedrmq.service.ArticleService;
import com.maverick.delayedrmq.service.UserViewsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@AllArgsConstructor
@Slf4j
public class ArticlesController {

    private ArticleService articleService;

    @GetMapping("/article/{articleId}")
    public ResponseEntity<ArticleDTO> getArticleById(@RequestHeader(Constants.USER_ID_HEADER) Integer userId, @PathVariable Integer articleId) {
        log.info("Getting articles by articleId={}, userId={}", articleId, userId);
        ArticleDTO articleDTO = articleService.getArticle(articleId,userId);
        return ResponseEntity.ok().body(articleDTO);
    }

    @PostMapping("/article/{articleId}")
    public ResponseEntity<ArticleDTO> saveArticleById(@PathVariable Integer articleId) {
        log.info("Saving article with articleId={}", articleId);
        articleService.saveArticle(ArticleDTO.builder().articleId(articleId).build());
        return ResponseEntity.ok().build();

    }
}
