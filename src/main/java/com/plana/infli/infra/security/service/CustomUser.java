package com.plana.infli.infra.security.service;

import com.plana.infli.domain.Member;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUser extends User {

    private Member member;

    public CustomUser(Member member) {
        super(member.getLoginCredentials().getUsername(),
                member.getLoginCredentials().getPassword(),
                member.getBasicCredentials().isDeleted() == false,
                true, true, true,
                List.of(new SimpleGrantedAuthority(member.getRole().toString())));

        this.member = member;
    }
}
