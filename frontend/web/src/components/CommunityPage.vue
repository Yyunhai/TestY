<template>
  <div class="community" ref="community">
    <div class="community-header" ref="communityHeader">
      <div class="header-content">
        <h2 class="header-title">社区讨论</h2>
        <p class="header-desc">与团队一起交流想法、分享知识</p>
      </div>
      <button class="btn-primary" type="button" @click="showCreateForm = true">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        发布新帖
      </button>
    </div>

    <Transition name="fade-slide">
      <div v-if="showCreateForm" class="create-card" ref="createCard">
        <div class="create-header">
          <h3 class="create-title">{{ editingPost ? '编辑帖子' : '发布新帖' }}</h3>
          <button class="btn-close" type="button" @click="cancelCreate">&times;</button>
        </div>
        <form class="create-form" @submit.prevent="submitPost">
          <div class="form-group">
            <label class="form-label">标题</label>
            <input class="form-input" type="text" v-model="postForm.title" placeholder="输入帖子标题" required maxlength="120" />
          </div>
          <div class="form-group">
            <label class="form-label">内容</label>
            <textarea class="form-textarea" rows="6" v-model="postForm.content" placeholder="写下你想讨论的内容..." required></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">标签</label>
            <input class="form-input" type="text" v-model="postForm.tags" placeholder="用逗号分隔，例如: 技术, 提问" />
          </div>
          <div class="form-actions">
            <button class="btn-ghost" type="button" @click="cancelCreate">取消</button>
            <button class="btn-primary" type="submit" :disabled="postBusy">{{ postBusy ? '处理中...' : editingPost ? '保存' : '发布' }}</button>
          </div>
        </form>
      </div>
    </Transition>

    <div v-if="!currentPost" class="posts-list" ref="postsList">
      <div class="list-toolbar">
        <div class="search-box">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" stroke-width="1.3"/>
            <path d="M10.5 10.5L14.5 14.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          <input class="search-input" type="text" v-model="searchKeyword" placeholder="搜索帖子..." />
        </div>
        <span class="post-count">{{ filteredPosts.length }} 个帖子</span>
      </div>

      <div v-if="postsLoading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <template v-else>
        <div v-for="(post, i) in filteredPosts" :key="post.id" class="post-card" :ref="el => setPostCardRef(el, i)" @click="openPost(post)">
          <div class="post-meta">
            <span class="post-author">{{ post.authorName }}</span>
            <span class="post-date">{{ formatDateTime(post.createdAt) }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <div class="post-footer">
            <div class="post-tags" v-if="post.tags">
              <span v-for="tag in parseTags(post.tags)" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <div class="post-stats">
              <span class="post-replies">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 5H11M3 8H8M3 11H6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
                {{ post.replyCount }}
              </span>
              <span class="post-stat" :class="{ active: post.likedByCurrentUser }">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 12L6.1 11.2C3.4 8.7 1.5 7 1.5 5C1.5 3.3 2.8 2 4.5 2C5.5 2 6.4 2.5 7 3.2C7.6 2.5 8.5 2 9.5 2C11.2 2 12.5 3.3 12.5 5C12.5 7 10.6 8.7 7.9 11.2L7 12Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
                {{ post.likeCount }}
              </span>
              <span class="post-stat" :class="{ active: post.bookmarkedByCurrentUser }">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 1.5V12.5L7 10L11 12.5V1.5H3Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
                {{ post.bookmarkCount }}
              </span>
            </div>
          </div>
        </div>
        <div v-if="!filteredPosts.length" class="empty-state">
          <p>暂无帖子，快来发布第一个讨论吧！</p>
        </div>
      </template>
    </div>

    <Transition name="fade-slide">
      <div v-if="currentPost" class="post-detail" ref="postDetail">
        <button class="btn-back" type="button" @click="currentPost = null">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M10 12L6 8L10 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          返回列表
        </button>

        <div class="detail-card">
          <div class="detail-header">
            <div>
              <h2 class="detail-title">{{ currentPost.title }}</h2>
              <div class="detail-meta">
                <span class="detail-author">{{ currentPost.authorName }}</span>
                <span class="detail-date">{{ formatDateTime(currentPost.createdAt) }}</span>
                <span v-if="currentPost.tags">
                  <span v-for="tag in parseTags(currentPost.tags)" :key="tag" class="tag">{{ tag }}</span>
                </span>
              </div>
            </div>
            <div class="detail-actions">
              <button v-if="currentPost.authorId === currentUserId" class="btn-icon" type="button" title="编辑" @click="editPost(currentPost)">✎</button>
              <button v-if="currentPost.authorId === currentUserId" class="btn-icon danger" type="button" title="删除" @click="deletePost(currentPost.id)">✕</button>
            </div>
          </div>
          <div class="detail-content">{{ currentPost.content }}</div>
          <div class="detail-interactions">
            <button class="interact-btn" :class="{ active: currentPost.likedByCurrentUser }" type="button" @click="toggleLike(currentPost)" :disabled="interactBusy">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 15.5L7.6 14.2C4.1 11 1.5 8.6 1.5 5.7C1.5 3.3 3.4 1.5 5.8 1.5C7.2 1.5 8.5 2.2 9 3.2C9.5 2.2 10.8 1.5 12.2 1.5C14.6 1.5 16.5 3.3 16.5 5.7C16.5 8.6 13.9 11 10.4 14.2L9 15.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
              <span>{{ currentPost.likeCount }}</span>
            </button>
            <button class="interact-btn" :class="{ active: currentPost.bookmarkedByCurrentUser }" type="button" @click="toggleBookmark(currentPost)" :disabled="interactBusy">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 2V16L9 13L14 16V2H4Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
              <span>{{ currentPost.bookmarkCount }}</span>
            </button>
            <button class="interact-btn" type="button" @click="sharePost(currentPost)">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M11 5L7 8M11 13L7 10M14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2C13.1 2 14 2.9 14 4ZM8 9C8 10.1 7.1 11 6 11C4.9 11 4 10.1 4 9C4 7.9 4.9 7 6 7C7.1 7 8 7.9 8 9ZM14 14C14 15.1 13.1 16 12 16C10.9 16 10 15.1 10 14C10 12.9 10.9 12 12 12C13.1 12 14 12.9 14 14Z" stroke="currentColor" stroke-width="1.5"/></svg>
              <span>分享</span>
            </button>
          </div>
        </div>

        <div class="replies-section">
          <h3 class="replies-title">回复 ({{ currentPost.replies.length }})</h3>
          <div v-for="(reply, i) in currentPost.replies" :key="reply.id" class="reply-card" :ref="el => setReplyRef(el, i)">
            <div class="reply-header">
              <div class="reply-author-info">
                <span class="reply-author">{{ reply.authorName }}</span>
                <span class="reply-date">{{ formatDateTime(reply.createdAt) }}</span>
              </div>
              <div v-if="reply.authorId === currentUserId" class="reply-actions">
                <button class="btn-icon" type="button" title="编辑" @click="editReply(reply)">✎</button>
                <button class="btn-icon danger" type="button" title="删除" @click="deleteReply(reply.id, i)">✕</button>
              </div>
            </div>

            <template v-if="editingReplyId === reply.id">
              <textarea class="form-textarea" v-model="editingReplyContent" rows="3"></textarea>
              <div class="form-actions">
                <button class="btn-ghost btn-sm" type="button" @click="editingReplyId = null">取消</button>
                <button class="btn-primary btn-sm" type="button" @click="saveReplyEdit(reply.id)">保存</button>
              </div>
            </template>
            <p v-else class="reply-content">{{ reply.content }}</p>
          </div>

          <div v-if="!currentPost.replies.length" class="empty-state">
            <p>暂无回复，来发表第一条评论吧！</p>
          </div>

          <div class="reply-form">
            <textarea class="form-textarea" v-model="replyFormContent" placeholder="写下你的回复..." rows="3"></textarea>
            <button class="btn-primary" type="button" @click="submitReply" :disabled="!replyFormContent.trim()">发表回复</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script>
import { gsap } from "gsap";

export default {
  name: "CommunityPage",
  props: {
    user: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      posts: [],
      currentPost: null,
      showCreateForm: false,
      editingPost: null,
      postForm: { title: "", content: "", tags: "" },
      postBusy: false,
      searchKeyword: "",
      postsLoading: false,
      replyFormContent: "",
      editingReplyId: null,
      editingReplyContent: "",
      postCardRefs: [],
      replyRefs: [],
      interactBusy: false
    };
  },
  computed: {
    currentUserId() {
      return this.user.id;
    },
    filteredPosts() {
      const kw = this.searchKeyword.toLowerCase();
      if (!kw) return this.posts;
      return this.posts.filter(p =>
        p.title.toLowerCase().includes(kw) ||
        (p.tags || "").toLowerCase().includes(kw) ||
        p.authorName.toLowerCase().includes(kw)
      );
    }
  },
  mounted() {
    this.loadPosts();
  },
  methods: {
    setPostCardRef(el, i) { if (el) this.postCardRefs[i] = el; },
    setReplyRef(el, i) { if (el) this.replyRefs[i] = el; },
    async loadPosts() {
      this.postsLoading = true;
      try {
        this.posts = await this.apiRequest("/api/discussions/posts");
        this.$nextTick(() => this.animatePostCards());
      } catch (err) {
        this.posts = [];
      } finally {
        this.postsLoading = false;
      }
    },
    animatePostCards() {
      const cards = this.postCardRefs.filter(Boolean);
      if (!cards.length) return;
      gsap.fromTo(cards, { opacity: 0, y: 15 }, { opacity: 1, y: 0, stagger: 0.05, duration: 0.35, ease: "power2.out", clearProps: "transform" });
    },
    async openPost(post) {
      try {
        const detail = await this.apiRequest(`/api/discussions/posts/${post.id}`);
        this.currentPost = detail;
        this.$nextTick(() => {
          gsap.fromTo(this.$refs.postDetail, { opacity: 0, y: 10 }, { opacity: 1, y: 0, duration: 0.3, ease: "power2.out", clearProps: "transform" });
          const replies = this.replyRefs.filter(Boolean);
          if (replies.length) {
            gsap.fromTo(replies, { opacity: 0, x: -10 }, { opacity: 1, x: 0, stagger: 0.04, duration: 0.25, ease: "power2.out", clearProps: "transform" });
          }
        });
      } catch (err) {
        void err;
      }
    },
    cancelCreate() {
      this.showCreateForm = false;
      this.editingPost = null;
      this.postForm = { title: "", content: "", tags: "" };
    },
    editPost(post) {
      this.editingPost = post;
      this.postForm = { title: post.title, content: post.content, tags: post.tags || "" };
      this.showCreateForm = true;
    },
    async submitPost() {
      this.postBusy = true;
      try {
        if (this.editingPost) {
          await this.apiRequest(`/api/discussions/posts/${this.editingPost.id}`, {
            method: "PUT", body: { title: this.postForm.title, content: this.postForm.content, tags: this.postForm.tags }
          });
        } else {
          await this.apiRequest("/api/discussions/posts", {
            method: "POST", body: { title: this.postForm.title, content: this.postForm.content, tags: this.postForm.tags }
          });
        }
        this.cancelCreate();
        await this.loadPosts();
      } catch (err) {
        void err;
      } finally {
        this.postBusy = false;
      }
    },
    async deletePost(postId) {
      if (!confirm("确定删除该帖子？")) return;
      try {
        await this.apiRequest(`/api/discussions/posts/${postId}`, { method: "DELETE" });
        this.currentPost = null;
        await this.loadPosts();
      } catch (err) {
        void err;
      }
    },
    async submitReply() {
      if (!this.replyFormContent.trim()) return;
      try {
        const reply = await this.apiRequest(`/api/discussions/posts/${this.currentPost.id}/replies`, {
          method: "POST", body: { content: this.replyFormContent }
        });
        this.currentPost.replies.push(reply);
        this.replyFormContent = "";
        this.$nextTick(() => {
          const els = this.replyRefs.filter(Boolean);
          if (els.length) {
            gsap.fromTo(els[els.length - 1], { opacity: 0, y: 10 }, { opacity: 1, y: 0, duration: 0.3, ease: "power2.out", clearProps: "transform" });
          }
        });
      } catch (err) {
        void err;
      }
    },
    editReply(reply) {
      this.editingReplyId = reply.id;
      this.editingReplyContent = reply.content;
    },
    async saveReplyEdit(replyId) {
      try {
        const updated = await this.apiRequest(`/api/discussions/replies/${replyId}`, {
          method: "PUT", body: { content: this.editingReplyContent }
        });
        const idx = this.currentPost.replies.findIndex(r => r.id === replyId);
        if (idx >= 0) this.currentPost.replies.splice(idx, 1, updated);
        this.editingReplyId = null;
        this.editingReplyContent = "";
      } catch (err) {
        void err;
      }
    },
    async deleteReply(replyId, index) {
      if (!confirm("确定删除该回复？")) return;
      try {
        await this.apiRequest(`/api/discussions/replies/${replyId}`, { method: "DELETE" });
        this.currentPost.replies.splice(index, 1);
      } catch (err) {
        void err;
      }
    },
    async toggleLike(post) {
      if (this.interactBusy) return;
      this.interactBusy = true;
      try {
        const result = await this.apiRequest(`/api/discussions/posts/${post.id}/like`, { method: "POST" });
        post.likedByCurrentUser = result.liked;
        post.likeCount = result.likeCount;
        if (this.currentPost && this.currentPost.id === post.id) {
          this.currentPost.likedByCurrentUser = result.liked;
          this.currentPost.likeCount = result.likeCount;
        }
      } catch (err) { void err; } finally { this.interactBusy = false; }
    },
    async toggleBookmark(post) {
      if (this.interactBusy) return;
      this.interactBusy = true;
      try {
        const result = await this.apiRequest(`/api/discussions/posts/${post.id}/bookmark`, { method: "POST" });
        post.bookmarkedByCurrentUser = result.bookmarked;
        post.bookmarkCount = result.bookmarkCount;
        if (this.currentPost && this.currentPost.id === post.id) {
          this.currentPost.bookmarkedByCurrentUser = result.bookmarked;
          this.currentPost.bookmarkCount = result.bookmarkCount;
        }
      } catch (err) { void err; } finally { this.interactBusy = false; }
    },
    sharePost(post) {
      const url = `${window.location.origin}${window.location.pathname}#community?post=${post.id}`;
      if (navigator.share) {
        navigator.share({ title: post.title, text: post.title, url }).catch(() => {});
      } else {
        navigator.clipboard.writeText(url).then(() => {
          alert("链接已复制到剪贴板");
        }).catch(() => {
          prompt("复制链接:", url);
        });
      }
    },
    parseTags(tags) {
      if (!tags) return [];
      return tags.split(",").map(t => t.trim()).filter(Boolean);
    },
    formatDateTime(value) {
      if (!value) return "--";
      const date = new Date(value);
      if (isNaN(date.getTime())) return value;
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      const h = String(date.getHours()).padStart(2, "0");
      const min = String(date.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${d} ${h}:${min}`;
    },
    async apiRequest(url, options = {}) {
      const config = { method: options.method || "GET", credentials: "include", headers: { Accept: "application/json" } };
      if (options.body) {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(options.body);
      }
      const res = await fetch(url, config);
      const raw = await res.text();
      let data = null;
      if (raw) { try { data = JSON.parse(raw); } catch (e) { data = raw; } }
      if (!res.ok) throw new Error(data && data.message ? data.message : "Request failed.");
      return data;
    }
  }
};
</script>

<style scoped>
.community {
  display: grid;
  gap: 20px;
}

.community-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.header-content {
  display: grid;
  gap: 6px;
}

.header-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.3px;
}

.header-desc {
  margin: 0;
  font-size: 14px;
  color: var(--text-muted);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 12px;
  background: var(--accent);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-primary:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-ghost:hover {
  background: var(--bg-secondary);
  color: var(--text);
  border-color: var(--border-hover);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  font-size: 20px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.15s;
}

.btn-close:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.btn-icon {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  font-size: 16px;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.15s;
}

.btn-icon:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.btn-icon.danger:hover {
  background: var(--danger-bg);
  color: var(--danger);
}

/* Create Card */
.create-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
  display: grid;
  gap: 16px;
}

.create-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.create-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.create-form {
  display: grid;
  gap: 14px;
}

.form-group {
  display: grid;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--bg-secondary);
  color: var(--text);
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
}

.form-input:focus, .form-textarea:focus {
  border-color: var(--accent);
  background: var(--surface);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.06);
}

.form-textarea {
  resize: vertical;
  line-height: 1.6;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* List */
.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 320px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 14px 10px 36px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  color: var(--text);
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: var(--accent);
}

.search-input::placeholder {
  color: var(--text-light);
}

.post-count {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}

.posts-list {
  display: grid;
  gap: 12px;
}

.post-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: grid;
  gap: 10px;
}

.post-card:hover {
  border-color: var(--border-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.post-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--text-muted);
}

.post-author {
  font-weight: 600;
  color: var(--text-secondary);
}

.post-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.4;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.post-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  border-radius: 8px;
  background: var(--accent-bg);
  color: var(--accent);
  font-size: 11px;
  font-weight: 600;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 14px;
}

.post-replies, .post-stat {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-muted);
}

.post-stat.active {
  color: var(--accent);
}

.post-replies svg, .post-stat svg {
  flex-shrink: 0;
}

.detail-interactions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.interact-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.interact-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--border-hover);
}

.interact-btn.active {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-bg);
}

.interact-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Detail */
.post-detail {
  display: grid;
  gap: 16px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  width: fit-content;
}

.btn-back:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.detail-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 28px;
  display: grid;
  gap: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--text-muted);
}

.detail-author {
  font-weight: 600;
  color: var(--text-secondary);
}

.detail-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.detail-content {
  font-size: 15px;
  line-height: 1.75;
  color: var(--text);
  white-space: pre-wrap;
}

/* Replies */
.replies-section {
  display: grid;
  gap: 12px;
}

.replies-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.reply-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px 20px;
  display: grid;
  gap: 10px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reply-author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reply-author {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}

.reply-date {
  font-size: 12px;
  color: var(--text-light);
}

.reply-actions {
  display: flex;
  gap: 2px;
}

.reply-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text);
  white-space: pre-wrap;
}

.reply-form {
  display: grid;
  gap: 10px;
  padding: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
}

.loading-state {
  display: grid;
  place-items: center;
  padding: 48px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2.5px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  padding: 40px 16px;
  text-align: center;
}

.empty-state p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* Transitions */
.fade-slide-enter-active {
  animation: fadeSlideIn 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.fade-slide-leave-active {
  animation: fadeSlideOut 0.25s ease;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeSlideOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-8px); }
}
</style>
