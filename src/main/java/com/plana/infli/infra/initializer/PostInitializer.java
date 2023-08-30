//package com.plana.infli.infra.initializer;
//
//import static com.plana.infli.domain.Company.*;
//import static com.plana.infli.domain.editor.MemberEditor.acceptPolicy;
//import static com.plana.infli.domain.embedded.member.LoginCredentials.*;
//import static com.plana.infli.domain.embedded.member.LoginCredentials.of;
//import static com.plana.infli.domain.type.BoardType.*;
//import static com.plana.infli.domain.type.PostType.*;
//import static com.plana.infli.domain.type.Role.*;
//import static com.plana.infli.domain.type.VerificationStatus.*;
//import static java.util.UUID.*;
//import static java.util.stream.IntStream.*;
//
//import com.plana.infli.domain.Board;
//import com.plana.infli.domain.Company;
//import com.plana.infli.domain.Member;
//import com.plana.infli.domain.embedded.member.BasicCredentials;
//import com.plana.infli.domain.embedded.member.CompanyCredentials;
//import com.plana.infli.domain.embedded.member.LoginCredentials;
//import com.plana.infli.domain.embedded.member.ProfileImage;
//import com.plana.infli.domain.embedded.member.StudentCredentials;
//import com.plana.infli.domain.type.Role;
//import com.plana.infli.domain.Post;
//import com.plana.infli.domain.type.PostType;
//import com.plana.infli.domain.University;
//import com.plana.infli.domain.embedded.post.Recruitment;
//import com.plana.infli.domain.type.VerificationStatus;
//import com.plana.infli.repository.board.BoardRepository;
//import com.plana.infli.repository.company.CompanyRepository;
//import com.plana.infli.repository.member.MemberRepository;
//import com.plana.infli.repository.post.PostRepository;
//import com.plana.infli.repository.university.UniversityRepository;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Profile({"dev"})
//@Order(2)
//public class PostInitializer implements CommandLineRunner {
//
//    private final UniversityRepository universityRepository;
//
//    private final BoardRepository boardRepository;
//
//    private final PostRepository postRepository;
//
//    private final MemberRepository memberRepository;
//
//    private final CompanyRepository companyRepository;
//
//    private final PasswordEncoder encoder;
//
//    private University university;
//
//    @Override
//    public void run(String... args) {
//
//        university = universityRepository.findByName("푸단대학교").get();
//
//        List<Board> boards = boardRepository.findAllActiveBoardBy(university);
//
//        Board employment = boards.get(0);
//        Board activity = boards.get(1);
//        Board club = boards.get(2);
//        Board anonymous = boards.get(3);
//        Board campusLife = boards.get(4);
//
//        createPost(employment, NORMAL);
//        createRecruitmentPost(employment);
//
//        createPost(activity, NORMAL);
//        createPost(activity, RECRUITMENT);
//
//        createPost(club, NORMAL);
//
//        createPost(anonymous, NORMAL);
//
//        createPost(campusLife, NORMAL);
//        createPost(campusLife, ANNOUNCEMENT);
//    }
//
//    private void createRecruitmentPost(Board board) {
//
//        rangeClosed(1, 30).forEach(i -> {
//            Member member = createVerifiedMemberWithRole(COMPANY);
//
//            postRepository.save(Post.builder()
//                    .board(board)
//                    .postType(RECRUITMENT)
//                    .title("제목 " + i)
//                    .content("내용 " + i)
//                    .member(member)
//                    .recruitment(createRecruitment(board, RECRUITMENT))
//                    .build());
//        });
//    }
//
//    public void createPost(Board board, PostType postType) {
//
//        rangeClosed(1, 30).forEach(i -> {
//            Member member = createVerifiedMemberWithRole(ADMIN);
//
//            postRepository.save(Post.builder()
//                    .board(board)
//                    .postType(postType)
//                    .title("제목 " + i)
//                    .content("내용 " + i)
//                    .member(member)
//                    .recruitment(createRecruitment(board, postType))
//                    .build());
//        });
//    }
//
//    private Recruitment createRecruitment(Board board, PostType postType) {
//        if (board.getBoardType() == ACTIVITY && postType == RECRUITMENT) {
//            return Recruitment.create(randomString(), LocalDateTime.now(), LocalDateTime.now());
//        }
//        return null;
//    }
//
//
//    private Member createVerifiedMemberWithRole(Role role) {
//
//        CompanyCredentials companyCredentials = createCompanyCredentials(role);
//        StudentCredentials studentCredentials = createStudentCredentials(role);
//
//        Member member = memberRepository.save(Member.builder()
//                .university(university)
//                .role(role)
//                .verificationStatus(SUCCESS)
//                .loginCredentials(of(randomString(), encoder.encode("password")))
//                .profileImage(ProfileImage.ofDefaultProfileImage())
//                .basicCredentials(BasicCredentials.ofDefaultWithNickname(randomString()))
//                .companyCredentials(companyCredentials)
//                .studentCredentials(studentCredentials)
//                .build());
//
//        acceptPolicy(member);
//        return member;
//    }
//
//    private String randomString() {
//        return UUID.randomUUID().toString().substring(0, 12);
//    }
//
//    private CompanyCredentials createCompanyCredentials(Role role) {
//        if (role == COMPANY) {
//            Company company = companyRepository.save(create(randomString()));
//            return CompanyCredentials.ofDefault(company);
//        }
//
//        return null;
//    }
//
//    private StudentCredentials createStudentCredentials(Role role) {
//        if (role == STUDENT) {
//            return StudentCredentials.ofDefault(randomString());
//        }
//        return null;
//    }
//}
