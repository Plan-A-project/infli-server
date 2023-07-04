package com.plana.infli.web.dto.request.image;

import com.plana.infli.domain.Image;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageCreateRs {

    private Long imageId;
    private String imageUrl;
    private int index;

    public ImageCreateRs(Image entity) {
        this.imageId = entity.getId();
        this.imageUrl = entity.getImageUrl();
        this.index = entity.getPage();
    }
}
