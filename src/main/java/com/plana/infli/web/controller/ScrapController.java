package com.plana.infli.web.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.plana.infli.service.ScrapService;
import com.plana.infli.web.resolver.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping("/posts/{postId}/scraps")
    @ResponseStatus(CREATED)
    public void createScrap(@AuthenticatedPrincipal String username, @PathVariable Long postId) {
        scrapService.createScrap(username, postId);
    }

    @DeleteMapping("/posts/{postId}/scraps")
    public void cancelScrap(@AuthenticatedPrincipal String username, @PathVariable Long postId) {
        scrapService.cancelScrap(username, postId);
    }
}
