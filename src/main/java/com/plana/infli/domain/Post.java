package com.plana.infli.domain;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Post extends BaseEntity {


    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String title;

    private String main;

    private boolean notice;

    private boolean isDeleted;

    @ManyToOne
    private Board board;

    //TODO 회원 컬럼

    private int viewCount;
}
