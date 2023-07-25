package com.plana.infli.web.dto.request.post.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PostViewOrder {

    recent("최신순"),

    popular("인기순"),
            ;

    private final String value;
}
