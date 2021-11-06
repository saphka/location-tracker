package org.saphka.location.tracker.user.service

import org.saphka.location.tracker.user.api.model.UserAuthDTO
import org.saphka.location.tracker.user.api.model.UserCreateDTO
import org.saphka.location.tracker.user.api.model.UserPatchDTO
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.model.User
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {

    fun getUserById(id: Int): Mono<User>

    fun authUser(authRequest: UserAuthDTO): Mono<String>

    fun createUser(createRequest: UserCreateDTO): Mono<User>

    fun updateUser(id: Int, updateRequest: UserPatchDTO): Mono<User>
}

@Service
class UserServiceImpl(
    private val userDAO: UserDAO,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) : UserService {
    override fun getUserById(id: Int): Mono<User> {
        return userDAO.findUser(id)
    }

    override fun authUser(authRequest: UserAuthDTO): Mono<String> {
        return userDAO.findUser(authRequest.alias)
            .filter { passwordEncoder.matches(authRequest.password, it.passwordHash) }
            .switchIfEmpty(Mono.error { BadCredentialsException("Username/password does not match") })
            .map { jwtService.createToken(it) }
    }

    override fun createUser(createRequest: UserCreateDTO): Mono<User> {
        return userDAO.createUser(
            createRequest.alias,
            createRequest.publicKey,
            passwordEncoder.encode(createRequest.password)
        )
    }

    override fun updateUser(id: Int, updateRequest: UserPatchDTO): Mono<User> {
        return userDAO.findUser(id)
            .map {
                User(
                    it.id,
                    updateRequest.alias,
                    updateRequest.publicKey,
                    it.passwordHash
                )
            }
            .flatMap { userDAO.updateUser(it) }
    }

}