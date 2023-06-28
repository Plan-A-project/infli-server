package com.plana.infli.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostType {
    NORMAL("normal"),
    GATHER("gather"),
    NOTICE("notice");

    private final String name;

    public static PostType of(String type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        for (PostType o : PostType.values()) {
            if (o.name.equals(type)) {
                return o;
            }
        }

        throw new IllegalArgumentException("일치하는 이름이 없습니다.");
    }
}
