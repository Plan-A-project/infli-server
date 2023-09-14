package com.plana.infli.repository.member;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.type.Role;
import com.plana.infli.web.dto.response.admin.member.SignedUpStudentMember;
import com.plana.infli.web.dto.response.admin.verification.company.CompanyVerificationImage;
import com.plana.infli.web.dto.response.admin.verification.student.StudentVerificationImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepositoryCustom {

    boolean existsByVerifiedUniversityEmail(String universityEmail);

    boolean existsByUsername(String username);

    boolean existsByRole(Role role);

    Optional<Member> findActiveMemberBy(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findActiveMemberWithUniversityBy(String username);

    Optional<Member> findActiveMemberWithUniversityBy(Long memberId);

    List<StudentVerificationImage> loadStudentVerificationImages(University university);

    List<CompanyVerificationImage> loadCompanyVerificationImages(University university);

    Optional<Member> findActiveMemberBy(Long memberId);

    Optional<Member> findActiveMemberWithCompanyBy(String username);

    Optional<Member> findDeletedMemberBy(Long memberId);

    List<SignedUpStudentMember> loadSignedUpStudentMember(
            University university, LocalDateTime localDateTime);
}
