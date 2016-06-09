package omsu.omsuts.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.db.models.User;
import omsu.omsuts.api.json.models.UserLoginModel;
import spark.ModelAndView;
import spark.Session;
import spark.template.freemarker.FreeMarkerEngine;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
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

        val keyStoreLocation = RESOURCES_DIR + "/deploy/identity.jks";
        val keyStorePassword = "qwerty";
        secure(keyStoreLocation, keyStorePassword, null, null);
    }

    private void setupRoutes() {
        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            final String message = req.session(false) != null
                    ? "welcome back, " + req.session(false).attribute("user")
                    : "Hello, anon";
            attributes.put("message", message);

            return new ModelAndView(attributes, "index.html");
        }, freeMarkerEngine);

        get("/hello", (req, res) -> "Hello");

        post("/login", (req, res) -> {
            ObjectMapper objectMapper = new ObjectMapper();

            //TODO: replace serverside json-request generation with clientside
            final String jsonRequest = objectMapper
                    .writeValueAsString(req.queryMap().toMap())
                    .replaceAll("\\[", "")
                    .replaceAll("]", "");

            final UserLoginModel userLogin;
            log.info("jsonRequest: {}", jsonRequest);

            try {
                userLogin = objectMapper.readValue(jsonRequest, UserLoginModel.class);

                log.info("UserLogin: {}", userLogin);

                if (!userLogin.isValid()) {
                    res.status(HTTP_BAD_REQUEST);
                    log.warn("userLogin model isn't valid");
                    return null;
                }
            } catch (JsonParseException e) {
                res.status(HTTP_BAD_REQUEST);
                log.warn("jsonRequest parse error", e);
                return null;
            } catch (JsonMappingException e) {
                res.status(HTTP_BAD_REQUEST);
                log.warn("jsonRequest mapping error", e);
                return null;
            }

            try {
                Dao<User, String> userDAO = DaoManager.createDao(dbConnectionSource, User.class);
                User user = userDAO.queryForId(userLogin.getLogin());
                if (user != null && user.getPassword().equals(userLogin.getPassword())) {
                    req.session(true).attribute("user", userLogin.getLogin());
                    return "welcome, " + userLogin.getLogin();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return "Incorrect login or password";
        });

        before((req, res) -> {
            Session session = req.session(false);
            if (session != null) {
                log.info(session.attribute("user"));
            }
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
