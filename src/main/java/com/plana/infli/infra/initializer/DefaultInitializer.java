package com.plana.infli.infra.initializer;

import static com.plana.infli.domain.Board.create;
import static com.plana.infli.domain.embedded.member.LoginCredentials.*;
import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.Role.ADMIN;
import static com.plana.infli.domain.type.Role.STUDENT;
import static com.plana.infli.domain.type.Role.STUDENT_COUNCIL;
import static com.plana.infli.domain.type.VerificationStatus.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.embedded.member.BasicCredentials;
import com.plana.infli.domain.embedded.member.ProfileImage;
import com.plana.infli.domain.embedded.member.StudentCredentials;
import com.plana.infli.domain.type.BoardType;
import com.plana.infli.domain.University;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile({"dev", "prod", "local"})
@Order(0)
public class DefaultInitializer implements CommandLineRunner {

    private static final String FUDAN = "푸단대학교";

    private final BoardRepository boardRepository;

    private final UniversityRepository universityRepository;

    private final MemberRepository memberRepository;

    private final PasswordEncoder encoder;

    private University university;

    @Override
    @Transactional
    public void run(String... args) {

        university = universityRepository.findByName(FUDAN)
                .orElseGet(() -> universityRepository.save(University.create(FUDAN)));

        createBoardWithType(EMPLOYMENT);
        createBoardWithType(ACTIVITY);
        createBoardWithType(CLUB);
        createBoardWithType(ANONYMOUS);
        createBoardWithType(CAMPUS_LIFE);

        createAdminMember();
        createAnonymousUser();
        createStudentCouncilMember();
    }

    private void createStudentCouncilMember() {

        if (memberRepository.existsByRole(STUDENT_COUNCIL) == false) {

            memberRepository.save(Member.builder()
                    .university(university)
                    .role(STUDENT_COUNCIL)
                    .verificationStatus(SUCCESS)
                    .loginCredentials(of("studentcouncil1234", encoder.encode("infli1234!")))
                    .profileImage(ProfileImage.ofDefaultProfileImage())
                    .basicCredentials(BasicCredentials.ofDefaultWithNickname("학생회"))
                    .studentCredentials(null)
                    .companyCredentials(null)
                    .build());
        }
    }

    private void createAnonymousUser() {
        if (memberRepository.existsByUsername("anonymousUser") == false) {

            memberRepository.save(Member.builder()
                    .university(university)
                    .role(STUDENT)
                    .verificationStatus(SUCCESS)
                    .loginCredentials(of("anonymousUser", encoder.encode("password1234!")))
                    .profileImage(ProfileImage.ofDefaultProfileImage())
                    .basicCredentials(BasicCredentials.ofDefaultWithNickname("익명사용자"))
                    .studentCredentials(StudentCredentials.ofDefault("이영진"))
                    .companyCredentials(null)
                    .build());
        }
    }

    private void createBoardWithType(BoardType boardType) {
        if (boardRepository.existsByBoardTypeAndUniversity(boardType, university) == false) {
            boardRepository.save(create(boardType, university));
        }
    }

    private void createAdminMember() {
            memberRepository.save(Member.builder()
                    .university(university)
                    .role(ADMIN)
                    .verificationStatus(SUCCESS)
                    .loginCredentials(of("admin1111", encoder.encode("infli1234!")))
                    .profileImage(ProfileImage.ofDefaultProfileImage())
                    .basicCredentials(BasicCredentials.ofDefaultWithNickname("인플리 관리자"))
                    .studentCredentials(null)
                    .companyCredentials(null)
                    .build());
    }
}
