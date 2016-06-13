package omsu.omsuts.application.service.round;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.BotWebSocket;
import omsu.omsuts.application.service.BackgroundService;
import org.eclipse.jetty.websocket.api.Session;
import rx.Observable;

import java.util.*;
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
                this::startRound,
                e -> log.error("Timer error:", e),
                () -> log.info("Timer service completed")
        ));
    }

    private void startRound(long number) {
        log.info("Round {} is started", number);
        val readyBots = new LinkedList<Session>(connectedBotsToUsernames.keySet());
        Collections.shuffle(readyBots);
        while (readyBots.size() >= 2) {
            val firstBot = readyBots.pollFirst();
            val secondBot = readyBots.pollFirst();
            val firstName = connectedBotsToUsernames.get(firstBot);
            val secondName = connectedBotsToUsernames.get(secondBot);
            log.info("Start game with {} and {}", firstName, secondName);
        }
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
