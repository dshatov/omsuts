package omsu.omsuts.application;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.RouteHandler;
import omsu.omsuts.api.db.entities.User;
import spark.Session;
import spark.template.freemarker.FreeMarkerEngine;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Scanner;

import static spark.Spark.*;
import static spark.Spark.get;

/**
 * Created by sds on 08.06.16.
 */
@Slf4j
public class Application implements Runnable {
    public static final String RESOURCES_DIR = System.getProperty("user.dir") + "/src/main/resources";
    public static final String STATIC_FILES_DIR = "/public";
    public static final String FREEMARKER_TEMPLATES_DIR = "/ftl";
    public static final String FREEMARKER_VERSION = "2.3.24";

    @Inject public ConnectionSource dbConnectionSource;
    @Inject public FreeMarkerEngine freeMarkerEngine;

    private RouteHandler routeHandler;

    public Application() {
        omsu.omsuts.application.DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build()
                .inject(this);

        routeHandler = new RouteHandler(dbConnectionSource);
    }

    private void setupStaticFiles() {
        //staticFiles.location(STATIC_FILES_DIR);

        //DEBUG
        staticFiles.externalLocation(RESOURCES_DIR + STATIC_FILES_DIR);
    }

    private void setupSSL() {
        port(8443);

        val keyStoreLocation = RESOURCES_DIR + "/deploy/identity.jks";
        val keyStorePassword = "qwerty";
        secure(keyStoreLocation, keyStorePassword, null, null);
    }

    private void setupRoutes() {
        get("/", routeHandler::handleRoot, freeMarkerEngine);
        get("/hello", (req, res) -> "hello");
        post("/login", routeHandler::handleLogin);

        before((req, res) -> {
            Session session = req.session(false);
            if (session != null) {
                log.info(session.attribute("user"));
            }
        });

        //DEBUG
        exception(Exception.class, (exception, request, response) -> {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            exception.printStackTrace(printWriter);
            String s = writer.toString();
            response.body(s);
        });
    }

    private void setupDB() {
        try {
            TableUtils.createTable(dbConnectionSource, User.class);

            Dao<User, String> userDAO = DaoManager.createDao(dbConnectionSource, User.class);
            User user = new User("qq", "ww");
            userDAO.create(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows(SQLException.class)
    public void run() {
        @SuppressWarnings("unused") @Cleanup val connection = dbConnectionSource;

        setupDB();
        setupSSL();
        setupStaticFiles();
        setupRoutes();

        val in = new Scanner(System.in);
        do{
            System.out.println("Command:");
            val cmd = in.nextLine();
            if ("exit".equals(cmd)) {
                break;
            }
            log.error("Invalid command: '{}'", cmd);
        } while (true);

        stop();
    }
}
