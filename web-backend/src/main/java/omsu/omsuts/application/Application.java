package omsu.omsuts.application;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.RouteHandler;
import omsu.omsuts.api.db.entities.User;
import spark.Session;
import spark.TemplateEngine;

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
    public static final String TEMPLATES_DIR = "/ftl";

    @Inject public ConnectionSource dbConnectionSource;
    @Inject public TemplateEngine templateEngine;

    private RouteHandler routeHandler;

    @Getter
    private ApplicationComponent applicationComponent;

    public Application() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);

        routeHandler = new RouteHandler(this);
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
        before((req, res) -> {
            Session session = req.session(false);
            if (session != null) {
                log.info("'" + req.pathInfo() + "' request from " + session.attribute("user"));
            }
            else {
                log.info("'" + req.pathInfo() + "' request from unauthorized user");
            }
        });

        get("/", routeHandler::handleRoot);
        post("/login", routeHandler::handleLogin);
        get("/login", (req, res) -> {
            res.redirect("/");
            return "";
        });
        get("/logout", routeHandler::handleLogout);

        get("/*", (req, res) -> {
            res.status(404);
            res.redirect("/404.html");
            return "Page not found :(";
        });

        //DEBUG ONLY!
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
            userDAO.create(new User("qq", "ww"));
            userDAO.create(new User("dmitry", "1q2w3e4r"));
            userDAO.create(new User("user", "hardcoded"));
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
