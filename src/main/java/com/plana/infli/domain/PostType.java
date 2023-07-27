package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;
import static java.util.List.*;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostType {

    NORMAL("일반", of(STUDENT, COMPANY, STUDENT_COUNCIL, ADMIN)),

    RECRUITMENT("모집", of(ADMIN, COMPANY)),

    ANNOUNCEMENT("공지", of(ADMIN, STUDENT_COUNCIL)),
    ;

    private final String name;

    private final List<Role> allowedRoll;


    public boolean hasWritePermission(Role role) {
        return allowedRoll.contains(role);
    }

}
