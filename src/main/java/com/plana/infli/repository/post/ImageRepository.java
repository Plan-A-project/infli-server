package com.plana.infli.repository.post;

import com.plana.infli.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByPostIdOrderByPageDesc(Long postId);
}
