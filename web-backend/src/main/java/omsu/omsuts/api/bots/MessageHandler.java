package omsu.omsuts.api.bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.support.ConnectionSource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.json.models.LoginRequestModel;
import omsu.omsuts.api.bots.json.models.MessageModel;
import omsu.omsuts.application.Application;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class MessageHandler {

    @Inject public ConnectionSource dbConnectionSource;

    private BotWebSocket webSocket;

    public MessageHandler(BotWebSocket webSocket) {
        Application.getRunningApp().getApplicationComponent()
                .inject(this);

        this.webSocket = webSocket;
    }

    public static final String MESSAGE_TYPE_LOGIN = "login";

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

    public void handleLoginRequest(Session session, LoginRequestModel loginRequestModel) {
        log.info("login request received; username:'{}', password:'{}'",
                loginRequestModel.getUsername(),
                loginRequestModel.getPassword());

        
    }

}
