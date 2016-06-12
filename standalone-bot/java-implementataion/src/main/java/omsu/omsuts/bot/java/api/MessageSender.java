package omsu.omsuts.bot.java.api;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ws.WebSocket;
import okio.BufferedSink;
import omsu.omsuts.bot.java.api.json.models.LoginRequestModel;
import omsu.omsuts.bot.java.api.json.models.MessageModel;

import java.io.IOException;

import static okhttp3.ws.WebSocket.TEXT;
import static omsu.omsuts.bot.java.api.json.Utils.getJsonString;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
public class MessageSender {
    public static final String MESSAGE_TYPE_LOGIN = "login";
    public static final String MESSAGE_TYPE_LOGIN_STATUS = "loginStatus";

    private static void sendJson(WebSocket socket, Object jsonModel) {
        if (socket == null) {
            log.error("Socket is closed");
            return;
        }

        val jsonString = getJsonString(jsonModel);
        if (jsonString == null) {
            log.error("jsonString parse error");
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
        val loginRequestModel = new LoginRequestModel(username, password);
        sendJson(socket, new MessageModel(MESSAGE_TYPE_LOGIN,
                getJsonString(loginRequestModel)));
    }
}
