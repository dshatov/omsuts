package omsu.omsuts.application.service.round;

import omsu.omsuts.api.bots.json.models.GameActionModel;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by sds on 6/13/16.
 */

public interface Game {
    void addBot(Session session);
    void start();

    void handleGameAction(Session bot, GameActionModel gameActionModel);
}
