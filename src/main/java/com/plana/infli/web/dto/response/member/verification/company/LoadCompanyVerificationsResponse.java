package com.plana.infli.web.dto.response.member.verification.company;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCompanyVerificationsResponse {

    private final int sizeRequest;

    private final int currentPage;

    private final List<CompanyVerificationImage> studentVerifications;

    @Builder
    private LoadCompanyVerificationsResponse(int sizeRequest, int currentPage,
            List<CompanyVerificationImage> studentVerifications) {

        this.sizeRequest = sizeRequest;
        this.currentPage = currentPage;
        this.studentVerifications = studentVerifications != null ?
                studentVerifications : new ArrayList<>();
    }

    public static LoadCompanyVerificationsResponse of(int sizeRequest, int currentPage,
            List<CompanyVerificationImage> studentVerifications) {

        return LoadCompanyVerificationsResponse.builder()
                .sizeRequest(sizeRequest)
                .currentPage(currentPage)
                .studentVerifications(studentVerifications)
                .build();
    }
}
