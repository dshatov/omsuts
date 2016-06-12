package omsu.omsuts.api.bots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

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

        val objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(jsonModel);
        } catch (JsonProcessingException e) {
            log.error("Failed to create json string", e);
            return;
        }

        try {
            session.getRemote().sendString(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        public static void loginStatus(Session session, String username, String password) {
        //log.info("Send login status...");
        //sendJson(session, new Logsdfs(ACTION_LOGIN, username, password));
    }
}
