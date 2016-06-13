package omsu.omsuts.application.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Created by sds on 6/13/16.
 */

@Slf4j
public class RoundService extends BackgroundService {

    @Override
    public void run() {
        val timer = Observable.interval(2, 1, TimeUnit.SECONDS);
        addSubscription(timer.subscribe(
                v -> log.info("Received from timer: {}", v),
                e -> log.error("Timer error:", e),
                () -> log.info("Timer service completed")
        ));
    }
}
