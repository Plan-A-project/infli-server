package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.infra.exception.custom.BadRequestException.IMAGE_IS_EMPTY;
import static com.plana.infli.infra.exception.custom.BadRequestException.INVALID_NICKNAME;
import static com.plana.infli.infra.exception.custom.BadRequestException.NOT_MATCHES_NEW_PASSWORD_CONFIRM;
import static com.plana.infli.infra.exception.custom.BadRequestException.PASSWORD_NOT_MATCH;
import static com.plana.infli.infra.exception.custom.BadRequestException.UNREGISTER_NOT_ALLOWED;
import static com.plana.infli.infra.exception.custom.ConflictException.DUPLICATED_NICKNAME;
import static com.plana.infli.infra.exception.custom.NotFoundException.MEMBER_NOT_FOUND;
import static com.plana.infli.service.MemberService.NICKNAME_REGEX;
import static org.springframework.security.core.context.SecurityContextHolder.*;
import static org.springframework.util.StringUtils.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.type.Role;
import com.plana.infli.infra.exception.custom.BadRequestException;
import com.plana.infli.infra.exception.custom.ConflictException;
import com.plana.infli.infra.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.service.aop.upload.Upload;
import com.plana.infli.service.utils.S3Uploader;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordServiceRequest;
import com.plana.infli.web.dto.response.profile.MyProfileResponse;
import com.plana.infli.web.dto.response.profile.MyProfileToUnregisterResponse;
import com.plana.infli.web.dto.response.profile.image.ChangeProfileImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SettingService {

    private final MemberRepository memberRepository;

    private final S3Uploader s3Uploader;

    private final PasswordEncoder passwordEncoder;

    public MyProfileResponse loadMyProfile(String username) {

        Member member = findMemberBy(username);

        return MyProfileResponse.of(member);
    }

    private Member findMemberBy(String username) {
        return memberRepository.findActiveMemberBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

    public String checkIsAvailableNewNickname(String nickname) {
        if (nickname.matches(NICKNAME_REGEX) == false) {
            throw new BadRequestException(INVALID_NICKNAME);
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }

        return "사용 가능한 닉네임";
    }

    @Transactional
    public void changeNickname(String username, String newNickname) {

        Member member = findMemberBy(username);

        checkIsAvailableNewNickname(newNickname);

        editNickname(member, newNickname);
    }

    public String verifyCurrentPassword(String username, String currentPassword) {

        Member member = findMemberBy(username);

        return checkPasswordMatches(member, currentPassword);
    }

    private String checkPasswordMatches(Member member, String password) {

        if (passwordEncoder.matches(password, member.getLoginCredentials().getPassword())) {
            return "비밀번호 일치";
        }

        throw new BadRequestException(PASSWORD_NOT_MATCH);
    }

    @Transactional
    public void changePassword(ModifyPasswordServiceRequest request) {

        Member member = findMemberBy(request.getUsername());

        validateModifyPasswordRequest(member, request);

        String encryptedPassword = encryptNewPassword(request.getNewPassword());

        editPassword(member, encryptedPassword);
    }


    private void validateModifyPasswordRequest(Member member,
            ModifyPasswordServiceRequest request) {

        checkPasswordMatches(member, request.getCurrentPassword());

        checkPasswordConfirmMatches(request);
    }

    private static void checkPasswordConfirmMatches(ModifyPasswordServiceRequest request) {

        String newPassword = request.getNewPassword();

        String newPasswordConfirm = request.getNewPasswordConfirm();

        if (newPassword.equals(newPasswordConfirm) == false) {
            throw new BadRequestException(NOT_MATCHES_NEW_PASSWORD_CONFIRM);
        }
    }

    private String encryptNewPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    @Transactional
    @Upload
    public ChangeProfileImageResponse changeProfileImage(String username,
            MultipartFile multipartFile) {

        Member member = findMemberBy(username);

        checkIsValidFile(multipartFile);

        String path = "members/" + member.getId();

        String originalUrl = s3Uploader.uploadAsOriginalImage(multipartFile, path);

        String thumbnailUrl = s3Uploader.uploadAsThumbnailImage(multipartFile, path);

        ProfileImage newProfileImage = ProfileImage.of(originalUrl, thumbnailUrl);

        editProfileImage(member, newProfileImage);

        return ChangeProfileImageResponse.of(newProfileImage);
    }

    private void checkIsValidFile(MultipartFile file) {
        if (file.isEmpty() || hasText(file.getOriginalFilename()) == false) {
            throw new BadRequestException(IMAGE_IS_EMPTY);
        }
    }

    @Transactional
    public void unregisterMember(String username, String password) {
        Member member = findMemberBy(username);

        validateUnregisterRequest(password, member);

        unregister(member);
        clearContext();
    }

    private void validateUnregisterRequest(String password, Member member) {

        if (isAllowedRoleToUnregister(member.getRole()) == false) {
            throw new BadRequestException(UNREGISTER_NOT_ALLOWED);
        }

        checkPasswordMatches(member, password);
    }


    public MyProfileToUnregisterResponse loadProfileToUnregister(String username) {

        Member member = findMemberWithCompanyBy(username);

        if (isAllowedRoleToUnregister(member.getRole())) {
            return MyProfileToUnregisterResponse.of(member);
        }

        throw new BadRequestException(UNREGISTER_NOT_ALLOWED);
    }

    private boolean isAllowedRoleToUnregister(Role role) {
        return role == STUDENT || role == COMPANY;
    }


    private Member findMemberWithCompanyBy(String username) {
        return memberRepository.findActiveMemberWithCompanyBy(username)
                .orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));
    }

}
