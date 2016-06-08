package omsu.omsuts.webcore;

import freemarker.template.Configuration;
import freemarker.template.Version;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by sds on 07.06.16.
 */
public class Main {

    public static final String RESOURCES_DIR = System.getProperty("user.dir") + "/src/main/resources";
    public static final String STATIC_FILES_DIR = "/public";
    public static final String FREEMARKER_TEMPLATES_DIR = "/ftl";
    public static final String FREEMARKER_VERSION = "2.3.24";

    private FreeMarkerEngine freeMarkerEngine;

    private Main() {
        Configuration freeMarkerConfiguration = new Configuration(new Version(FREEMARKER_VERSION));

        try {
            freeMarkerConfiguration.setDirectoryForTemplateLoading(new File(RESOURCES_DIR + FREEMARKER_TEMPLATES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(Main.class, FREEMARKER_TEMPLATES_DIR));

        freeMarkerEngine = new FreeMarkerEngine(freeMarkerConfiguration);
    }

    public static void main(String[] args) {
        new Main()
                .setupSSL()
                .setupStaticFiles()
                .setupRoutes();
    }

    private Main setupStaticFiles() {
        //staticFiles.location(STATIC_FILES_DIR);
        staticFiles.externalLocation(RESOURCES_DIR + STATIC_FILES_DIR);

        return this;
    }

    private Main setupSSL() {
        port(8443);

        String keyStoreLocation = RESOURCES_DIR + "/deploy/identity.jks";
        String keyStorePassword = "qwerty";
        secure(keyStoreLocation, keyStorePassword, null, null);
        return this;
    }

    private Main setupRoutes() {
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            return new ModelAndView(attributes, "index.html");
        }, freeMarkerEngine);

        get("/hello", (req, res) -> "Hello");

        return this;
    }
}

