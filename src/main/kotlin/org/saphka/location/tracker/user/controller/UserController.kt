package org.saphka.location.tracker.user.controller

import org.saphka.location.tracker.user.api.UsersApi
import org.saphka.location.tracker.user.api.model.*
import org.saphka.location.tracker.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class UserController(private val userService: UserService) : UsersApi {
    override fun usersAuthPost(
        userAuthDTO: Mono<UserAuthDTO>?,
        exchange: ServerWebExchange?
    ): Mono<ResponseEntity<TokenResponseDTO>> {
        return userAuthDTO!!
            .flatMap { userService.authUser(it) }
            .map { TokenResponseDTO().token(it.accessToken) }
            .map { ResponseEntity.accepted().body(it) }
    }

    override fun usersMeGet(exchange: ServerWebExchange?): Mono<ResponseEntity<UserDTO>> {
        TODO("Not yet implemented")
    }

    override fun usersMePatch(
        inlineObjectDTO: Mono<InlineObjectDTO>?,
        exchange: ServerWebExchange?
    ): Mono<ResponseEntity<UserDTO>> {
        TODO("Not yet implemented")
    }

    override fun usersRegisterPost(
        userCreateDTO: Mono<UserCreateDTO>?,
        exchange: ServerWebExchange?
    ): Mono<ResponseEntity<UserDTO>> {
        TODO("Not yet implemented")
    }
}