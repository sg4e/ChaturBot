# ChaturBot

WebSocket library for connecting to Chaturbate chat.

By sg4e.

## How to use

Gradle (with Jitpack):
```groovy
repositories {
  // ...
  maven { url 'https://jitpack.io' }
}

dependencies {
  // ...
  compile 'com.github.sg4e:ChaturBot:master-SNAPSHOT'
}
```

Fatjars are also available under [*Releases*](https://github.com/sg4e/ChaturBot/releases).

### Getting Started

This library requires Java 8 or above.

There are 3 strings required to connect to chat. The 1st is simply the Chaturbate user whose chat you are connecting to. The 2nd and 3rd must be retrieved while on the channel page with the chat you wish to connect to, opening your browser's devtools, and refreshing the tab/window. You do not need to be logged in to acquire these strings (which act as a basic authenticator for the WS protocol).

- String 1: Username of Chaturbate chat to connect to
- String 2: WebSocket URL
- String 3: Authorization message (right-click and copy its entirety. Begins with `["{\"method\":\"connect\"`)

#### Acquiring String 2
![Screenshot for String 2](https://raw.githubusercontent.com/sg4e/ChaturBot/master/screenshots/2.png)

#### Acquiring String 3
![Screenshot for String 3](https://raw.githubusercontent.com/sg4e/ChaturBot/master/screenshots/3.png)

### Javadoc

https://maika.moe/chaturbot/javadoc/index.html

### Using as a library

ChaturBot exposes a simple subclass API.

```java
public class MyBot extends ChaturBot {
  public MyBot(String roomName, String URL, String connectAuth) throws IOException, WebSocketException {
    super(roomName, URL, connectAuth);
  }

  @Override
  protected void onRoomMsg(RoomMessage message) {
    System.out.println(message.getUser() + ": " + message.getMessage());
  }

  public static void main(String[] args) throws Exception {
    MyBot bot = new MyBot(/* The 3 Strings you obtained in Getting Started */);
    bot.start();
  }
}
```

A more detailed example is available in the [`examples`](https://github.com/sg4e/ChaturBot/tree/master/src/main/java/sg4e/chatur/examples) directory.

### Using as a command-line tool

This library also contains a basic command-line program to print chat to standard output. You can do this either with gradle or the JAR binary.

Gradle:
```sh
gradle run --args='string1 string2 string3'
```

JAR:
```sh
java -jar ChaturBot.jar 'string1' 'string2' 'string3'
```

## Disclaimer

Chaturbate's WebSocket chat interface is not a public API. Chaturbate may change its WS interface at any time without warning. Likewise, the API exposed in this library may have to change to keep up, and instability will be unavoidable.

## TO-DO
*PRs welcome*

- Add emoticon parsing to `RoomMessage`
- Implement connecting to chat with only the model's Chaturbate username (ideally without running any JS)
- Allow user to make the bot run and send messages over their own Chaturbate account credential
- Implement update room status
