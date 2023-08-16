package com.plana.infli.repository.member;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.web.dto.response.member.verification.company.CompanyVerificationImage;
import com.plana.infli.web.dto.response.member.verification.student.StudentVerificationImage;
import java.util.List;
import java.util.Optional;

public interface MemberRepositoryCustom {

    boolean existsByVerifiedUniversityEmail(String universityEmail);

    boolean existsByUsername(String username);

    Optional<Member> findActiveMemberBy(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findActiveMemberWithUniversityBy(String username);

    Optional<Member> findActiveMemberWithUniversityBy(Long memberId);

    List<StudentVerificationImage> loadStudentVerificationImages(University university, int page);

    List<CompanyVerificationImage> loadCompanyVerificationImages(University university, int page);

    Optional<Member> findActiveMemberBy(Long memberId);

    Optional<Member> findActiveMemberWithCompanyBy(String username);

}
