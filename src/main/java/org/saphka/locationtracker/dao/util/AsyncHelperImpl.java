package org.saphka.locationtracker.dao.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

@Component
public class AsyncHelperImpl implements AsyncHelper {

    @Qualifier("jdbcScheduler")
    private final Scheduler scheduler;


    public AsyncHelperImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public <T> Flux<T> fromArray(Callable<T[]> callable) {
        return Mono.fromCallable(callable)
                .flatMapMany(Flux::fromArray)
                .subscribeOn(scheduler)
                .publishOn(Schedulers.parallel());
    }

    @Override
    public <T> Mono<T> from(Callable<T> callable) {
        return Mono.fromCallable(callable)
                .subscribeOn(scheduler)
                .publishOn(Schedulers.parallel());
    }
}
