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
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Parent class for models that containing tipping data.
 */
@Data
@Setter(AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TippingMetadata {

    @Getter(AccessLevel.NONE)
    private boolean hasTokens, tippedAlotRecently, tippedTonsRecently, tippedRecently;
    private boolean inFanclub;

    public boolean hasTokens() {
        return hasTokens;
    }

    public boolean hasTippedAlotRecently() {
        return tippedAlotRecently;
    }

    public boolean hasTippedTonsRecently() {
        return tippedTonsRecently;
    }

    public boolean hasTippedRecently() {
        return tippedRecently;
    }
}
