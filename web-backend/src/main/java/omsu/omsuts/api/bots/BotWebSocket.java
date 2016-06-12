package omsu.omsuts.api.bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import omsu.omsuts.api.bots.json.models.MessageModel;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
@WebSocket
public class BotWebSocket {

    private MessageHandler messageHandler;

    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    private static final Map<Session, String> connectedUsers = new HashMap<>();

    private static final Map<String, Session> authenticatedUserSessions = new HashMap<>();

    public BotWebSocket() {
        this.messageHandler = new MessageHandler(this);
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        log.info("websocket connected()");
        sessions.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        log.info("websocket closed(); status: {}, reason: '{}'", statusCode, reason);
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.info("websocket message(): {}", message);
        messageHandler.handle(session, message);

//        session.getRemote().sendString(message); // and send it back
    }


}

