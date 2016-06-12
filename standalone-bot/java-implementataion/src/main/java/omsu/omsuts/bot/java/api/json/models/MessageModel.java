package omsu.omsuts.bot.java.api.json.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sds on 6/12/16.
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageModel {
    private String messageType;
    private String body;
}
