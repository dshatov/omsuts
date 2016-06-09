package omsu.omsuts.application;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import dagger.Module;
import dagger.Provides;
import freemarker.template.Configuration;
import freemarker.template.Version;
import lombok.val;
import spark.TemplateEngine;
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
    public static final String FREEMARKER_VERSION = "2.3.24";

    private Application application;
    private TemplateEngine templateEngine;
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
    public TemplateEngine provideTemplateEngine() {
        if (templateEngine == null) {
            val freeMarkerConfiguration = new Configuration(new Version(FREEMARKER_VERSION));

            //release directory
            try {
                freeMarkerConfiguration.setDirectoryForTemplateLoading(
                        new File(Application.RESOURCES_DIR + Application.TEMPLATES_DIR)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Debug directory
//        freeMarkerConfiguration.setTemplateLoader(
//                new ClassTemplateLoader(Main.class, Application.TEMPLATES_DIR)
//        );

            templateEngine = new FreeMarkerEngine(freeMarkerConfiguration);
        }
        return templateEngine;
    }
}
