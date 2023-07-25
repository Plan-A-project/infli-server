package com.plana.infli.web.dto.request.member;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyMemberCreateRequest {

  private String email;
  private String password;
  private String companyName;


}
