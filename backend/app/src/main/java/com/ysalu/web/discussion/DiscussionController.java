package com.ysalu.web.discussion;

import com.ysalu.service.discussion.DiscussionService;
import com.ysalu.service.discussion.PostDetailView;
import com.ysalu.service.discussion.PostSummaryView;
import com.ysalu.service.discussion.ReplyView;
import com.ysalu.service.security.PermissionCodes;
import com.ysalu.web.auth.SessionUser;
import com.ysalu.web.common.SessionAuthorization;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;
    private final SessionAuthorization sessionAuthorization;

    public DiscussionController(DiscussionService discussionService, SessionAuthorization sessionAuthorization) {
        this.discussionService = discussionService;
        this.sessionAuthorization = sessionAuthorization;
    }

    @GetMapping("/posts")
    public List<PostSummaryView> listPosts(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        return discussionService.listPosts(sessionUser.getId());
    }

    @GetMapping("/posts/{postId}")
    public PostDetailView getPost(@PathVariable Long postId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        return discussionService.getPost(postId, sessionUser.getId());
    }

    @PostMapping("/posts")
    public PostDetailView createPost(@Valid @RequestBody CreatePostRequest request, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        return discussionService.createPost(sessionUser.getId(), request.getTitle(), request.getContent(), request.getTags());
    }

    @PutMapping("/posts/{postId}")
    public PostDetailView updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostRequest request, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        return discussionService.updatePost(sessionUser.getId(), postId, request.getTitle(), request.getContent(), request.getTags());
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        discussionService.deletePost(sessionUser.getId(), postId);
    }

    @PostMapping("/posts/{postId}/replies")
    public ReplyView addReply(@PathVariable Long postId, @Valid @RequestBody CreateReplyRequest request, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        return discussionService.addReply(postId, sessionUser.getId(), request.getContent());
    }

    @PutMapping("/replies/{replyId}")
    public ReplyView updateReply(@PathVariable Long replyId, @Valid @RequestBody UpdateReplyRequest request, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        return discussionService.updateReply(sessionUser.getId(), replyId, request.getContent());
    }

    @DeleteMapping("/replies/{replyId}")
    public void deleteReply(@PathVariable Long replyId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_WRITE);
        discussionService.deleteReply(sessionUser.getId(), replyId);
    }

    @PostMapping("/posts/{postId}/like")
    public Map<String, Object> toggleLike(@PathVariable Long postId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        boolean liked = discussionService.toggleLike(postId, sessionUser.getId());
        PostDetailView post = discussionService.getPost(postId, sessionUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", post.getLikeCount());
        return result;
    }

    @PostMapping("/posts/{postId}/bookmark")
    public Map<String, Object> toggleBookmark(@PathVariable Long postId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        boolean bookmarked = discussionService.toggleBookmark(postId, sessionUser.getId());
        PostDetailView post = discussionService.getPost(postId, sessionUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("bookmarked", bookmarked);
        result.put("bookmarkCount", post.getBookmarkCount());
        return result;
    }

    @GetMapping("/my/posts")
    public List<PostSummaryView> myPosts(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        return discussionService.listPostsByUser(sessionUser.getId(), sessionUser.getId());
    }

    @GetMapping("/my/likes")
    public List<PostSummaryView> myLikes(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        return discussionService.listLikedPostsByUser(sessionUser.getId(), sessionUser.getId());
    }

    @GetMapping("/my/bookmarks")
    public List<PostSummaryView> myBookmarks(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DISCUSSION_READ);
        return discussionService.listBookmarkedPostsByUser(sessionUser.getId(), sessionUser.getId());
    }
}
