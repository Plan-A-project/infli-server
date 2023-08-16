package com.plana.infli.domain;

import static java.util.UUID.*;
import static lombok.AccessLevel.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_verification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String universityEmail;

    private String code;

    private LocalDateTime codeGeneratedTime;

    @Builder
    private EmailVerification(Member member, String universityEmail,
            String code, LocalDateTime codeGeneratedTime) {

        this.universityEmail = universityEmail;
        this.member = member;
        this.code = code;
        this.codeGeneratedTime = codeGeneratedTime;
    }

    public static EmailVerification create(Member member,
            LocalDateTime codeGeneratedTIme, String universityEmail) {

        return builder()
                .member(member)
                .code(randomUUID().toString())
                .universityEmail(universityEmail)
                .codeGeneratedTime(codeGeneratedTIme)
                .build();
    }
}

