package com.plana.infli.domain.embedded.member;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class StudentCredentials {

    private String realName;

    private String universityEmail;

    private String universityCertificateUrl;

    @Builder
    private StudentCredentials(String realName, String universityEmail,
            String universityCertificateUrl) {

        this.realName = realName;
        this.universityEmail = universityEmail;
        this.universityCertificateUrl = universityCertificateUrl;
    }


    private static StudentCredentials of(String realName, String universityEmail, String universityCertificateUrl) {
        return StudentCredentials.builder()
                .realName(realName)
                .universityEmail(universityEmail)
                .universityCertificateUrl(universityCertificateUrl)
                .build();
    }


    public static StudentCredentials ofDefault(String realName) {
        return of(realName, null, null);
    }


    public static StudentCredentials ofWithEmail(StudentCredentials studentCredentials,
            String universityEmail) {

        return of(studentCredentials.realName, universityEmail, null);
    }

    public static StudentCredentials ofWithCertificate(String realName, String certificateUrl) {
        return of(realName, null, certificateUrl);
    }
}
