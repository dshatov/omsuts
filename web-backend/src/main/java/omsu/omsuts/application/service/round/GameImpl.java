package omsu.omsuts.application.service.round;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.application.Application;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by sds on 6/13/16.
 */

@Slf4j
public class GameImpl implements Game {
    private Session firstBot;
    private Session secondBot;
    private String firstName;
    private String secondName;

    private Integer secret;
    private Integer firstBotAnswer;
    private Integer secondBotAnswer;

    @Getter
    private boolean started = false;

    @Override
    public void addBot(Session session) {
        val roundManager = Application.getRunningApp().getRoundService();
        if (firstBot == null) {
            firstBot = session;
            firstName = roundManager.getUsername(session);
        }
        else if(secondBot == null) {
            secondBot = session;
            secondName = roundManager.getUsername(session);
        }
        else {
            log.error("Can't add bot to game, slots are full");
        }
    }

    @Override
    public void start() {
        if (!firstBot.isOpen() || !secondBot.isOpen()) {
            log.error("Can't start game: one of players is disconnected");
        }
        if (started) {
            log.error("Can't start game: already started");
        }
        log.info("Start game with {} and {}", firstName, secondName);
        started = true;

        secret = Math.round(10f * (float)Math.random());
    }

    @Override
    public void handleGameActionMessage(Session bot, String jsonMessage) {
        val roundManager = Application.getRunningApp().getRoundService();
        val senderName = roundManager.getUsername(bot);
        if (!firstName.equals(senderName) && !secondName.equals(senderName)) {
            log.error("sender isn't player at this game");
            return;
        }
        log.info("Action message got from '{}': '{}'", senderName, jsonMessage);
    }

    private void sendGameState(Session session) {
        
    }
}
