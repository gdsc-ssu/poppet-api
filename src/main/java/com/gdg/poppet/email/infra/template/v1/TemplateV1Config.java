package com.gdg.poppet.email.infra.template.v1;

public record TemplateV1Config(String BACKGROUND_FILE_NAME,
                               String FONT_NAME,
                               int LINE_HEIGHT,
                               int MAX_CHARS_IN_LINE,
                               int DEFAULT_FONT_SIZE,
                               RGB DEFAULT_TEXT_COLOR) {
}