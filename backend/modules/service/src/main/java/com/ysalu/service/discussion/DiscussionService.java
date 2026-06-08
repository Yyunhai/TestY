package com.ysalu.service.discussion;

import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.discussion.DiscussionPost;
import com.ysalu.domain.discussion.DiscussionReply;
import com.ysalu.domain.discussion.PostBookmark;
import com.ysalu.domain.discussion.PostLike;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.discussion.DiscussionPostRepository;
import com.ysalu.repository.discussion.DiscussionReplyRepository;
import com.ysalu.repository.discussion.PostBookmarkRepository;
import com.ysalu.repository.discussion.PostLikeRepository;
import com.ysalu.service.common.AuthException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscussionService {

    private final DiscussionPostRepository discussionPostRepository;
    private final DiscussionReplyRepository discussionReplyRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;

    public DiscussionService(
            DiscussionPostRepository discussionPostRepository,
            DiscussionReplyRepository discussionReplyRepository,
            UserAccountRepository userAccountRepository,
            PostLikeRepository postLikeRepository,
            PostBookmarkRepository postBookmarkRepository
    ) {
        this.discussionPostRepository = discussionPostRepository;
        this.discussionReplyRepository = discussionReplyRepository;
        this.userAccountRepository = userAccountRepository;
        this.postLikeRepository = postLikeRepository;
        this.postBookmarkRepository = postBookmarkRepository;
    }

    private PostSummaryView toSummaryView(DiscussionPost post, Long currentUserId) {
        long replyCount = discussionReplyRepository.countByPost_Id(post.getId());
        long likeCount = postLikeRepository.countByPost_Id(post.getId());
        long bookmarkCount = postBookmarkRepository.countByPost_Id(post.getId());
        boolean liked = currentUserId != null && postLikeRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId);
        boolean bookmarked = currentUserId != null && postBookmarkRepository.existsByPost_IdAndUser_Id(post.getId(), currentUserId);
        return new PostSummaryView(
                post.getId(), post.getTitle(), post.getTags(),
                post.getAuthor().getUsername(), replyCount,
                likeCount, bookmarkCount, liked, bookmarked,
                post.getCreatedAt(), post.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<PostSummaryView> listPosts(Long currentUserId) {
        return discussionPostRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(post -> toSummaryView(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummaryView> listPostsByUser(Long userId, Long currentUserId) {
        return discussionPostRepository.findByAuthor_IdOrderByCreatedAtDesc(userId).stream()
                .map(post -> toSummaryView(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummaryView> listLikedPostsByUser(Long userId, Long currentUserId) {
        return postLikeRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(like -> toSummaryView(like.getPost(), currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostSummaryView> listBookmarkedPostsByUser(Long userId, Long currentUserId) {
        return postBookmarkRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(bm -> toSummaryView(bm.getPost(), currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostDetailView getPost(Long postId, Long currentUserId) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new AuthException("Post not found."));
        List<ReplyView> replies = discussionReplyRepository.findByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(reply -> new ReplyView(
                        reply.getId(), reply.getContent(),
                        reply.getAuthor().getUsername(), reply.getAuthor().getId(),
                        reply.getCreatedAt(), reply.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        long replyCount = replies.size();
        long likeCount = postLikeRepository.countByPost_Id(postId);
        long bookmarkCount = postBookmarkRepository.countByPost_Id(postId);
        boolean liked = currentUserId != null && postLikeRepository.existsByPost_IdAndUser_Id(postId, currentUserId);
        boolean bookmarked = currentUserId != null && postBookmarkRepository.existsByPost_IdAndUser_Id(postId, currentUserId);
        return new PostDetailView(
                post.getId(), post.getTitle(), post.getContent(), post.getTags(),
                post.getAuthor().getUsername(), post.getAuthor().getId(),
                replyCount, likeCount, bookmarkCount, liked, bookmarked,
                post.getCreatedAt(), post.getUpdatedAt(), replies
        );
    }

    @Transactional
    public PostDetailView createPost(Long authorId, String title, String content, String tags) {
        UserAccount author = userAccountRepository.findById(authorId)
                .orElseThrow(() -> new AuthException("User not found."));
        DiscussionPost post = new DiscussionPost();
        post.setAuthor(author);
        post.setTitle(title);
        post.setContent(content);
        post.setTags(tags != null && !tags.trim().isEmpty() ? tags.trim() : null);
        discussionPostRepository.save(post);
        return getPost(post.getId(), authorId);
    }

    @Transactional
    public PostDetailView updatePost(Long userId, Long postId, String title, String content, String tags) {
        DiscussionPost post = discussionPostRepository.findByIdAndAuthor_Id(postId, userId)
                .orElseThrow(() -> new AuthException("Post not found or access denied."));
        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (tags != null) post.setTags(tags.trim().isEmpty() ? null : tags.trim());
        discussionPostRepository.save(post);
        return getPost(post.getId(), userId);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new AuthException("Post not found."));
        if (!post.getAuthor().getId().equals(userId)) {
            throw new AuthException("Access denied.");
        }
        postLikeRepository.findByPost_IdAndUser_Id(postId, userId).ifPresent(postLikeRepository::delete);
        postBookmarkRepository.findByPost_IdAndUser_Id(postId, userId).ifPresent(postBookmarkRepository::delete);
        discussionReplyRepository.deleteByPost_Id(postId);
        discussionPostRepository.delete(post);
    }

    @Transactional
    public ReplyView addReply(Long postId, Long authorId, String content) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new AuthException("Post not found."));
        UserAccount author = userAccountRepository.findById(authorId)
                .orElseThrow(() -> new AuthException("User not found."));
        DiscussionReply reply = new DiscussionReply();
        reply.setPost(post);
        reply.setAuthor(author);
        reply.setContent(content);
        discussionReplyRepository.save(reply);
        return new ReplyView(reply.getId(), content, author.getUsername(), author.getId(), reply.getCreatedAt(), reply.getUpdatedAt());
    }

    @Transactional
    public ReplyView updateReply(Long userId, Long replyId, String content) {
        DiscussionReply reply = discussionReplyRepository.findByIdAndAuthor_Id(replyId, userId)
                .orElseThrow(() -> new AuthException("Reply not found or access denied."));
        if (content != null) reply.setContent(content);
        discussionReplyRepository.save(reply);
        return new ReplyView(reply.getId(), reply.getContent(), reply.getAuthor().getUsername(), reply.getAuthor().getId(), reply.getCreatedAt(), reply.getUpdatedAt());
    }

    @Transactional
    public void deleteReply(Long userId, Long replyId) {
        DiscussionReply reply = discussionReplyRepository.findById(replyId)
                .orElseThrow(() -> new AuthException("Reply not found."));
        if (!reply.getAuthor().getId().equals(userId)) {
            throw new AuthException("Access denied.");
        }
        discussionReplyRepository.delete(reply);
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new AuthException("Post not found."));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found."));
        java.util.Optional<PostLike> existing = postLikeRepository.findByPost_IdAndUser_Id(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return false;
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
            return true;
        }
    }

    @Transactional
    public boolean toggleBookmark(Long postId, Long userId) {
        DiscussionPost post = discussionPostRepository.findById(postId)
                .orElseThrow(() -> new AuthException("Post not found."));
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found."));
        java.util.Optional<PostBookmark> existing = postBookmarkRepository.findByPost_IdAndUser_Id(postId, userId);
        if (existing.isPresent()) {
            postBookmarkRepository.delete(existing.get());
            return false;
        } else {
            PostBookmark bookmark = new PostBookmark();
            bookmark.setPost(post);
            bookmark.setUser(user);
            postBookmarkRepository.save(bookmark);
            return true;
        }
    }
}
