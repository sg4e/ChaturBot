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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Number of users in the chat. This class encapsulates the asynchronous query.
 * You must check that {@link #hasServerResponse()} is true and
 * {@link #hasError()} is false before calling {@link #get()} to be sure that
 * the query has been successful. It is also possible for queries to fail
 * indefinitely, so it is advisable to run the query again via
 * {@link ChaturBot#getRoomCount()} and handle the new object if the current
 * object fails to receive a server response after a few seconds.
 * <p>
 * {@code RoomCount} objects are updated for only one query. Subsequent calls to
 * {@link ChaturBot#getRoomCount()} will update only the new object, not
 * previous {@code RoomCount}s.
 */
public class RoomCount {

    private final AtomicInteger count;
    private volatile boolean hasServerResponse;
    private volatile Throwable error;

    RoomCount() {
        count = new AtomicInteger(-1);
        hasServerResponse = false;
        error = null;
    }

    /**
     * Returns the response from the server. Returns -1 if no response has been
     * received yet. May also return -1 if the room is offline.
     *
     * @return
     */
    public int get() {
        return count.get();
    }

    void set(int value) {
        count.set(value);
    }

    public boolean hasServerResponse() {
        return hasServerResponse;
    }

    void setServerResponse() {
        hasServerResponse = true;
    }

    public boolean hasError() {
        return error != null;
    }

    public Throwable getError() {
        return error;
    }

    void setError(Throwable error) {
        hasServerResponse();
        this.error = error;
    }

}
