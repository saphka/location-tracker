package org.saphka.location.tracker.user.controller

import org.saphka.location.tracker.user.api.UsersApi
import org.saphka.location.tracker.user.api.model.*
import org.saphka.location.tracker.user.model.User
import org.saphka.location.tracker.user.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
            .map { TokenResponseDTO().token(it) }
            .map { ResponseEntity.accepted().body(it) }
    }

    override fun usersMeGet(exchange: ServerWebExchange?): Mono<ResponseEntity<UserDTO>> {
        return exchange!!.getPrincipal<UsernamePasswordAuthenticationToken>()
            .flatMap { userService.getUserById(it.principal as Int) }
            .map { mapToUserDTO(it) }
            .map { ResponseEntity.ok(it) }
    }

    override fun usersMePatch(
        userPatchDTO: Mono<UserPatchDTO>?,
        exchange: ServerWebExchange?
    ): Mono<ResponseEntity<UserDTO>> {
        return Mono.zip(
            exchange!!.getPrincipal<UsernamePasswordAuthenticationToken>(), userPatchDTO!!
        )
            .flatMap { userService.updateUser(it.t1.principal as Int, it.t2) }
            .map { mapToUserDTO(it) }
            .map { ResponseEntity.accepted().body(it) }
    }

    override fun usersRegisterPost(
        userCreateDTO: Mono<UserCreateDTO>?,
        exchange: ServerWebExchange?
    ): Mono<ResponseEntity<UserDTO>> {
        return userCreateDTO!!
            .flatMap { userService.createUser(it) }
            .map {
                mapToUserDTO(it)
            }
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    private fun mapToUserDTO(it: User) = UserDTO()
        .id(it.id)
        .alias(it.alias)
        .publicKey(it.publicKey)
}