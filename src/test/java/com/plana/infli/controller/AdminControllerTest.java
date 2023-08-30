package com.plana.infli.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plana.infli.annotation.MockMvcTest;
import com.plana.infli.factory.UniversityFactory;
import com.plana.infli.repository.company.CompanyRepository;
import com.plana.infli.repository.member.MemberRepository;
import com.plana.infli.repository.university.UniversityRepository;
import com.plana.infli.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@MockMvcTest
public class AdminControllerTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UniversityFactory universityFactory;

    @Autowired
    private MemberService memberService;


    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        universityRepository.deleteAllInBatch();
    }
}
