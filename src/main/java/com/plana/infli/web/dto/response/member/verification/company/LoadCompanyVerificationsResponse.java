package com.plana.infli.web.dto.response.member.verification.company;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCompanyVerificationsResponse {

    private final List<CompanyVerificationImage> studentVerifications;

    @Builder
    private LoadCompanyVerificationsResponse(List<CompanyVerificationImage> studentVerifications) {

        this.studentVerifications = studentVerifications != null ?
                studentVerifications : new ArrayList<>();
    }

    public static LoadCompanyVerificationsResponse of(List<CompanyVerificationImage> studentVerifications) {
        return LoadCompanyVerificationsResponse.builder()
                .studentVerifications(studentVerifications)
                .build();
    }
}
