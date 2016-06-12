package omsu.omsuts.bot.java.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSink;

import java.io.IOException;

import static okhttp3.ws.WebSocket.TEXT;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class OmsutsWebSocketListener implements WebSocketListener {

    @Getter
    private WebSocket socket;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("websocket onOpen()");
        if (socket != null) {
            log.warn("Socket is already opened");
            return;
        }
        socket = webSocket;

        try {
            webSocket.sendMessage(new RequestBody() {
                @Override
                public MediaType contentType() {
                    return TEXT;
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.writeUtf8("Hello, chief!");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(IOException e, Response response) {
        socket = null;
        log.info("websocket onFailure()", e);
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        log.info("websocket onMessage(): got '{}'", message.string());
    }

    @Override
    public void onPong(Buffer payload) {
        log.info("websocket onPong()");
    }

    @Override
    public void onClose(int code, String reason) {
        socket = null;
        log.info("websocket onClose()");
    }
}
