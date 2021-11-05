package org.saphka.location.tracker.user.service

import org.saphka.location.tracker.user.api.model.UserAuthDTO
import org.saphka.location.tracker.user.dao.UserDAO
import org.saphka.location.tracker.user.model.Token
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

interface UserService {

    fun authUser(authRequest: UserAuthDTO): Mono<Token>

}

@Service
class UserServiceImpl(private val userDAO: UserDAO, private val jwtService: JwtService) : UserService {
    override fun authUser(authRequest: UserAuthDTO): Mono<Token> {
        return userDAO.findUser(authRequest.alias)
            .map { jwtService.createToken(it) }
            .map { Token(it) }
    }

}