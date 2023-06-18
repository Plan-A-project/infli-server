package com.plana.infli.repository.university;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.Post;
import com.plana.infli.domain.University;

public interface UniversityRepositoryCustom {

    University findByMember(Member member);

    Boolean isMemberAndPostInSameUniversity(Member member, Post post);
}
