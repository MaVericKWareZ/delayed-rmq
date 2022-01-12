package com.maverick.delayedrmq.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class UserDTO {

    Integer userId;

    String userName;

    String externalUserId;

}
