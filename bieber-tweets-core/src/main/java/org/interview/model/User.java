package org.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {

    @JsonProperty("id_str")
    private final String id;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("screen_name")
    private final String screenName;

}
