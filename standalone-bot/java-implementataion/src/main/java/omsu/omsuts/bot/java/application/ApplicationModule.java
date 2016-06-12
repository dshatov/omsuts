package omsu.omsuts.bot.java.application;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by sds on 6/12/16.
 */
@Module
public class ApplicationModule {

    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

}
