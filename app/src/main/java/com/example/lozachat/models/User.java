package com.example.lozachat.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;
    public Boolean is_friend;

    public User() {
    }

    public User(User user) {
        this.name = user.name;
        this.image = user.image;
        this.email = user.email;
        this.token = user.token;
        this.id = user.id;
        this.is_friend = user.is_friend;
    }
}
