package com.plana.infli.web.dto.response.admin.verification.company;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadCompanyVerificationsResponse {

    private final List<CompanyVerificationImage> companyVerifications;

    @Builder
    private LoadCompanyVerificationsResponse(List<CompanyVerificationImage> companyVerifications) {

        this.companyVerifications = companyVerifications != null ?
                companyVerifications : new ArrayList<>();
    }

    public static LoadCompanyVerificationsResponse of(List<CompanyVerificationImage> companyVerifications) {
        return LoadCompanyVerificationsResponse.builder()
                .companyVerifications(companyVerifications)
                .build();
    }
}
