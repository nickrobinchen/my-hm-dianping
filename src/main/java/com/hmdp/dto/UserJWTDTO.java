package com.hmdp.dto;

import lombok.Data;

@Data
public class UserJWTDTO {
    private Long id;
    private String nickName;
    private String icon;
    private String expireTime;
}
