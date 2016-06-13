package omsu.omsuts.application.service;

import rx.Subscription;
import rx.internal.util.SubscriptionList;

/**
 * Created by sds on 6/13/16.
 */
public abstract class BackgroundService implements Runnable {
    private SubscriptionList subscriptions = new SubscriptionList();

    public void addSubscription(Subscription s) {
        subscriptions.add(s);
    }

    public void stop() {
        subscriptions.unsubscribe();
    }
}
