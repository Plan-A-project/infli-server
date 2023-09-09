package com.plana.infli.web.dto.response.admin.member;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoadSignedUpStudentMembersResponse {


    private final List<SignedUpStudentMember> members;

    @Builder
    public LoadSignedUpStudentMembersResponse(List<SignedUpStudentMember> members) {
        this.members = members;
    }

    public static LoadSignedUpStudentMembersResponse of(List<SignedUpStudentMember> members) {
        return LoadSignedUpStudentMembersResponse.builder()
                .members(members)
                .build();
    }
}
