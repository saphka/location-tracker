package org.saphka.locationtracker.dao.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

public interface AsyncHelper {

    <T> Flux<T> fromArray(Callable<T[]> callable);

    <T> Mono<T> from(Callable<T> callable);

}
