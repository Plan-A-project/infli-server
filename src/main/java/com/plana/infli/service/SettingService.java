package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.domain.type.Role.*;
import static com.plana.infli.exception.custom.BadRequestException.*;
import static com.plana.infli.exception.custom.ConflictException.DUPLICATED_NICKNAME;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.type.Role;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.service.aop.upload.Upload;
import com.plana.infli.utils.S3Uploader;
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

    public void checkIsAvailableNewNickname(String newNickname) {
        if (newNickname.matches("^[ㄱ-ㅎ가-힣A-Za-z0-9-_]{2,8}$") == false) {
            throw new BadRequestException("닉네임은 2~8자리여야 합니다. 한글, 영어, 숫자 조합 가능.");
        }

        if (memberRepository.existsByNickname(newNickname)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }
    }

    @Transactional
    public void changeNickname(String username, String newNickname) {

        Member member = findMemberBy(username);

        checkIsAvailableNewNickname(newNickname);

        editNickname(member, newNickname);
    }

    public void verifyCurrentPassword(String username, String currentPassword) {

        Member member = findMemberBy(username);

        checkPasswordMatches(member, currentPassword);
    }

    private void checkPasswordMatches(Member member, String password) {

        if (passwordEncoder.matches(
                password, member.getLoginCredentials().getPassword()) == false) {

            throw new BadRequestException(PASSWORD_NOT_MATCH);
        }
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

        if (passwordAndPasswordConfirmMatches(request) == false) {
            throw new BadRequestException(NOT_MATCHES_NEW_PASSWORD_CONFIRM);
        }
    }

    private boolean passwordAndPasswordConfirmMatches(ModifyPasswordServiceRequest request) {
        String newPassword = request.getNewPassword();

        String newPasswordConfirm = request.getNewPasswordConfirm();

        return newPassword.equals(newPasswordConfirm);
    }

    private String encryptNewPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    @Transactional
    @Upload
    public ChangeProfileImageResponse changeProfileImage(String username,
            MultipartFile multipartFile) {

        Member member = findMemberBy(username);

        String path = "member/member_" + member.getId();

        String originalUrl = s3Uploader.uploadAsOriginalImage(multipartFile, path);

        String thumbnailUrl = s3Uploader.uploadAsThumbnailImage(multipartFile, path);

        ProfileImage newProfileImage = ProfileImage.of(originalUrl, thumbnailUrl);

        editProfileImage(member, newProfileImage);

        return ChangeProfileImageResponse.of(newProfileImage);
    }

    @Transactional
    public void unregisterMember(String username, String password) {
        Member member = findMemberBy(username);

        validateUnregisterRequest(password, member);

        unregister(member);
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
