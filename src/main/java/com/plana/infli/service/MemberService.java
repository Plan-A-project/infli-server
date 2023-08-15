package com.plana.infli.service;

import static com.plana.infli.domain.Company.*;
import static com.plana.infli.domain.Member.*;
import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static com.plana.infli.exception.custom.BadRequestException.COMPANY_VERIFICATION_ALREADY_EXISTS;
import static com.plana.infli.exception.custom.BadRequestException.IMAGE_IS_EMPTY;
import static com.plana.infli.exception.custom.BadRequestException.NOT_MATCHES_PASSWORD_CONFIRM;
import static com.plana.infli.exception.custom.ConflictException.*;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.editor.MemberEditor;
import com.plana.infli.exception.custom.AuthorizationFailedException;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.utils.S3Uploader;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
import com.plana.infli.web.dto.response.member.verification.student.LoadStudentVerificationsResponse;
import com.plana.infli.web.dto.response.member.verification.student.StudentVerificationImage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder encoder;

    private final S3Uploader s3Uploader;

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
        return encoder.encode(password);
    }

    private void validateCreateStudentMemberRequest(CreateStudentMemberServiceRequest request) {

        checkPasswordConfirmMatch(request.getPassword(), request.getPasswordConfirm());
        checkUsernameDuplicate(request.getUsername());
        checkNicknameDuplicate(request.getNickname());
    }

    private void checkPasswordConfirmMatch(String password, String passwordConfirm) {
        if (password.equals(passwordConfirm) == false) {
            throw new BadRequestException(NOT_MATCHES_PASSWORD_CONFIRM);
        }
    }

    public void checkUsernameDuplicate(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new ConflictException(DUPLICATED_USERNAME);
        }
    }

    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
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
        checkUsernameDuplicate(request.getUsername());
    }

    @Transactional
    public void uploadCompanyCertificateImage(String username, MultipartFile file) {
        Member member = findMemberBy(username);

        validateUploadCompanyCertificateRequest(member, file);

        String directoryPath = "member/member_" + member.getId() + "/certificate/company";

        String imageUrl = s3Uploader.uploadAsOriginalImage(file, directoryPath);

        setVerificationStatusAsPendingByCompanyCertificate(member, imageUrl);
    }

    private void validateUploadCompanyCertificateRequest(Member member, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(COMPANY_VERIFICATION_ALREADY_EXISTS);
        }
    }

    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public void uploadUniversityCertificateImage(String username, MultipartFile file) {

        Member member = findMemberBy(username);

        validateUploadUniversityCertificateRequest(member, file);

        String directoryPath = "member/member_" + member.getId() + "/certificate/student";

        String imageUrl = s3Uploader.uploadAsOriginalImage(file, directoryPath);

        setVerificationStatusAsPendingByUniversityCertificate(member, imageUrl);
    }

    private void validateUploadUniversityCertificateRequest(Member member, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }

        if (member.getVerificationStatus() == SUCCESS) {
            throw new BadRequestException(COMPANY_VERIFICATION_ALREADY_EXISTS);
        }
    }

    public LoadStudentVerificationsResponse loadStudentVerificationRequest(String username,
            int page) {

        Member admin = findWithUniversityBy(username);

        if (isAdmin(admin) == false) {
            throw new AuthorizationFailedException();
        }

        List<StudentVerificationImage> verificationImages = memberRepository.loadStudentVerificationImages(
                admin.getUniversity(), page);

        return LoadStudentVerificationsResponse.of(20, page, verificationImages);
    }

    private Member findWithUniversityBy(String username) {
        return memberRepository.findActiveMemberWithUniversityBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    public void setStudentVerificationStatusAsSuccess(String username, Long memberId) {
        Member admin = findMemberBy(username);

        if (isAdmin(admin) == false) {
            throw new AuthorizationFailedException();
        }

        Member member = memberRepository.findActiveMemberBy(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

        setVerificationStatusAsSuccess(member);
    }
}
