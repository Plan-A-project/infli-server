package com.plana.infli.domain.embedded.post;

import static lombok.AccessLevel.*;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PROTECTED)
@Embeddable
@Getter
public class Recruitment {

    private String companyName;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Builder
    private Recruitment(String companyName, LocalDateTime startDate, LocalDateTime endDate) {
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public static Recruitment create(String companyName, LocalDateTime startDate,
            LocalDateTime endDate) {

        return Recruitment.builder()
                .companyName(companyName)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
