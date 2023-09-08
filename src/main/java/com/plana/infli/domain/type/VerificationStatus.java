package com.plana.infli.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus {

    PENDING("관리자 승인중"),
    SUCCESS("인증 완료"),
    FAILED("인증 실패"),
    NOT_STARTED("미인증 상태"),
    ;

    private final String value;
}
