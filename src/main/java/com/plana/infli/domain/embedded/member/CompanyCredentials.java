package com.plana.infli.domain.embedded.member;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.*;

import com.plana.infli.domain.Company;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class CompanyCredentials {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Nullable
    private String companyCertificateUrl;

    @Builder
    private CompanyCredentials(Company company, @Nullable String companyCertificateUrl) {
        this.company = company;
        this.companyCertificateUrl = companyCertificateUrl;
    }

    private static CompanyCredentials of(Company company, String companyCertificateUrl) {
        return CompanyCredentials.builder()
                .company(company)
                .companyCertificateUrl(companyCertificateUrl)
                .build();
    }

    public static CompanyCredentials ofDefault(Company company) {
        return of(company, null);
    }

    public static CompanyCredentials ofWithCertificate(Company company,
            String companyCertificateUrl) {

        return of(company, companyCertificateUrl);
    }
}
