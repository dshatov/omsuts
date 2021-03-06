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

package omsu.omsuts.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.db.entities.User;
import omsu.omsuts.api.json.models.CredentialsModel;
import omsu.omsuts.application.Application;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateEngine;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Created by sds on 6/9/16.
 */

@Slf4j
public class RouteHandler {
    @Inject public ConnectionSource dbConnectionSource;
    @Inject public TemplateEngine templateEngine;

    public RouteHandler(Application app) {
        app.getApplicationComponent()
                .inject(this);
    }



    public String handleRoot (Request req, Response res) {
        val attributes = new HashMap<String, Object>();
        val authorized = req.session(false) != null;
        if (authorized) {
            val username = req.session(false).attribute("user");
            attributes.put("username", username);

            List<User> users;
            try {
                Dao<User, String> userDao = DaoManager.createDao(dbConnectionSource, User.class);
                users = userDao.queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();

                users = new ArrayList<>();
            }
            users.sort((user1, user2) -> user2.getScore() - user1.getScore());
            attributes.put("users", users);

            return templateEngine.render(new ModelAndView(attributes, "main_authorized.html"));
        }
        else {
            return templateEngine.render(new ModelAndView(attributes, "main_unauthorized.html"));
        }
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
            user = userDao.queryForId(credentialsModel.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }

        if (user != null && user.getPassword().equals(credentialsModel.getPassword())) {
            req.session(true).attribute("user", credentialsModel.getUsername());
            res.redirect("/");

            return "Welcome, " + credentialsModel.getUsername();
        }

        val attributes = new HashMap<String, Object>();
        return templateEngine.render(new ModelAndView(attributes, "login_failed.html"));
    }



    public String handleLogout (Request req, Response res) {
        res.redirect("/");
        val authorized = req.session(false) != null;
        if (authorized) {
            req.session().invalidate();
        }
        return "Bye!";
    }
}
