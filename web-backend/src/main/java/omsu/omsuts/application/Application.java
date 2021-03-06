/*
MIT License

Copyright (c) 2016 Dmitry Shatov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package omsu.omsuts.application;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.BotWebSocket;
import omsu.omsuts.api.RouteHandler;
import omsu.omsuts.application.service.round.RoundService;
import omsu.omsuts.db.entities.User;
import spark.Session;
import spark.TemplateEngine;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

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

    private static Application app;
    public static Application getRunningApp() {
        return app;
    }

    @Getter
    private RoundService roundService;

    public Application() {
        if(app != null) {
            throw new RuntimeException("Can't instantiate second application");
        }

        app = this;

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);

        routeHandler = new RouteHandler(this);

        roundService = new RoundService();
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

        //DEBUG ONLY!
        exception(Exception.class, (exception, request, response) -> {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            exception.printStackTrace(printWriter);
            String s = writer.toString();
            response.body(s);

            log.error("Got an exception:", exception);
        });
    }

    private void setupDB() {
        try {
            TableUtils.createTable(dbConnectionSource, User.class);

            Dao<User, String> userDAO = DaoManager.createDao(dbConnectionSource, User.class);
            userDAO.create(new User("qq", "ww", 0));
            userDAO.create(new User("qqq", "www", 0));
            userDAO.create(new User("aa", "ss", 0));
            userDAO.create(new User("aaa", "sss", 0));
            userDAO.create(new User("dmitry", "1q2w3e4r", 0));
            userDAO.create(new User("user", "hardcoded", 0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    //@SneakyThrows(SQLException.class)
    public void run() {
        //@SuppressWarnings("unused") @Cleanup val connection = dbConnectionSource;

        setupDB();
        setupSSL();
        webSocket("/connect", BotWebSocket.class);
        webSocketIdleTimeoutMillis(0);
        setupStaticFiles();
        setupRoutes();
        init();
        awaitInitialization();

        log.info("running service...");
        roundService.run();
        try {
            Thread.sleep(20 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        roundService.stop();
        log.info("service is stopped");

//        val in = new Scanner(System.in);
//        do{
//            System.out.println("Command:");
//            val cmd = in.nextLine();
//            if ("exit".equals(cmd)) {
//                break;
//            }
//            log.error("Invalid command: '{}'", cmd);
//        } while (true);

//        stop();
    }
}
