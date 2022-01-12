package com.maverick.delayedrmq.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserViewsDTO {

    Integer userViewId;

    Integer userId;

    Integer articleId;

}
