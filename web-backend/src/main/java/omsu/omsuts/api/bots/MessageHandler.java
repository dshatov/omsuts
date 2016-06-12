package omsu.omsuts.api.bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.json.models.LoginRequestModel;
import omsu.omsuts.api.bots.json.models.MessageModel;
import omsu.omsuts.api.db.entities.User;
import omsu.omsuts.application.Application;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class MessageHandler {
    private static final Map<Session, String> connectedBotsToUsernames = new HashMap<>();
    private static final Map<String, Session> connectedUsernamesToBots = new HashMap<>();


    @Inject public ConnectionSource dbConnectionSource;

    private BotWebSocket webSocket;

    public MessageHandler(BotWebSocket webSocket) {
        Application.getRunningApp().getApplicationComponent()
                .inject(this);

        this.webSocket = webSocket;
    }

    public static final String MESSAGE_TYPE_LOGIN = "login";

    public void updateSession(Session session) {
        if(!session.isOpen()) {
            val username = connectedBotsToUsernames.getOrDefault(session, null);
            if (username != null) {
                connectedBotsToUsernames.remove(session);
                connectedUsernamesToBots.remove(username);
            }
        }
    }

    public void handle(Session session, String message) {
        final MessageModel messageModel;
        val objectMapper = new ObjectMapper();
        try {
            messageModel = objectMapper.readValue(message, MessageModel.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (messageModel.getBody() == null || messageModel.getBody().length() == 0) {
            log.error("Can't handle empty message");
            return;
        }



        if (MESSAGE_TYPE_LOGIN.equals(messageModel.getMessageType())) {
            LoginRequestModel loginRequestModel;
            try {
                loginRequestModel = objectMapper.readValue(messageModel.getBody(),
                        LoginRequestModel.class);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            handleLoginRequest(session, loginRequestModel);
        }
        else {
            log.error("Can't handle message with unknown type");
        }
    }

    @SneakyThrows(SQLException.class)
    public void handleLoginRequest(Session session, LoginRequestModel loginRequestModel) {
        log.info("login request received; username:'{}', password:'{}'",
                loginRequestModel.getUsername(),
                loginRequestModel.getPassword());

        if(connectedBotsToUsernames.containsKey(session)) {
            //bot already authorized

            return;
        }

        if(connectedUsernamesToBots.containsKey(loginRequestModel.getUsername())) {
            //user already attach other bot

            session.close(4007, "user already attach other bot");
            return;
        }

        Dao<User, String> userDao = DaoManager.createDao(dbConnectionSource, User.class);
        val user = userDao.queryForId(loginRequestModel.getUsername());
        if (user == null || !user.getPassword().equals(loginRequestModel.getPassword())) {
            //password is incorrect

            session.close(4008, "password is incorrect");
            return;
        }

        connectedUsernamesToBots.put(loginRequestModel.getUsername(), session);
        connectedBotsToUsernames.put(session, loginRequestModel.getUsername());
        log.info("Bot successfully connected; username: '{}'", loginRequestModel.getUsername());
    }

}
