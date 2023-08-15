package com.plana.infli.web.dto.response.member.verification.student;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadStudentVerificationsResponse {


    private final int sizeRequest;

    private final int currentPage;

    private final List<StudentVerificationImage> studentVerifications;

    @Builder
    private LoadStudentVerificationsResponse(int sizeRequest, int currentPage,
            List<StudentVerificationImage> studentVerifications) {

        this.sizeRequest = sizeRequest;
        this.currentPage = currentPage;
        this.studentVerifications = studentVerifications;
    }

    public static LoadStudentVerificationsResponse of(int sizeRequest, int currentPage,
            List<StudentVerificationImage> studentVerifications) {

        return LoadStudentVerificationsResponse.builder()
                .sizeRequest(sizeRequest)
                .currentPage(currentPage)
                .studentVerifications(studentVerifications)
                .build();
    }
}
