package com.plana.infli.infra.security.service;

import com.plana.infli.domain.Member;
import com.plana.infli.domain.type.Role;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class CustomUser extends User {

    private final Role role;

    private final String nickname;

    public CustomUser(Member member) {
        super(member.getLoginCredentials().getUsername(),
                member.getLoginCredentials().getPassword(),
                member.getBasicCredentials().isDeleted() == false,
                true, true, true,
                List.of(new SimpleGrantedAuthority(member.getRole().toString())));

        this.role = member.getRole();
        this.nickname = member.getBasicCredentials().getNickname();
    }
}
