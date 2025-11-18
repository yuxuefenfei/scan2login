package com.wwz.login.qrauth.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private String id;

    private String username;

    private String phone;

    private String avatar;

    public User(String id, String username, String phone) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.avatar = "/images/" + id + ".jpg";
    }
}
