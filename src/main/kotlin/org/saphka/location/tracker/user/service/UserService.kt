package org.saphka.location.tracker.user.service

import org.saphka.location.tracker.user.api.model.UserAuthDTO
import org.saphka.location.tracker.user.api.model.UserCreateDTO
import org.saphka.location.tracker.user.api.model.UserPatchDTO
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.model.Token
import org.saphka.location.tracker.user.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

interface UserService {

    fun getUserByAlias(alias: String): Mono<User>

    fun authUser(authRequest: UserAuthDTO): Mono<Token>

    fun createUser(createRequest: UserCreateDTO): Mono<User>

    fun updateUser(alias: String, updateRequest: UserPatchDTO): Mono<User>
}

@Service
class UserServiceImpl(
    private val userDAO: UserDAO,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) : UserService {
    override fun getUserByAlias(alias: String): Mono<User> {
        return userDAO.findUser(alias)
    }

    override fun authUser(authRequest: UserAuthDTO): Mono<Token> {
        return userDAO.findUser(authRequest.alias)
            .map { jwtService.createToken(it) }
            .map { Token(it) }
    }

    override fun createUser(createRequest: UserCreateDTO): Mono<User> {
        return userDAO.createUser(
            createRequest.alias,
            Base64.getDecoder().decode(createRequest.publicKey),
            passwordEncoder.encode(createRequest.password)
        )
    }

    override fun updateUser(alias: String, updateRequest: UserPatchDTO): Mono<User> {
        TODO("Not yet implemented")
    }

}