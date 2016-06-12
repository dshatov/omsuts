package omsu.omsuts.bot.java.application;

import dagger.Module;
import dagger.Provides;
import okhttp3.ws.WebSocketListener;
import omsu.omsuts.bot.java.api.OmsutsWebSocketConnection;

import javax.inject.Singleton;

/**
 * Created by sds on 6/12/16.
 */
@Module
public class ApplicationModule {

    private Application application;
    private WebSocketListener socketListener;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public WebSocketListener provideWebSocketListener() {
        if(socketListener == null) {
            socketListener = new OmsutsWebSocketConnection();
        }
        return socketListener;
    }

}
