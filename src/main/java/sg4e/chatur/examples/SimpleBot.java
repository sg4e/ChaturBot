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
package sg4e.chatur.examples;

import com.neovisionaries.ws.client.WebSocketException;
import java.io.IOException;
import sg4e.chatur.ChaturBot;
import sg4e.chatur.json.Notification;
import sg4e.chatur.json.RoomMessage;

/**
 * Simple example of how to use the library. Prints basic messages to stdout.
 */
public class SimpleBot extends ChaturBot {

    public SimpleBot(String roomName, String URL, String connectAuth) throws IOException, WebSocketException {
        super(roomName, URL, connectAuth);
    }

    @Override
    protected void onRoomMsg(RoomMessage message) {
        System.out.println(message.getUser() + ": " + message.getMessage());
    }

    @Override
    protected void onTitleChange(String newTitle) {
        System.out.println("New title: " + newTitle);
    }

    @Override
    protected void onNotify(Notification notification) {
        if(notification.getAmount() != 0) {
            System.out.println(notification.getFromUsername() + " tipped " + notification.getAmount());
        }
        else if(notification.getMsg() != null) {
            System.out.println("Announcement: " + notification.getMsg());
        }
        // else ignore; there are a lot of meaningless notifications that simply tell the client to refresh a UI element.
    }

    @Override
    protected void onSilence(String arg0, String arg1) {
        System.out.println("Silence: " + arg0 + " > " + arg1);
    }

    public static void main(String[] args) throws Exception {
        SimpleBot bot = new SimpleBot(args[0], args[1], args[2]);
        bot.start();
    }
}
