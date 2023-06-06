package com.plana.infli.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
