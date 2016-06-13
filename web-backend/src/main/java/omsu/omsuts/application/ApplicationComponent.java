package omsu.omsuts.application;

import dagger.Component;
import omsu.omsuts.api.RouteHandler;
import omsu.omsuts.api.bots.MessageHandler;
import omsu.omsuts.application.service.round.RoundService;

import javax.inject.Singleton;

/**
 * Created by sds on 08.06.16.
 */

@Singleton
@Component(modules = {
        ApplicationModule.class
})
public interface ApplicationComponent {
    void inject(Application application);
    void inject(RouteHandler routeHandler);
    void inject(MessageHandler messageHandler);
    void inject(RoundService roundService);
}

