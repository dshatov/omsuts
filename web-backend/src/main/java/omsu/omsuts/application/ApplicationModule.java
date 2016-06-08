package omsu.omsuts.application;

import dagger.Module;
import dagger.Provides;
import freemarker.template.Configuration;
import freemarker.template.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.template.freemarker.FreeMarkerEngine;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/**
 * Created by sds on 08.06.16.
 */
@Module
public class ApplicationModule {
    private Application application;
    private FreeMarkerEngine freeMarkerEngine;

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
    public FreeMarkerEngine provideFreeMarkerEngine() {
        if (freeMarkerEngine == null) {
            Configuration freeMarkerConfiguration = new Configuration(new Version(Application.FREEMARKER_VERSION));

            //release directory
            try {
                freeMarkerConfiguration.setDirectoryForTemplateLoading(
                        new File(Application.RESOURCES_DIR + Application.FREEMARKER_TEMPLATES_DIR)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Debug directory
//        freeMarkerConfiguration.setTemplateLoader(
//                new ClassTemplateLoader(Main.class, Application.FREEMARKER_TEMPLATES_DIR)
//        );

            freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);
        }
        return freeMarkerEngine;
    }

    @Provides
    Logger provideLogger() {
        return LoggerFactory.getLogger(Application.class);
    }
}
