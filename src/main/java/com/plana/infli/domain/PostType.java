package com.plana.infli.domain;

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
