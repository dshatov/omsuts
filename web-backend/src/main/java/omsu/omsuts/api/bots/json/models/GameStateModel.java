package omsu.omsuts.api.bots.json.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sds on 6/13/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateModel {
    private Integer lastAnswer;
    private boolean warmer;
    private boolean first;
}
