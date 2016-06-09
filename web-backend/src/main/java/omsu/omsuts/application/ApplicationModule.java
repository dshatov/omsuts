package omsu.omsuts.application;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import dagger.Module;
import dagger.Provides;
import freemarker.template.Configuration;
import freemarker.template.Version;
import lombok.val;
import spark.template.freemarker.FreeMarkerEngine;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by sds on 08.06.16.
 */
@Module
public class ApplicationModule {
    private Application application;
    private FreeMarkerEngine freeMarkerEngine;
    private ConnectionSource dbConnectionSource;

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
    public ConnectionSource provideDbConnectionSource() {
        if (dbConnectionSource == null) {
            try {
                dbConnectionSource = new JdbcPooledConnectionSource(("jdbc:h2:mem:user"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dbConnectionSource;
    }

    @Provides
    @Singleton
    public FreeMarkerEngine provideFreeMarkerEngine() {
        if (freeMarkerEngine == null) {
            val freeMarkerConfiguration = new Configuration(new Version(Application.FREEMARKER_VERSION));

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
}
