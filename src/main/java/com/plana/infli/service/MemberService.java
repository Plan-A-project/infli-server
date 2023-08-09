package com.plana.infli.service;

import static com.plana.infli.domain.Company.*;
import static com.plana.infli.exception.custom.BadRequestException.NOT_MATCHES_PASSWORD_CONFIRM;
import static com.plana.infli.exception.custom.ConflictException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
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

    private final CompanyRepository companyRepository;

    @Transactional
    public Long signupAsStudentMember(CreateStudentMemberServiceRequest request) {

        validateCreateStudentMemberRequest(request);

        University university = findUniversityBy(request.getUniversityId());

        Member member = request.toEntity(university, encodePassword(request.getPassword()));

        return memberRepository.save(member).getId();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void validateCreateStudentMemberRequest(CreateStudentMemberServiceRequest request) {
        checkPasswordConfirmMatch(request.getPassword(), request.getPasswordConfirm());
        checkEmailDuplicate(request.getEmail());
        checkNicknameDuplicated(request.getNickname());
    }

    private void checkPasswordConfirmMatch(String password, String passwordConfirm) {
        if (password.equals(passwordConfirm) == false) {
            throw new BadRequestException(NOT_MATCHES_PASSWORD_CONFIRM);
        }
    }

    public void checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new ConflictException(DUPLICATED_EMAIL);
        }
    }

    public void checkNicknameDuplicated(String email) {
        if (memberRepository.existsByNickname(email)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }
    }

    private University findUniversityBy(Long universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
    }

    @Transactional
    public Long signupAsCompanyMember(CreateCompanyMemberServiceRequest request) {

        validateCreateCompanyMemberRequest(request);

        Company company = findCompanyOrCreateBy(request.getCompanyName());

        University university = findUniversityBy(request.getUniversityId());

        Member member = request.toEntity(company, encodePassword(request.getPassword()),
                university);

        return memberRepository.save(member).getId();
    }

    private Company findCompanyOrCreateBy(String companyName) {
        return companyRepository.findByName(companyName)
                .orElseGet(() -> companyRepository.save(create(companyName)));
    }

    private void validateCreateCompanyMemberRequest(CreateCompanyMemberServiceRequest request) {
        checkPasswordConfirmMatch(request.getPassword(), request.getPasswordConfirm());
        checkEmailDuplicate(request.getEmail());
    }

}
