package omsu.omsuts.bot.java;

import omsu.omsuts.bot.java.api.json.models.GameActionModel;
import omsu.omsuts.bot.java.api.json.models.GameStateModel;

public class PlayerImpl implements Player {
    @Override
    public GameActionModel answer(GameStateModel gameState) {
        return new GameActionModel(Math.round((float) Math.random() * 10f));
    }
}
