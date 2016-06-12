package omsu.omsuts.bot.java.api.json.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sds on 6/13/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginStatusModel {
    boolean success;
    String reason;
}

