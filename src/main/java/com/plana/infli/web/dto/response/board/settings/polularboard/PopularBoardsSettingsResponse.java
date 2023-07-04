package com.plana.infli.web.dto.response.board.settings.polularboard;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.parameters.P;

@Getter
public class PopularBoardsSettingsResponse {


    private final List<SinglePopularBoardForSetting> popularBoards;

    @Builder
    public PopularBoardsSettingsResponse(List<SinglePopularBoardForSetting> popularBoards) {

        this.popularBoards = popularBoards != null ? popularBoards : new ArrayList<>();
    }

    public static PopularBoardsSettingsResponse createPopularBoardsSettingsResponse(List<SinglePopularBoardForSetting> popularBoards) {
        return PopularBoardsSettingsResponse.builder()
                .popularBoards(popularBoards)
                .build();
    }
}
