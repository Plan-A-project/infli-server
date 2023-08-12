package com.plana.infli.config.initializer;

import static com.plana.infli.domain.type.BoardType.*;
import static com.plana.infli.domain.type.PostType.*;
import static com.plana.infli.domain.type.MemberRole.*;
import static java.util.stream.IntStream.*;

import com.plana.infli.domain.Board;
import com.plana.infli.domain.Company;
import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.MemberRole;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.type.PostType;
import com.plana.infli.domain.University;
import com.plana.infli.domain.embedded.member.MemberName;
import com.plana.infli.domain.embedded.post.Recruitment;
import com.plana.infli.repository.board.BoardRepository;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.post.PostRepository;
import com.plana.infli.repository.university.UniversityRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"dev"})
@Order(2)
public class PostInitializer implements CommandLineRunner {

    private final UniversityRepository universityRepository;

    private final BoardRepository boardRepository;

    private final PostRepository postRepository;

    private final MemberRepository memberRepository;

    private final CompanyRepository companyRepository;

    private final PasswordEncoder passwordEncoder;

    private University university;

    @Override
    public void run(String... args) {

        university = universityRepository.findByName("푸단대학교").get();

        List<Board> boards = boardRepository.findAllActiveBoardBy(university);

        Board employment = boards.get(0);
        Board activity = boards.get(1);
        Board club = boards.get(2);
        Board anonymous = boards.get(3);
        Board campusLife = boards.get(4);

        createPost(employment, NORMAL, STUDENT);
        createRecruitmentPost(employment, RECRUITMENT, COMPANY);

        createPost(activity, NORMAL, STUDENT);
        createPost(activity, RECRUITMENT, ADMIN);

        createPost(club, NORMAL, STUDENT);

        createPost(anonymous, NORMAL, STUDENT);

        createPost(campusLife, NORMAL, STUDENT);
        createPost(campusLife, ANNOUNCEMENT, STUDENT_COUNCIL);
    }

    private void createRecruitmentPost(Board board, PostType postType, MemberRole memberRole) {
        rangeClosed(1, 30).forEach(i -> {
            Member member = createCompanyMember(board, memberRole, i);

            postRepository.save(Post.builder()
                    .board(board)
                    .postType(postType)
                    .member(member)
                    .title("제목 " + i)
                    .content("내용 " + i)
                    .recruitment(createRecruitment(board, postType, i))
                    .build());

        });
    }

    public void createPost(Board board, PostType postType, MemberRole memberRole) {

        rangeClosed(1, 30).forEach(i -> {
            Member member = createMember(board, memberRole, i);

            postRepository.save(Post.builder()
                    .board(board)
                    .postType(postType)
                    .member(member)
                    .title("제목 " + i)
                    .content("내용 " + i)
                    .recruitment(createRecruitment(board, postType, i))
                    .build());

        });
    }

    private Recruitment createRecruitment(Board board, PostType postType, int i) {
        if (board.getBoardType() == ACTIVITY && postType == RECRUITMENT) {
            return Recruitment.create("회사 " + i, LocalDateTime.now(), LocalDateTime.now());
        }

        return null;
    }

    public Member createMember(Board board, MemberRole memberRole, int i) {
        return memberRepository.save(Member.builder()
                .username(memberRole.name().toLowerCase() + " " + board.getBoardName() + i)
                .encodedPassword(passwordEncoder.encode("password"))
                .name(MemberName.of("인플리 " + board.getBoardName() + memberRole.name() + i,
                        "인플리 " + board.getBoardName() + memberRole.name() + i))
                .role(memberRole)
                .university(university)
                .build());
    }

    public Member createCompanyMember(Board board, MemberRole memberRole, int i) {
        Company company = Company.create("카카오 " + i);

        companyRepository.save(company);
        return memberRepository.save(Member.builder()
                .username(memberRole.name().toLowerCase() + " " + board.getBoardName() + i)
                .encodedPassword("password")
                .company(company)
                .role(memberRole)
                .university(university)
                .build());
    }
}
