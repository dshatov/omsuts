package omsu.omsuts.application.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.BotWebSocket;
import org.eclipse.jetty.websocket.api.Session;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by sds on 6/13/16.
 */

@Slf4j
public class RoundService extends BackgroundService {
    private static final Map<Session, String> connectedBotsToUsernames = new ConcurrentHashMap<>();
    private static final Map<String, Session> connectedUsernamesToBots = new ConcurrentHashMap<>();

    private BotWebSocket webSocket;

    @Override
    public void run() {
        val timer = Observable.interval(2, 1, TimeUnit.SECONDS);
        addSubscription(timer.subscribe(
                v -> log.info("Received from timer: {}", v),
                e -> log.error("Timer error:", e),
                () -> log.info("Timer service completed")
        ));
    }


    public void setWebSocket(BotWebSocket webSocket) {
        if (this.webSocket != null) {
            log.warn("webSocket already defined at RoundService; redefine");
        }
        this.webSocket = webSocket;
    }

    public String getUsername(Session session) {
        return connectedBotsToUsernames.getOrDefault(session, null);
    }


    public void removeBot(Session session, String username) {
        connectedBotsToUsernames.remove(session);
        connectedUsernamesToBots.remove(username);
    }

    public void addBot(Session session, String username) {
        connectedUsernamesToBots.put(username, session);
        connectedBotsToUsernames.put(session, username);
    }

    public boolean hasSession(Session session) {
        return connectedBotsToUsernames.containsKey(session);
    }

    public boolean hasUsername(String username) {
        return connectedUsernamesToBots.containsKey(username);
    }
}
