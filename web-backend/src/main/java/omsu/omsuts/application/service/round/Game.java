package omsu.omsuts.application.service.round;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Created by sds on 6/13/16.
 */

public interface Game {
    void addBot(Session session);
    void start();

    void handleGameActionMessage(Session bot, String jsonMessage);
}
