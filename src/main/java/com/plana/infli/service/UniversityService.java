package com.plana.infli.service;

import static com.plana.infli.exception.custom.NotFoundException.*;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.University;
import com.plana.infli.exception.custom.NotFoundException;
import com.plana.infli.repository.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityService {

    private final UniversityRepository universityRepository;

    public University findByMember(Member member) {
        return universityRepository.findByMember(member);
    }

    public University findById(Long id) {
        return universityRepository.findUniversityById(id);
    }
}
