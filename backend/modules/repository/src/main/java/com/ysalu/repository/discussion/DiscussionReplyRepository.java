package com.ysalu.repository.discussion;

import com.ysalu.domain.discussion.DiscussionReply;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    List<DiscussionReply> findByPost_IdOrderByCreatedAtAsc(Long postId);

    long countByPost_Id(Long postId);

    Optional<DiscussionReply> findByIdAndAuthor_Id(Long id, Long authorId);

    void deleteByPost_Id(Long postId);
}
