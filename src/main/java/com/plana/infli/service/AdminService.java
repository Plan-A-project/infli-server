package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.setVerificationStatusAsSuccess;
import static com.plana.infli.domain.type.VerificationStatus.PENDING;
import static com.plana.infli.infra.exception.custom.BadRequestException.MEMBER_VERIFICATION_STATUS_IS_NOT_PENDING;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;

import com.plana.infli.domain.Member;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.web.dto.response.member.verification.company.CompanyVerificationImage;
import com.plana.infli.web.dto.response.member.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.member.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.dto.response.member.verification.student.StudentVerificationImage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;

    public LoadStudentVerificationsResponse loadStudentVerificationRequestImages(String username) {

        Member admin = findWithUniversityBy(username);

        List<StudentVerificationImage> verificationImages = memberRepository
                .loadStudentVerificationImages(admin.getUniversity());

        return LoadStudentVerificationsResponse.of(verificationImages);
    }

    private Member findWithUniversityBy(String username) {
        return memberRepository.findActiveMemberWithUniversityBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    public LoadCompanyVerificationsResponse loadCompanyVerificationRequestImages(String username) {

        Member admin = findWithUniversityBy(username);

        List<CompanyVerificationImage> verificationImages = memberRepository.loadCompanyVerificationImages(
                admin.getUniversity());

        return LoadCompanyVerificationsResponse.of(verificationImages);
    }

    @Transactional
    public void setStatusAsVerifiedMember(String username, Long memberId) {

        Member admin = findWithUniversityBy(username);

        Member member = findWithUniversityBy(memberId);

        checkIsInSameUniversity(admin, member);

        checkVerificationStatusIsPending(member);

        setVerificationStatusAsSuccess(member);
    }

    private Member findWithUniversityBy(Long memberId) {
        return memberRepository.findActiveMemberWithUniversityBy(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private void checkIsInSameUniversity(Member admin, Member member) {
        if (admin.getUniversity().equals(member.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    private void checkVerificationStatusIsPending(Member member) {
        if (member.getVerificationStatus() != PENDING) {
            throw new BadRequestException(MEMBER_VERIFICATION_STATUS_IS_NOT_PENDING);
        }
    }
}
