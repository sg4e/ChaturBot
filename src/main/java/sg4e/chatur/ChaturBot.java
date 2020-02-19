/*
 * Copyright 2020 sg4e.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sg4e.chatur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sg4e.chatur.json.Notification;
import sg4e.chatur.json.RoomMessage;

/**
 * The gateway to the chat API; subclass and override relevant methods for
 * callbacks.
 */
public class ChaturBot {

    private static final WebSocketFactory WEBSOCKET_FACTORY = new WebSocketFactory();
    private static final String CONNECT_SUCCESSFUL_MESSAGE = "o";
    private static final String AUTH_SUCCESSFUL_MESSAGE = "a[\"{\\\"args\\\":[\\\"1\\\"],\\\"callback\\\":null,\\\"method\\\":\\\"onAuthResponse\\\"}\"]";
    private static final String UPDATE_ROOM_COUNT_RESPONSE_METHOD = "onRoomCountUpdate";
    private static final Logger LOG = LoggerFactory.getLogger(ChaturBot.class);

    private final ObjectMapper mapper;
    private final WebSocket ws;
    private final String roomName;
    private volatile boolean authenticated;

    /**
     * Creates a new instance; {@link #start()} must be called to open the
     * connection.
     *
     * @param URL
     * @param connectAuth
     * @param roomName
     * @throws IOException
     */
    public ChaturBot(String roomName, String URL, String connectAuth) throws IOException {
        this.roomName = roomName;
        authenticated = false;
        ws = WEBSOCKET_FACTORY.createSocket(URL);
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        ws.addListener(new WebSocketAdapter() {
            private int handshakePhase = 0;

            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                LOG.error("Error during WS message parsing", cause);
            }

            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                LOG.trace(message);
                if(handshakePhase == 0) {
                    if(!CONNECT_SUCCESSFUL_MESSAGE.equals(message)) {
                        LOG.warn("Unexpected message after connection: {}", message);
                    }
                    websocket.sendText(connectAuth);
                    handshakePhase++;
                }
                if(message.startsWith("a")) {
                    ParsedWebsocketMessage parsed = new ParsedWebsocketMessage(message);
                    switch(parsed.getMethod()) {
                        case "onAuthResponse":
                            onAuthResponse(websocket, message);
                            break;
                        case "onRoomMsg":
                            onRoomMsg(parsed.mapToObject(1, RoomMessage.class));
                            break;
                        case "onTitleChange":
                            onTitleChange(parsed.mapToObject(0, String.class));
                            break;
                        case "onNotify":
                            onNotify(parsed.mapToObject(0, Notification.class));
                            break;
                        case "onNotifyGroupShowCancel":
                            onNotifyGroupShowCancel();
                            break;
                        case "onSilence":
                            onSilence(parsed.getArgs().get(0).asText(), parsed.getArgs().get(1).asText());
                            break;
                        case UPDATE_ROOM_COUNT_RESPONSE_METHOD:
                            //this is handled by a dedicated listener
                            break;
                        default:
                            LOG.warn(String.format("Unrecognized method %s: %s", parsed.getMethod(), message));
                            break;
                    }
                } else {
                    //ignore
                }
            }
        });
    }

    /**
     * Opens the connection and starts receiving messages from the server.
     *
     * @throws WebSocketException
     */
    public void start() throws WebSocketException {
        ws.connect();
    }

    /**
     * Closes the connection and stops receiving messages form the server.
     */
    public void stop() {
        ws.disconnect();
    }

    /**
     * Queries the server for number of users in room and returns the response
     * asynchronously inside the {@link RoomCount} object. See the object's
     * documentation for how to manage the asynchronous nature of the query.
     *
     * @return
     * @throws IllegalStateException if {@link #start()} has not been called, or
     * authentication with server is incomplete (see {@link #isAuthenticated()}.
     */
    public RoomCount getRoomCount() {
        if(!ws.isOpen()) {
            throw new IllegalStateException(String.format("Must call start() on %s before room count query", getClass().getSimpleName()));
        }
        if(!isAuthenticated()) {
            throw new IllegalStateException("Client has not yet authenticated");
        }
        RoomCount count = new RoomCount();
        ws.addListener(new WebSocketAdapter() {
            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                LOG.error("Uncaught exception in room count server response parsing");
                count.setError(cause);
                ws.removeListener(this);
            }

            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                ParsedWebsocketMessage parsed = new ParsedWebsocketMessage(message);
                if(UPDATE_ROOM_COUNT_RESPONSE_METHOD.equals(parsed.getMethod())) {
                    count.set(Integer.parseInt(parsed.mapToObject(0, String.class)));
                    ws.removeListener(this);
                }
            }
        });
        ws.sendText(String.format("[\"{\\\"method\\\":\\\"updateRoomCount\\\",\\\"data\\\":{\\\"model_name\\\":\\\"%s\\\",\\\"private_room\\\":false}}\"]", roomName));
        return count;
    }

    /**
     * Returns whether the authentication process with the server has succeeded
     * yet.
     *
     * @return
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the room name, as specified to the constructor.
     *
     * @return
     */
    public String getRoomName() {
        return roomName;
    }

    @Data
    private class ParsedWebsocketMessage {

        private final String method;
        private final JsonNode args;

        public ParsedWebsocketMessage(String rawMessage) throws JsonProcessingException {
            String stripped = StringEscapeUtils.unescapeJava(rawMessage.substring(3, rawMessage.length() - 2));
            JsonNode wsBody = mapper.readTree(stripped);
            args = wsBody.at("/args");
            method = wsBody.at("/method").asText();
        }

        public <T> T mapToObject(int argPosition, Class<T> clazz) throws JsonProcessingException {
            if(String.class.equals(clazz)) {
                String text = args.get(argPosition).asText();
                @SuppressWarnings("unchecked")
                T t = (T) text;
                return t;
            } else {
                return mapper.readValue(args.get(argPosition).asText(), clazz);
            }
        }
    }

    /**
     * Called after authorization.The default implementation joins the channel
     * specified in this instance's constructor. Unless you are familiar with
     * Chaturbate's WS protocol, do not override this method, or at least call
     * {@code super} so that the connection process to the chat is completed.
     *
     * @param websocket
     * @param message - raw text from websocket
     */
    protected void onAuthResponse(WebSocket websocket, String message) {
        if(!AUTH_SUCCESSFUL_MESSAGE.equals(message)) {
            LOG.warn("Unexpected message after connection: {}", message);
        }
        authenticated = true;
        websocket.sendText(String.format("[\"{\\\"method\\\":\\\"joinRoom\\\",\\\"data\\\":{\\\"room\\\":\\\"%s\\\"}}\"]", roomName));
    }

    /**
     * Called when a normal chat message is sent.
     *
     * @param message
     */
    protected void onRoomMsg(RoomMessage message) {

    }

    /**
     * Called on stream title change; empty strings are common because of
     * Chaturbate's implementation. Consider filtering out empty strings.
     *
     * @param newTitle
     */
    protected void onTitleChange(String newTitle) {

    }

    /**
     * Called when an announcement or tip is sent. See {@link Notification} for
     * more info.
     *
     * @param notification
     */
    protected void onNotify(Notification notification) {

    }

    /**
     * Called when a user is timed out by a moderator. One of these parameters
     * is the moderator and the other is the receiver of the timeout.
     *
     * @param arg0
     * @param arg1
     */
    protected void onSilence(String arg0, String arg1) {

    }

    /**
     * Called when a group show is cancelled.
     */
    protected void onNotifyGroupShowCancel() {

    }
}
