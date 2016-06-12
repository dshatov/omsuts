package omsu.omsuts.bot.java.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ws.WebSocket;
import okio.BufferedSink;
import omsu.omsuts.bot.java.api.json.models.LoginRequestModel;

import java.io.IOException;

import static okhttp3.ws.WebSocket.TEXT;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
public class RequestHelper {
    public static final String ACTION_LOGIN = "login";

    private static void sendJson(WebSocket socket, Object jsonModel) {
        if (socket == null) {
            log.error("Socket is closed");
            return;
        }

        val objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(jsonModel);
        } catch (JsonProcessingException e) {
            log.error("Failed to create json string", e);
            return;
        }

        try {
            socket.sendMessage(new RequestBody() {
                @Override
                public MediaType contentType() {
                    return TEXT;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.writeUtf8(jsonString);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void login(WebSocket socket, String username, String password) {
        log.info("Trying to log in...");
        sendJson(socket, new LoginRequestModel(ACTION_LOGIN, username, password));
    }
}
