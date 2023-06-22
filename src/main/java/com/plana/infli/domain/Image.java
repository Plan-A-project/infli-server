package com.plana.infli.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class Image extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String imageUrl;

    private boolean isDeleted;

    private int page;

    @Builder
    public Image(String imageUrl, boolean isDeleted, int page) {
        this.imageUrl = imageUrl;
        this.isDeleted = isDeleted;
        this.page = page;
    }

    public void setPost(Post post) {
        this.post = post;
        if (!post.getImageList().contains(this)) {
            post.getImageList().add(this);
        }
    }
}
