package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.setVerificationStatusAsSuccess;
import static com.plana.infli.domain.type.VerificationStatus.PENDING;
import static com.plana.infli.infra.exception.custom.BadRequestException.MEMBER_VERIFICATION_STATUS_IS_NOT_PENDING;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.UNIVERSITY_NOT_FOUND;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.response.admin.member.LoadSignedUpStudentMembersResponse;
import com.plana.infli.web.dto.response.admin.member.SignedUpStudentMember;
import com.plana.infli.web.dto.response.admin.verification.company.CompanyVerificationImage;
import com.plana.infli.web.dto.response.admin.verification.company.LoadCompanyVerificationsResponse;
import com.plana.infli.web.dto.response.admin.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.dto.response.admin.verification.student.StudentVerificationImage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;

    private final UniversityRepository universityRepository;

    public LoadStudentVerificationsResponse loadCertificateUploadedStudentMembers(String username) {

        University university = findByUsername(username);

        List<StudentVerificationImage> verificationImages = memberRepository
                .loadStudentVerificationImages(university);

        return LoadStudentVerificationsResponse.of(verificationImages);
    }

    private University findByUsername(String username) {
        return universityRepository.findByMemberUsername(username)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
    }


    public LoadCompanyVerificationsResponse loadCertificateUploadedCompanyMembers(String username) {

        University university = findByUsername(username);

        List<CompanyVerificationImage> verificationImages = memberRepository
                .loadCompanyVerificationImages(university);

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

    private Member findWithUniversityBy(String username) {
        return memberRepository.findActiveMemberBy(username)
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

    public LoadSignedUpStudentMembersResponse loadSignedUpStudentMembers(
            String username, LocalDateTime joinedDateTime) {

        University university = findByUsername(username);

        List<SignedUpStudentMember> members =
                memberRepository.loadSignedUpStudentMember(university, joinedDateTime);

        return LoadSignedUpStudentMembersResponse.of(members);
    }
}
