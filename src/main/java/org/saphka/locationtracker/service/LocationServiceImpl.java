package org.saphka.locationtracker.service;

import org.saphka.locationtracker.api.model.LocationDTO;
import org.saphka.locationtracker.dao.LocationsRepository;
import org.saphka.locationtracker.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationsRepository repository;
    private final LocationMapper mapper;

    @Autowired
    public LocationServiceImpl(LocationsRepository repository, LocationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<LocationDTO> getLocations(Integer userId) {
        return repository.getLocationsByReceiver(userId).map(mapper::sourceToTarget);
    }
}
