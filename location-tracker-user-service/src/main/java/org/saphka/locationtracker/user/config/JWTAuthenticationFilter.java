package org.saphka.locationtracker.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.saphka.locationtracker.user.api.model.TokenResponseDTO;
import org.saphka.locationtracker.user.api.model.UserAuthDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final String jwtSecret;
    private final String jwtIssuer;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, String authPath, String jwtSecret, String jwtIssuer) {
        this.authenticationManager = authenticationManager;
        this.jwtSecret = jwtSecret;
        this.jwtIssuer = jwtIssuer;

        setFilterProcessesUrl(authPath);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            UserAuthDTO creds = new ObjectMapper()
                    .readValue(req.getInputStream(), UserAuthDTO.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getAlias(),
                            creds.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException {
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(((User) auth.getPrincipal()).getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(Duration.ofDays(365))))
                .setIssuer(jwtIssuer)
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret)))
                .compact();

        TokenResponseDTO body = new TokenResponseDTO().token(token);

        res.getWriter().write(new ObjectMapper().writeValueAsString(body));
        res.setContentType("application/json");
        res.getWriter().flush();
    }
}