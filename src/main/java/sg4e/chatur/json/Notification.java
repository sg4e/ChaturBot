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
package sg4e.chatur.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.awt.Color;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * A multipurpose model for most notices in chat, including tips and
 * announcements.
 * <p>
 * The structure of this model is very haphazard. Rather than create separate
 * models for different types of messages, Chaturbate opted for using the
 * {@code onNotify} type for almost everything. This class merely reflects
 * Chaturbate's design.
 * <p>
 * Many fields will remain null. The fields that remain null depend on the
 * notice type (tips, announcements, etc.). For some notices, almost all fields
 * are null (e.g. {@code refresh_panel}). The {@code type} fields seems to
 * always be non-null and helps distinguish between notices.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Setter(AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification extends TippingMetadata {

    private boolean isMod, history;
    private String toUsername, message, dontSendTo, fromUsername, sendTo, type, weight;
    private Color foreground, background;
    private List<String> msg;
    private int amount;

    @JsonProperty("foreground")
    private void unpackForeground(String c) {
        foreground = RoomMessage.readColor(c);
    }

    @JsonProperty("background")
    private void unpackBackground(String c) {
        background = RoomMessage.readColor(c);
    }
}
