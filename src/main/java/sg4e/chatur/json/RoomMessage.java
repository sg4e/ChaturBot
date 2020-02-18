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
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * Model for normal chat messages (not tips or announcements).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Setter(AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomMessage extends TippingMetadata {

    private Color color;
    @JsonProperty("X-Successful")
    private boolean xSuccessful;
    private boolean isMod;
    private String gender, user;
    @JsonProperty("f")
    private String font;
    @JsonProperty("m")
    private String message;

    @JsonProperty("c")
    private void unpackColor(String c) {
        color = readColor(c);
    }

    static Color readColor(String c) {
        if(c.startsWith("#")) {
            //#494949
            return Color.decode(c);
        }
        else {
            //rgb(73,73,73)
            String[] parts = c.split(",");
            return new Color((Integer.parseInt(parts[0].split("\\(")[1])), Integer.parseInt(parts[1]), Integer.parseInt(parts[2].substring(0, parts[2].length() - 1)));
        }
    }
}
