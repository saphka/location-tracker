package org.saphka.locationtracker.user.domain;

import lombok.Data;

import java.util.function.Function;

@Data
public class User {

    private final Integer id;

    private String alias;

    private String publicKey;

    private String password;

    private final Function<String, String> passwordEncoder;

    public void setNewPassword(String password) {
        this.password = passwordEncoder.apply(password);
    }

}
