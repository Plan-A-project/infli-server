package com.plana.infli.service;

import static com.plana.infli.domain.editor.MemberEditor.*;
import static com.plana.infli.exception.custom.BadRequestException.*;
import static com.plana.infli.exception.custom.ConflictException.DUPLICATED_NICKNAME;
import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.MemberProfileImage;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.exception.custom.ConflictException;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.service.aop.upload.Upload;
import com.plana.infli.utils.S3Uploader;
import com.plana.infli.web.dto.request.setting.modify.nickname.ModifyNicknameServiceRequest;
import com.plana.infli.web.dto.request.setting.modify.password.ModifyPasswordServiceRequest;
import com.plana.infli.web.dto.request.setting.unregister.UnregisterMemberServiceRequest;
import com.plana.infli.web.dto.request.setting.validate.nickname.ValidateNewNicknameServiceRequest;
import com.plana.infli.web.dto.request.setting.validate.password.AuthenticatePasswordServiceRequest;
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

    public boolean isAvailableNewNickname(ValidateNewNicknameServiceRequest request) {
        checkDuplicate(request.getNewNickname());
        return true;
    }

    private void checkDuplicate(String newNickname) {
        if (memberRepository.existsByNickname(newNickname)) {
            throw new ConflictException(DUPLICATED_NICKNAME);
        }
    }


    @Transactional
    public void changeNickname(ModifyNicknameServiceRequest request) {
        Member member = findMemberBy(request.getUsername());

        checkDuplicate(request.getNewNickname());

        editNickname(member, request.getNewNickname());
    }

    //TODO
    public void authenticatePassword(AuthenticatePasswordServiceRequest request) {

        Member member = findMemberBy(request.getUsername());

        checkPasswordMatches(member, request.getPassword());
    }

    private void checkPasswordMatches(Member member, String password) {

        if (passwordEncoder.matches(password, member.getPassword()) == false) {
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

        MemberProfileImage newProfileImage = MemberProfileImage.of(originalUrl, thumbnailUrl);

        editProfileImage(member, newProfileImage);

        return ChangeProfileImageResponse.of(newProfileImage);
    }

    @Transactional
    public void unregisterMember(UnregisterMemberServiceRequest request) {
        Member member = findMemberBy(request.getAuthenticatedEmail());

        validateUnregisterRequest(member, request);

        unregister(member);
    }

    //TODO
    private void validateUnregisterRequest(Member member, UnregisterMemberServiceRequest request) {
        checkPasswordMatches(member, request.getPassword());

        if (emailAndNameMatches(member, request) == false) {
            throw new BadRequestException(INVALID_MEMBER_INFO);
        }
    }

    //TODO 회원탈퇴시 기업회원과 일반 회원 구분해야됨
    private boolean emailAndNameMatches(Member member, UnregisterMemberServiceRequest request) {
        return member.getName().getRealName().equals(request.getName()) &&
                member.getUsername().equals(request.getEmail());
    }


    public MyProfileToUnregisterResponse loadProfileToUnregister(String username) {
        return MyProfileToUnregisterResponse.of(findMemberBy(username));
    }

}
