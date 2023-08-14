package com.plana.infli.domain;

import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private boolean isDeleted;

    @Builder
    private Company(String name) {
        this.name = name;
        this.isDeleted = false;
    }

    public static Company create(String name) {
        return Company.builder()
                .name(name)
                .build();
    }
}
