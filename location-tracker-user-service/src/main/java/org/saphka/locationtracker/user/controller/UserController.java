package org.saphka.locationtracker.user.controller;

import org.saphka.locationtracker.user.api.UsersApi;
import org.saphka.locationtracker.user.api.model.*;
import org.saphka.locationtracker.user.domain.UserService;
import org.saphka.locationtracker.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController implements UsersApi {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<TokenResponseDTO> usersAuthPost(@Valid UserAuthDTO userAuthDTO) {
        return ResponseEntity.accepted().body(new TokenResponseDTO().token(""));
    }

    @Override
    public ResponseEntity<UserDTO> usersMeGet() {
        return null;
    }

    @Override
    public ResponseEntity<UserDTO> usersMePatch(@Valid InlineObjectDTO inlineObjectDTO) {
        return null;
    }

    @Override
    public ResponseEntity<UserDTO> usersRegisterPost(@Valid UserCreateDTO userCreateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userMapper.toUserDto(userService.createUser(userMapper.toUserCreateData(userCreateDTO)))
        );
    }
}
