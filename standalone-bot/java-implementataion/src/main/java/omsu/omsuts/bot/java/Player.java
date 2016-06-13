package omsu.omsuts.bot.java;

import omsu.omsuts.bot.java.api.json.models.GameActionModel;
import omsu.omsuts.bot.java.api.json.models.GameStateModel;

/**
 * Created by sds on 6/13/16.
 */

public interface Player {
    GameActionModel answer(GameStateModel gameState);
}
