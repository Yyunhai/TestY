package com.ysalu.repository.discussion;

import com.ysalu.domain.discussion.DiscussionPost;
import com.ysalu.domain.discussion.PostBookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    long countByPost_Id(Long postId);

    Optional<PostBookmark> findByPost_IdAndUser_Id(Long postId, Long userId);

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    List<PostBookmark> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
