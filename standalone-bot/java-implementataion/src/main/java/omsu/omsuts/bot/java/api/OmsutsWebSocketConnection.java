package omsu.omsuts.bot.java.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

import java.io.IOException;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class OmsutsWebSocketConnection implements WebSocketListener {

    @Getter
    private WebSocket socket;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        if (socket != null) {
            log.error("Socket is already opened");
            return;
        }
        socket = webSocket;
        log.info("Connected.");

        MessageSender.login(socket, "qq", "ww");
    }

    @Override
    public void onFailure(IOException e, Response response) {
        socket = null;
        log.error("WebSocket failure:", e);
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        log.info("new message: got '{}'", message.string());
    }

    @Override
    public void onPong(Buffer payload) {
    }

    @Override
    public void onClose(int code, String reason) {
        socket = null;
        log.info("connection closed; code: {}, reason: '{}'", code, reason);
    }

    public boolean isConnected() {
        return socket != null;
    }
}
