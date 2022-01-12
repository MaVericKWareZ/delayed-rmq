package com.maverick.delayedrmq.dto;


import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class ArticleDTO {

    Integer articleId;

    String heading;

    String imageUrl;

    Long viewCount;

    Date viewsLastUpdatedAt;
}
