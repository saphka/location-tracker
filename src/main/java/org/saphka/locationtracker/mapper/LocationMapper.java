package org.saphka.locationtracker.mapper;

import org.mapstruct.Mapper;
import org.saphka.locationtracker.api.model.LocationDTO;
import org.saphka.locationtracker.dao.jooq.tables.records.LocationsRecord;

@Mapper
public interface LocationMapper extends BaseMapper<LocationsRecord, LocationDTO>{

}
