package org.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * This bean simply wraps Jackson functionality to write object as a JSON string.
 * */
@Component
public class JsonWriter {

    public String writeToString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize object", e);
        }
    }
}