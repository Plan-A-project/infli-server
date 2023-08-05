package com.plana.infli.domain;

import static com.plana.infli.domain.Role.*;
import static java.util.List.*;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostType {

    NORMAL("일반"),

    RECRUITMENT("모집"),

    ANNOUNCEMENT("공지"),
    ;

    private final String name;

}
