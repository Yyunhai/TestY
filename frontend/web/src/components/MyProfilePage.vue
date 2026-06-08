<template>
  <div class="profile" ref="profile">
    <div class="profile-header">
      <div class="profile-avatar">{{ displayName.charAt(0).toUpperCase() }}</div>
      <div class="profile-info">
        <h2 class="profile-name">{{ displayName }}</h2>
        <p class="profile-username">@{{ user.username }}</p>
        <div class="profile-meta">
          <span>角色: {{ (user.roles || []).join(', ') || '无' }}</span>
          <span v-if="user.lastLoginAt">最近登录: {{ formatDateTime(user.lastLoginAt) }}</span>
        </div>
      </div>
    </div>

    <div class="profile-tabs">
      <button class="tab-btn" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'; loadTab()">我的帖子</button>
      <button class="tab-btn" :class="{ active: activeTab === 'likes' }" @click="activeTab = 'likes'; loadTab()">我的点赞</button>
      <button class="tab-btn" :class="{ active: activeTab === 'bookmarks' }" @click="activeTab = 'bookmarks'; loadTab()">我的收藏</button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <template v-else>
      <div v-if="items.length" class="posts-list">
        <div v-for="post in items" :key="post.id" class="post-card" @click="viewPost(post)">
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
              <span class="post-stat" :class="{ active: post.likedByCurrentUser }">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 12L6.1 11.2C3.4 8.7 1.5 7 1.5 5C1.5 3.3 2.8 2 4.5 2C5.5 2 6.4 2.5 7 3.2C7.6 2.5 8.5 2 9.5 2C11.2 2 12.5 3.3 12.5 5C12.5 7 10.6 8.7 7.9 11.2L7 12Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
                {{ post.likeCount }}
              </span>
              <span class="post-stat" :class="{ active: post.bookmarkedByCurrentUser }">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 1.5V12.5L7 10L11 12.5V1.5H3Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg>
                {{ post.bookmarkCount }}
              </span>
              <span class="post-stat">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 5H11M3 8H8M3 11H6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
                {{ post.replyCount }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>{{ emptyText }}</p>
      </div>
    </template>
  </div>
</template>

<script>
import { gsap } from "gsap";

export default {
  name: "MyProfilePage",
  props: {
    user: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      activeTab: "posts",
      items: [],
      loading: false
    };
  },
  computed: {
    displayName() {
      return this.user.displayName || this.user.username || "未知用户";
    },
    emptyText() {
      const map = { posts: "暂无帖子", likes: "暂无点赞", bookmarks: "暂无收藏" };
      return map[this.activeTab] || "暂无内容";
    }
  },
  mounted() {
    this.loadTab();
  },
  methods: {
    async loadTab() {
      this.loading = true;
      try {
        const map = { posts: "/api/discussions/my/posts", likes: "/api/discussions/my/likes", bookmarks: "/api/discussions/my/bookmarks" };
        this.items = await this.apiRequest(map[this.activeTab]);
        this.$nextTick(() => this.animateCards());
      } catch (err) {
        this.items = [];
      } finally {
        this.loading = false;
      }
    },
    animateCards() {
      const cards = this.$el.querySelectorAll(".post-card");
      if (!cards.length) return;
      gsap.fromTo(cards, { opacity: 0, y: 15 }, { opacity: 1, y: 0, stagger: 0.05, duration: 0.35, ease: "power2.out", clearProps: "transform" });
    },
    viewPost(post) {
      window.location.hash = `#community?post=${post.id}`;
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
.profile {
  display: grid;
  gap: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  min-width: 64px;
  border-radius: 16px;
  background: var(--accent);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 700;
}

.profile-info {
  display: grid;
  gap: 4px;
}

.profile-name {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}

.profile-username {
  margin: 0;
  font-size: 14px;
  color: var(--text-muted);
}

.profile-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
  flex-wrap: wrap;
}

.profile-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.tab-btn {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: var(--text);
}

.tab-btn.active {
  background: var(--surface);
  color: var(--text);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
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

.post-stat {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-muted);
}

.post-stat.active {
  color: var(--accent);
}

.post-stat svg {
  flex-shrink: 0;
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
</style>
