package omsu.omsuts.bot.java.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import omsu.omsuts.bot.java.api.json.models.LoginStatusModel;
import omsu.omsuts.bot.java.api.json.models.MessageModel;

import java.io.IOException;
import java.util.Scanner;

import static omsu.omsuts.bot.java.api.MessageSender.MESSAGE_TYPE_LOGIN_STATUS;

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
        log.info("Socket is opened");

        val in = new Scanner(System.in);
        System.out.println("Username:");
        val username = in.nextLine();
        System.out.println("Password:");
        val password = in.nextLine();

        MessageSender.login(socket, username, password);
    }

    @Override
    public void onFailure(IOException e, Response response) {
        socket = null;
        log.error("WebSocket failure:", e);
    }

    @Override
    public void onMessage(ResponseBody message) throws IOException {
        val msg = message.string();
        log.info("new message: got '{}'", msg);
        handle(msg);
    }

    @Override
    public void onPong(Buffer payload) {
    }

    @Override
    public void onClose(int code, String reason) {
        socket = null;
        log.info("connection closed; code: {}, reason: '{}'", code, reason);
    }

    private void handle(String message) {
        final MessageModel messageModel;
        val objectMapper = new ObjectMapper();
        try {
            messageModel = objectMapper.readValue(message, MessageModel.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (messageModel.getBody() == null || messageModel.getBody().length() == 0) {
            log.error("Can't handle empty message");
            return;
        }

        if (MESSAGE_TYPE_LOGIN_STATUS.equals(messageModel.getMessageType())) {
            LoginStatusModel loginStatusModel;
            try {
                loginStatusModel = objectMapper.readValue(messageModel.getBody(),
                        LoginStatusModel.class);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            handleLoginStatus(loginStatusModel);
        }
        else {
            log.error("Can't handle message with unknown type");
        }
    }

    private void handleLoginStatus(LoginStatusModel loginStatusModel) {
        if (loginStatusModel.isSuccess()) {
            System.out.println("Connected!");
        }
        else {
            System.out.println("Connection error: " + loginStatusModel.getReason());
        }
    }

    public boolean isConnected() {
        return socket != null;
    }
}
