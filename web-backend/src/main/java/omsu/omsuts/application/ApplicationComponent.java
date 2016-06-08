package omsu.omsuts.application;

import dagger.Component;

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
}
