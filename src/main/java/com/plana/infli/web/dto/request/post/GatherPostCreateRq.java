package com.plana.infli.web.dto.request.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GatherPostCreateRq extends PostCreateRq{

    private String enterprise;
    private LocalDate startDate;
    private LocalDate endDate;
}
