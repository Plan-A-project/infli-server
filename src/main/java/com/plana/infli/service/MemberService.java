package com.plana.infli.service;

import static com.plana.infli.domain.Company.*;
import static com.plana.infli.domain.Member.*;
import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.VerificationStatus.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.COMPANY_VERIFICATION_ALREADY_EXISTS;
import static com.plana.infli.infra.exception.custom.BadRequestException.IMAGE_IS_EMPTY;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_NICKNAME;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_USERNAME;
import static com.plana.infli.infra.exception.custom.BadRequestException.MEMBER_VERIFICATION_STATUS_IS_NOT_PENDING;
import static com.plana.infli.infra.exception.custom.BadRequestException.NOT_MATCHES_PASSWORD_CONFIRM;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_NICKNAME;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_USERNAME;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.infra.exception.custom.NotFoundException.UNIVERSITY_NOT_FOUND;

import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.domain.editor.MemberEditor;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.type.VerificationStatus;
import com.plana.infli.infra.exception.custom.AuthorizationFailedException;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.utils.S3Uploader;
import com.plana.infli.web.dto.request.member.signup.company.CreateCompanyMemberServiceRequest;
import com.plana.infli.web.dto.request.member.signup.student.CreateStudentMemberServiceRequest;
import com.plana.infli.web.dto.response.member.verification.company.CompanyVerificationImage;
import com.plana.infli.web.dto.response.member.verification.company.LoadCompanyVerificationsResponse;
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

    public static final String USERNAME_REGEX = "^[a-z0-9_-]{5,20}$";

    public static final String REAL_NAME_REGEX = "^[가-힣]{2,10}$";

    public static final String NICKNAME_REGEX = "^[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,8}$";

    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$";

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
        checkIsValidUsername(request.getUsername());
        checkIsValidNickname(request.getNickname());
    }

    private void checkPasswordConfirmMatch(String password, String passwordConfirm) {
        if (password.equals(passwordConfirm) == false) {
            throw new BadRequestException(NOT_MATCHES_PASSWORD_CONFIRM);
        }
    }

    public boolean checkIsValidUsername(String username) {
        if (username.matches(USERNAME_REGEX) == false) {
            throw new BadRequestException(INVALID_USERNAME);
        }

        if (memberRepository.existsByUsername(username)) {
            throw new ConflictException(DUPLICATED_USERNAME);
        }

        return true;
    }

    public boolean checkIsValidNickname(String nickname) {
        if (nickname.matches(NICKNAME_REGEX) == false) {
            throw new BadRequestException(INVALID_NICKNAME);
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }

        return true;
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
        checkIsValidUsername(request.getUsername());
    }

    @Transactional
    public void uploadCompanyCertificateImage(String username, MultipartFile file) {
        Member member = findMemberBy(username);

        validateUploadCompanyCertificateRequest(member, file);

        String directoryPath = "members/" + member.getId() + "/certificate/company";

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

        String directoryPath = "members/" + member.getId() + "/certificate/student";

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

    public LoadStudentVerificationsResponse loadStudentVerificationRequestImages(String username) {

        Member admin = findWithUniversityBy(username);

        if (isAdmin(admin) == false) {
            throw new AuthorizationFailedException();
        }

        List<StudentVerificationImage> verificationImages = memberRepository.loadStudentVerificationImages(
                admin.getUniversity());

        return LoadStudentVerificationsResponse.of(verificationImages);
    }

    private Member findWithUniversityBy(String username) {
        return memberRepository.findActiveMemberWithUniversityBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    private Member findWithUniversityBy(Long memberId) {
        return memberRepository.findActiveMemberWithUniversityBy(memberId)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    @Transactional
    public void setMemberVerificationStatusAsSuccess(String username, Long memberId) {
        Member admin = findWithUniversityBy(username);

        Member member = findWithUniversityBy(memberId);

        checkIsInSameUniversity(admin, member);
        checkVerificationStatusIsPending(member);

        setVerificationStatusAsSuccess(member);
    }

    private void checkVerificationStatusIsPending(Member member) {
        if (member.getVerificationStatus() != PENDING) {
            throw new BadRequestException(MEMBER_VERIFICATION_STATUS_IS_NOT_PENDING);
        }
    }

    private void checkIsInSameUniversity(Member admin, Member member) {
        if (admin.getUniversity().equals(member.getUniversity()) == false) {
            throw new AuthorizationFailedException();
        }
    }

    public LoadCompanyVerificationsResponse loadCompanyVerificationRequestImages(String username) {

        Member admin = findWithUniversityBy(username);

        if (isAdmin(admin) == false) {
            throw new AuthorizationFailedException();
        }

        List<CompanyVerificationImage> verificationImages = memberRepository.loadCompanyVerificationImages(
                admin.getUniversity());

        return LoadCompanyVerificationsResponse.of(verificationImages);
    }

    public boolean checkMemberAcceptedPolicy(String username) {
        BasicCredentials basicCredentials = findMemberBy(username).getBasicCredentials();

        return basicCredentials.isPolicyAccepted();
    }

    @Transactional
    public void acceptPolicy(String username) {
        Member member = findMemberBy(username);

        MemberEditor.acceptPolicy(member);
    }

    public VerificationStatus loadVerificationStatus(String username) {
        return findMemberBy(username).getVerificationStatus();
    }
}
