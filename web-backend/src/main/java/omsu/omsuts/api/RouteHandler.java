package omsu.omsuts.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.db.entities.User;
import omsu.omsuts.api.json.models.CredentialsModel;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by sds on 6/9/16.
 */

@Slf4j
public class RouteHandler {
    private final ConnectionSource dbConnectionSource;

    public RouteHandler(ConnectionSource connectionSource) {
        dbConnectionSource = connectionSource;
    }

    public ModelAndView handleRoot (Request req, Response res) {
        val attributes = new HashMap<String, Object>();
        val message = req.session(false) != null
                ? "welcome back, " + req.session(false).attribute("user")
                : "Hello, anon";
        attributes.put("message", message);

        return new ModelAndView(attributes, "index.html");
    }

    public String handleLogin (Request req, Response res) {
        val objectMapper = new ObjectMapper();

        //TODO: replace serverside json-request generation with clientside
        final String jsonRequest;
        try {
            jsonRequest = objectMapper
                        .writeValueAsString(req.queryMap().toMap())
                        .replaceAll("\\[", "")
                        .replaceAll("]", "");
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            return null;
        }

        final CredentialsModel credentialsModel;
        try {
            credentialsModel = objectMapper.readValue(jsonRequest, CredentialsModel.class);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        log.info("UserLogin: {}", credentialsModel);

        if (!credentialsModel.isValid()) {
            res.status(HTTP_BAD_REQUEST);
            log.warn("userLogin model isn't valid");

            return null;
        }

        final User user;
        try {
            Dao<User, String> userDao = DaoManager.createDao(dbConnectionSource, User.class);
            user = userDao.queryForId(credentialsModel.getLogin());
        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }

        if (user != null && user.getPassword().equals(credentialsModel.getPassword())) {
            req.session(true).attribute("user", credentialsModel.getLogin());

            return "welcome, " + credentialsModel.getLogin();
        }

        return "Incorrect login or password";
    }
}
