package com.gdg.poppet.chat.infra.gemini.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiResponseDto {

    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
    }

    @Data
    public static class Content {
        private List<Parts> parts;
        private String role;
    }

    @Data
    public static class Parts {
        private String text;
    }
}
