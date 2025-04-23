package com.gdg.poppet.email.infra.template.v1;

import lombok.Getter;

@Getter
public class TemplateV1 {
    private final TemplateV1Config config;

    public TemplateV1() {
        this.config = new TemplateV1Config(
                "email_background.pdf",
                "Pretendard.ttf",
                50,
                45,
                18,
                new RGB(0.3f, 0.3f, 0.3f));
    }
}
