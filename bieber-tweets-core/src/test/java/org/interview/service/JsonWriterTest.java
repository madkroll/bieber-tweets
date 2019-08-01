package org.interview.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonWriterTest {

    @Test
    public void shouldReturnObjectAsJsonString() {
        final SampleModel sampleModel = new SampleModel("sample-model-field");
        final String jsonContent = new JsonWriter().writeToString(sampleModel);
        assertThat(jsonContent).isEqualToIgnoringWhitespace("{\"sampleField\":\"sample-model-field\"}");
    }

    @Test
    public void shouldFail() {
        assertThatThrownBy(() -> new JsonWriter().writeToString(new Object()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to serialize object");
    }

    @Getter
    @AllArgsConstructor
    private static class SampleModel {

        @JsonProperty
        private final String sampleField;

    }
}