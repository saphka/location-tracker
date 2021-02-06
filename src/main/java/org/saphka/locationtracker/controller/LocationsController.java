package org.saphka.locationtracker.controller;

import org.saphka.locationtracker.api.LocationsApi;
import org.saphka.locationtracker.api.model.LocationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.OffsetDateTime;

@Controller
public class LocationsController implements LocationsApi {

    @Override
    public Mono<ResponseEntity<Flux<LocationDTO>>> locationsGet(@Valid Integer senderId, @Valid OffsetDateTime timestamp, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<LocationDTO>>> locationsPost(@Valid Flux<LocationDTO> locationDTO, ServerWebExchange exchange) {
        return null;
    }
}
