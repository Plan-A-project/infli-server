package com.plana.infli.service;

import static com.plana.infli.exception.custom.BadRequestException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.BadRequestException;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.utils.S3Uploader;
import com.plana.infli.web.dto.request.profile.MemberWithdrawalRequest;
import com.plana.infli.web.dto.request.profile.NicknameModifyRequest;
import com.plana.infli.web.dto.request.profile.PasswordConfirmRequest;
import com.plana.infli.web.dto.request.profile.PasswordModifyRequest;
import com.plana.infli.web.dto.response.profile.MemberProfileResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberRepository memberRepository;

    private final S3Uploader s3Uploader;

    private final PasswordEncoder passwordEncoder;

    public MemberProfileResponse getMemberProfile(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));

        return new MemberProfileResponse(member.getNickname(), member.getRole(), member.getEmail());
    }

    @Transactional
    public boolean modifyNickname(NicknameModifyRequest nicknameModifyRequest) {
        Member member = memberRepository.findByEmail(nicknameModifyRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(nicknameModifyRequest.getEmail() + "is not found"));

        member.changeNickname(nicknameModifyRequest.getAfterNickname());
        return true;
    }

    public boolean checkPassword(PasswordConfirmRequest passwordConfirmRequest) {
        Member member = memberRepository.findByEmail(passwordConfirmRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(
                passwordConfirmRequest.getEmail() + " is not found"));

        if(passwordEncoder.matches(passwordConfirmRequest.getPassword(), member.getPassword())){
            return true;
        } else {
            throw new BadRequestException(PASSWORD_NOT_MATCH);
        }
    }

    @Transactional
    public boolean modifyPassword(PasswordModifyRequest passwordModifyRequest) {
        String encodeAfterPassword = new BCryptPasswordEncoder().encode(passwordModifyRequest.getNewPassword());

        Member member = memberRepository.findByEmail(passwordModifyRequest.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException(passwordModifyRequest.getEmail() + " is not found"));

        member.changePassword(encodeAfterPassword);
        return true;
    }

    @Transactional
    public String modifyProfileImage(String email, MultipartFile profileImage, String dirName){
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email + " is not found"));

        memberRepository.save(member);

        String path = dirName + "/member_" + member.getId();

        String imageUrl = s3Uploader.upload(profileImage, path);

        member.changeProfileImage(imageUrl);

        return imageUrl;
    }

    @Transactional
    public boolean deleteMember(MemberWithdrawalRequest memberWithdrawalRequest){
        Member member = memberRepository.findByEmail(memberWithdrawalRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException(
                memberWithdrawalRequest.getEmail() + " is not found"));

        member.deleteMember();
        return true;
    }


}
