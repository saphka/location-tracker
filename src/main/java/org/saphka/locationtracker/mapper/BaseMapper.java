package org.saphka.locationtracker.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<S, T> {

    T sourceToTarget(S source);

    S targetToSource(T target);

    S copySource(S source, @MappingTarget S target);

    T copyTarget(T source, @MappingTarget T target);
}
