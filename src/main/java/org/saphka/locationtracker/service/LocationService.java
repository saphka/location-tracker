package org.saphka.locationtracker.service;

import org.saphka.locationtracker.api.model.LocationDTO;
import reactor.core.publisher.Flux;

public interface LocationService {

    Flux<LocationDTO> getLocations(Integer userId);

}
