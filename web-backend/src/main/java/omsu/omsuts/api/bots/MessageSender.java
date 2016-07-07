/*
MIT License

Copyright (c) 2016 Dmitry Shatov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package omsu.omsuts.api.bots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import omsu.omsuts.api.bots.json.models.GameStateModel;
import omsu.omsuts.api.bots.json.models.LoginRequestModel;
import omsu.omsuts.api.bots.json.models.LoginStatusModel;
import omsu.omsuts.api.bots.json.models.MessageModel;
import omsu.omsuts.api.json.Utils;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_GAMESTATE;
import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_LOGIN;
import static omsu.omsuts.api.bots.MessageHandler.MESSAGE_TYPE_LOGIN_STATUS;
import static omsu.omsuts.api.json.Utils.getJsonString;

/**
 * Created by sds on 6/12/16.
 */

@Slf4j
public class MessageSender {
    private static void sendJson(Session session, Object jsonModel) {
        if (session == null || !session.isOpen()) {
            log.error("Session is closed");
            return;
        }

        val jsonString = getJsonString(jsonModel);
        if (jsonString == null) {
            log.error("Failed to send jsonModel: jsonString building error");
            return;
        }

        try {
            session.getRemote().sendString(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to send jsonModel", e);
        }
    }


    public static void sendloginStatus(Session session, boolean success, String reason) {
        log.info("Send login status...");
        val loginStatusModel = new LoginStatusModel(success, reason);
        sendJson(session, new MessageModel(MESSAGE_TYPE_LOGIN_STATUS,
                getJsonString(loginStatusModel)));
    }

    public static void sendGameState(Session session,
                                     Integer lastAnswer, boolean warmer, boolean first) {
        log.info("Send gamestate...");
        val gameStateModel = new GameStateModel(lastAnswer, warmer, first);
        sendJson(session, new MessageModel(MESSAGE_TYPE_GAMESTATE,
                getJsonString(gameStateModel)));
    }
}
