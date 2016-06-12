package omsu.omsuts.bot.java.api.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
public class Utils {
    public static String getJsonString(Object jsonModel) {
        val objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(jsonModel);
        } catch (JsonProcessingException e) {
            log.error("Failed to create json string", e);
            return null;
        }

        return jsonString;
    }
}
