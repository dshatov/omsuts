package omsu.omsuts.application.service.round;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.BotWebSocket;
import omsu.omsuts.api.bots.MessageSender;
import omsu.omsuts.api.bots.json.models.GameActionModel;
import omsu.omsuts.application.Application;
import omsu.omsuts.application.service.BackgroundService;
import omsu.omsuts.db.entities.User;
import org.eclipse.jetty.websocket.api.Session;
import rx.Observable;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by sds on 6/13/16.
 */

@Slf4j
public class RoundService extends BackgroundService {
    private final Map<Session, String> connectedBotsToUsernames = new ConcurrentHashMap<>();
    private final Map<String, Session> connectedUsernamesToBots = new ConcurrentHashMap<>();

    private final Map<Session, Game> botsToGames = new ConcurrentHashMap<>();

    private BotWebSocket webSocket;

    @Inject public ConnectionSource dbConnectionSource;

    public RoundService() {
        Application.getRunningApp().getApplicationComponent()
                .inject(this);
    }

    @Override
    public void run() {
        val timer = Observable.interval(30, 10, TimeUnit.SECONDS);
        addSubscription(timer.subscribe(
                this::startRound,
                e -> log.error("Timer error:", e),
                () -> log.info("Timer service completed")
        ));
    }

    @SneakyThrows(SQLException.class)
    public void givePointsToUser(String username, int points) {
        Dao<User, String> userDao = DaoManager.createDao(dbConnectionSource, User.class);
        val user = userDao.queryForId(username);
        if (user != null ) {
            user.setScore(user.getScore() + points);
            userDao.update(user);
        }
    }

    private void startRound(long number) {
        //Clear old games
        botsToGames.clear();

        log.info("Round {} is started", number);
        val readyBots = new LinkedList<Session>(connectedBotsToUsernames.keySet());
        Collections.shuffle(readyBots);
        while (readyBots.size() >= 2) {
            val firstBot = readyBots.pollFirst();
            val secondBot = readyBots.pollFirst();

            //create and start game for first and second bots
            Game game = new GameImpl();
            game.addBot(firstBot);
            game.addBot(secondBot);

            botsToGames.put(firstBot, game);
            botsToGames.put(secondBot, game);

            game.start();
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

    public void sendGameActionToGame(Session session, GameActionModel gameActionModel) {
        val game = botsToGames.getOrDefault(session, null);
        if (game != null) {
            game.handleGameAction(session, gameActionModel);
        }
    }

    public boolean hasSession(Session session) {
        return connectedBotsToUsernames.containsKey(session);
    }

    public boolean hasUsername(String username) {
        return connectedUsernamesToBots.containsKey(username);
    }
}
