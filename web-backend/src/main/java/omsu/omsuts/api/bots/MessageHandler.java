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
import omsu.omsuts.db.entities.User;
import omsu.omsuts.application.Application;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class MessageHandler {
    @Inject public ConnectionSource dbConnectionSource;

    public MessageHandler(BotWebSocket webSocket) {
        Application.getRunningApp().getApplicationComponent()
                .inject(this);

        Application.getRunningApp().getRoundService().setWebSocket(webSocket);
    }

    public static final String MESSAGE_TYPE_LOGIN = "login";
    public static final String MESSAGE_TYPE_LOGIN_STATUS = "loginStatus";

    public void updateSession(Session session) {
        if(!session.isOpen()) {
            val roundService = Application.getRunningApp().getRoundService();

            val username = roundService.getUsername(session);
            if (username != null) {
                roundService.removeBot(session, username);
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

        val roundService = Application.getRunningApp().getRoundService();
        if(roundService.hasSession(session)) {
            MessageSender.sendloginStatus(session, false, "bot already authorized");
            return;
        }

        if(roundService.hasUsername(loginRequestModel.getUsername())) {
            MessageSender.sendloginStatus(session, false, "user already attach other bot");
            //session.close(4007, "user already attach other bot");
            return;
        }

        Dao<User, String> userDao = DaoManager.createDao(dbConnectionSource, User.class);
        val user = userDao.queryForId(loginRequestModel.getUsername());
        if (user == null || !user.getPassword().equals(loginRequestModel.getPassword())) {
            MessageSender.sendloginStatus(session, false, "password is incorrect");
            //session.close(4008, "password is incorrect");
            return;
        }

        roundService.addBot(session, loginRequestModel.getUsername());
        log.info("Bot successfully connected; username: '{}'", loginRequestModel.getUsername());
        MessageSender.sendloginStatus(session, true, "");
    }

}
