package com.plana.infli.web.controller;

import com.plana.infli.service.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

}
