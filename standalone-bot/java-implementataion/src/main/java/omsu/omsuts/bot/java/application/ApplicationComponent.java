package omsu.omsuts.bot.java.application;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Created by sds on 6/12/16.
 */
@Singleton
@Component(modules = {
        ApplicationModule.class
})
public interface ApplicationComponent {
    void inject(Application application);
}

