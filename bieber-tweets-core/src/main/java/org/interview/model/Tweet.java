package org.interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tweet {

    @JsonProperty("id_str")
    private final String id;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("text")
    private final String text;

    @JsonProperty("user")
    private final User user;

}
