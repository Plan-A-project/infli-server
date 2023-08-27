package com.plana.infli.web.dto.response.member.verification.student;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadStudentVerificationsResponse {

    private final List<StudentVerificationImage> studentVerifications;

    @Builder
    private LoadStudentVerificationsResponse(List<StudentVerificationImage> studentVerifications) {

        this.studentVerifications = studentVerifications != null ?
                studentVerifications : new ArrayList<>();
    }

    public static LoadStudentVerificationsResponse of(
            List<StudentVerificationImage> studentVerifications) {

        return LoadStudentVerificationsResponse.builder()
                .studentVerifications(studentVerifications)
                .build();
    }
}
