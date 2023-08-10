package com.plana.infli.repository.university;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;
import java.util.Optional;

public interface UniversityRepositoryCustom {

    Optional<University> findByMemberUsername(String username);

    Boolean isMemberAndPostInSameUniversity(Member member, Post post);
}
