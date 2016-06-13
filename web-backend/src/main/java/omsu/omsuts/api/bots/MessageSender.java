package omsu.omsuts.api.bots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.json.models.GameStateModel;
import omsu.omsuts.api.bots.json.models.LoginRequestModel;
import omsu.omsuts.api.bots.json.models.LoginStatusModel;
import omsu.omsuts.api.bots.json.models.MessageModel;
import omsu.omsuts.api.json.Utils;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_GAMESTATE;
import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_LOGIN;
import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_LOGIN_STATUS;
import static omsu.omsuts.api.json.Utils.getJsonString;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class MessageSender {
    private static void sendJson(Session session, Object jsonModel) {
        if (session == null || !session.isOpen()) {
            log.error("Session is closed");
            return;
        }

        val jsonString = getJsonString(jsonModel);
        if (jsonString == null) {
            log.error("Failed to send jsonModel: jsonString building error");
            return;
        }

        try {
            session.getRemote().sendString(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to send jsonModel", e);
        }
    }


    public static void sendloginStatus(Session session, boolean success, String reason) {
        log.info("Send login status...");
        val loginStatusModel = new LoginStatusModel(success, reason);
        sendJson(session, new MessageModel(MESSAGE_TYPE_LOGIN_STATUS,
                getJsonString(loginStatusModel)));
    }

    public static void sendGameState(Session session,
                                     Integer lastAnswer, boolean warmer, boolean first) {
        log.info("Send gamestate...");
        val gameStateModel = new GameStateModel(lastAnswer, warmer, first);
        sendJson(session, new MessageModel(MESSAGE_TYPE_GAMESTATE,
                getJsonString(gameStateModel)));
    }
}
