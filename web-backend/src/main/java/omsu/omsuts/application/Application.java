package omsu.omsuts.application;

import org.slf4j.Logger;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static spark.Spark.*;
import static spark.Spark.get;

/**
 * Created by sds on 08.06.16.
 */
public class Application implements Runnable {
    public static final String RESOURCES_DIR = System.getProperty("user.dir") + "/src/main/resources";
    public static final String STATIC_FILES_DIR = "/public";
    public static final String FREEMARKER_TEMPLATES_DIR = "/ftl";
    public static final String FREEMARKER_VERSION = "2.3.24";

    @Inject public Logger log;
    @Inject public FreeMarkerEngine freeMarkerEngine;

    public Application() {
        omsu.omsuts.application.DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build()
                .inject(this);
    }

    private void setupStaticFiles() {
        //staticFiles.location(STATIC_FILES_DIR);
        staticFiles.externalLocation(RESOURCES_DIR + STATIC_FILES_DIR);
    }

    private void setupSSL() {
        port(8443);

        String keyStoreLocation = RESOURCES_DIR + "/deploy/identity.jks";
        String keyStorePassword = "qwerty";
        secure(keyStoreLocation, keyStorePassword, null, null);
    }

    private void setupRoutes() {
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            return new ModelAndView(attributes, "index.html");
        }, freeMarkerEngine);

        get("/hello", (req, res) -> "Hello");
    }

    @Override
    public void run() {
        setupSSL();
        setupStaticFiles();
        setupRoutes();

        Scanner in = new Scanner(System.in);
        do{
            System.out.println("Command:");
            String cmd = in.nextLine();
            if ("exit".equals(cmd)) {
                break;
            }
            log.error("Invalid command: '{}'", cmd);
        } while (true);

        stop();
    }
}
