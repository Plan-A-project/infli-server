package com.plana.infli.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class University extends BaseEntity{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "university_id")
    private Long id;

    @Column(unique = true)
    private String name;

    private boolean isDeleted = false;

    @Builder
    private University(String name) {
        this.name = name;
    }

    public static University create(String name) {
        return University.builder()
                .name(name)
                .build();
    }
}
