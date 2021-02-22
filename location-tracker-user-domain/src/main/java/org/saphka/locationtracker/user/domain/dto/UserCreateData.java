package org.saphka.locationtracker.user.domain.dto;

import lombok.Data;

@Data
public class UserCreateData {

    private String alias;

    private String publicKey;

    private String password;

}
