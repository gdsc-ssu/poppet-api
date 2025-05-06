package com.gdg.poppet.auth.application.dto.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleExtraProfile {
    private List<GenderWrapper> genders;
    private List<BirthdayWrapper> birthdays;

    @Data public static class GenderWrapper {
        private String value;    // e.g. "male", "female", "other"
    }
    @Data public static class BirthdayWrapper {
        private Integer year;
        private Integer month;
        private Integer day;
    }
}

