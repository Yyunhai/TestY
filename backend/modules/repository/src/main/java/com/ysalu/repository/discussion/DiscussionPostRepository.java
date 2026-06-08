package com.ysalu.repository.discussion;

import com.ysalu.domain.discussion.DiscussionPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {

    List<DiscussionPost> findAllByOrderByCreatedAtDesc();

    Optional<DiscussionPost> findByIdAndAuthor_Id(Long id, Long authorId);

    void deleteByIdAndAuthor_Id(Long id, Long authorId);

    List<DiscussionPost> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);
}
