package com.plana.infli.service;

import static com.plana.infli.exception.custom.ConflictException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.member.signup.CompanyMemberCreateRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  private final UniversityRepository universityRepository;

  @Transactional
  public void signupMember(CreateStudentMemberRequest request) {
    checkEmailDuplicated(request.getEmail());
    checkNicknameDuplicated(request.getNickname());

    University university = universityRepository.findById(request.getUniversityId())
        .orElseThrow(() -> new NotFoundException(NotFoundException.UNIVERSITY_NOT_FOUND));

    Member member = new Member(request.getEmail(), request.getPassword(), request.getName(),
        request.getNickname(), university, passwordEncoder);

    memberRepository.save(member);
  }

  public void checkEmailDuplicated(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new ConflictException(DUPLICATED_EMAIL);
    }
  }

  public void checkNicknameDuplicated(String email) {
    if (memberRepository.existsByNickname(email)) {
      throw new ConflictException(DUPLICATED_NICKNAME);
    }
  }

  @Transactional
  public void signupCompanyMember(CompanyMemberCreateRequest request) {
    checkEmailDuplicated(request.getEmail());

    Member member = new Member(request.getEmail(), request.getPassword(),
        request.getCompanyName(), passwordEncoder);

    memberRepository.save(member);
  }
}
