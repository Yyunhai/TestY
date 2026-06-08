package com.ysalu.repository.discussion;

import com.ysalu.domain.discussion.DiscussionPost;
import com.ysalu.domain.discussion.PostLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPost_Id(Long postId);

    Optional<PostLike> findByPost_IdAndUser_Id(Long postId, Long userId);

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    List<PostLike> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
