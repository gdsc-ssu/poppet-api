package com.gdg.poppet.email.infra.template.v1;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TemplateV1Pos {
    USERNAME(273, 556, 18, new RGB(0.3f, 0.3f, 0.3f)),
    EMAIL_PERIOD(343, 522, 18, new RGB(0.3f, 0.3f, 0.3f)),
    CREATED_AT(308, 488, 18, new RGB(0.3f, 0.3f, 0.3f)),
    NOW(308, 454, 18, new RGB(0.3f, 0.3f, 0.3f)),
    SUMMARY(215, 299, 16, new RGB(0.5f, 0.5f, 0.5f));

    public final int x;
    public final int y;
    public final int fontSize;
    public final RGB color;
}