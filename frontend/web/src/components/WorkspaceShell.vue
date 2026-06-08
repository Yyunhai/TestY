<template>
  <div class="workspace">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }" ref="sidebar">
      <div class="sidebar-header">
        <div class="sidebar-brand">
          <img class="brand-icon" src="../assets/logo2.png" alt="TestY" />
          <span class="brand-text" v-show="!sidebarCollapsed">TestY</span>
        </div>
        <button class="sidebar-toggle" type="button" @click="$emit('toggle-sidebar')">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M11 4L6 9L11 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <nav class="sidebar-nav" ref="sidebarNav">
        <button
          type="button"
          class="nav-item"
          :class="{ active: currentPage === 'dashboard' }"
          @click="$emit('navigate', 'dashboard')"
          ref="navDashboard"
        >
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <rect x="2" y="2" width="7" height="7" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <rect x="11" y="2" width="7" height="7" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <rect x="2" y="11" width="7" height="7" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <rect x="11" y="11" width="7" height="7" rx="2" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">概览</span>
        </button>

        <button
          v-if="canReadDocs"
          type="button"
          class="nav-item"
          :class="{ active: currentPage === 'docs' }"
          @click="$emit('navigate', 'docs')"
          ref="navDocs"
        >
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M4 3C4 2.44772 4.44772 2 5 2H11L16 7V17C16 17.5523 15.5523 18 15 18H5C4.44772 18 4 17.5523 4 17V3Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M11 2V7H16" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 11H13M7 14H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">文档</span>
        </button>

        <button
          v-if="canReadDiscussion"
          type="button"
          class="nav-item"
          :class="{ active: currentPage === 'community' }"
          @click="$emit('navigate', 'community')"
          ref="navCommunity"
        >
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M14 3C14 2.44772 13.5523 2 13 2H7C6.44772 2 6 2.44772 6 3V8L4 10V16C4 16.5523 4.44772 17 5 17H15C15.5523 17 16 16.5523 16 16V10L14 8V3Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M8 12H12M8 15H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">社区</span>
        </button>

        <button
          type="button"
          class="nav-item"
          :class="{ active: currentPage === 'profile' }"
          @click="$emit('navigate', 'profile')"
          ref="navProfile"
        >
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M16 17C16 14.8 13.3 13 10 13C6.7 13 4 14.8 4 17M10 10C11.7 10 13 8.7 13 7C13 5.3 11.7 4 10 4C8.3 4 7 5.3 7 7C7 8.7 8.3 10 10 10Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">我的</span>
        </button>

        <button
          v-if="canAccessAdmin"
          type="button"
          class="nav-item"
          :class="{ active: currentPage === 'admin' }"
          @click="$emit('navigate', 'admin')"
          ref="navAdmin"
        >
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 2L3 6V12C3 15.3 6.03 18.36 10 19.24C13.97 18.36 17 15.3 17 12V6L10 2Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">后台管理</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card" v-show="!sidebarCollapsed">
          <div class="user-avatar">{{ (user.displayName || user.username || '?').charAt(0).toUpperCase() }}</div>
          <div class="user-info">
            <span class="user-name">{{ user.displayName || user.username }}</span>
            <span class="user-role">{{ (user.roles || []).join(', ') || '无角色' }}</span>
          </div>
        </div>
        <button class="nav-item logout-btn" type="button" @click="$emit('logout')">
          <span class="nav-icon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M7 2H5C4.44772 2 4 2.44772 4 3V17C4 17.5523 4.44772 18 5 18H7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M13 14L17 10L13 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M17 10H8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="nav-label" v-show="!sidebarCollapsed">退出登录</span>
        </button>
      </div>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <h1 class="topbar-title">{{ pageTitle }}</h1>
          <span class="topbar-breadcrumb" v-if="adminSection && currentPage === 'admin'">
            <span class="breadcrumb-sep">/</span>
            {{ adminSectionLabel }}
          </span>
        </div>
        <div class="topbar-right">
          <div class="topbar-meta">
            <span v-if="user.lastLoginAt" class="meta-item">最近登录: {{ formatDateTime(user.lastLoginAt) }}</span>
          </div>
        </div>
      </header>

      <Transition name="toast">
        <div v-if="message.text" class="toast" :class="message.type" ref="toast">
          <span class="toast-icon">{{ message.type === 'error' ? '!' : message.type === 'success' ? '✓' : 'i' }}</span>
          <span class="toast-text">{{ message.text }}</span>
        </div>
      </Transition>

      <main class="content" ref="content">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script>
import { gsap } from "gsap";

export default {
  name: "WorkspaceShell",
  props: {
    user: { type: Object, default: () => ({}) },
    currentPage: { type: String, default: "dashboard" },
    adminSection: { type: String, default: "overview" },
    message: { type: Object, default: () => ({ type: "info", text: "" }) },
    sidebarCollapsed: { type: Boolean, default: false },
    canReadDocs: { type: Boolean, default: false },
    canReadDiscussion: { type: Boolean, default: false },
    canAccessAdmin: { type: Boolean, default: false }
  },
  emits: ["navigate", "logout", "toggle-sidebar"],
  mounted() {
    this.animateSidebar();
  },
  computed: {
    pageTitle() {
      const map = { dashboard: "工作台", docs: "文档中心", community: "社区讨论", profile: "我的", admin: "后台管理" };
      return map[this.currentPage] || "TestY";
    },
    adminSectionLabel() {
      const map = { overview: "安全态势", users: "用户管理", roles: "角色权限", audits: "登录审计", operations: "操作日志" };
      return map[this.adminSection] || "";
    }
  },
  methods: {
    animateSidebar() {
      const items = [
        this.$refs.navDashboard,
        ...(this.$refs.navDocs ? [this.$refs.navDocs] : []),
        ...(this.$refs.navCommunity ? [this.$refs.navCommunity] : []),
        ...(this.$refs.navProfile ? [this.$refs.navProfile] : []),
        ...(this.$refs.navAdmin ? [this.$refs.navAdmin] : [])
      ].filter(Boolean);
      gsap.fromTo(items, { opacity: 0, x: -20 }, { opacity: 1, x: 0, stagger: 0.08, duration: 0.4, ease: "power2.out", clearProps: "transform" });
    },
    formatDateTime(value) {
      if (!value) return "--";
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return value;
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, "0");
      const d = String(date.getDate()).padStart(2, "0");
      const h = String(date.getHours()).padStart(2, "0");
      const min = String(date.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${d} ${h}:${min}`;
    }
  }
};
</script>

<style scoped>
.workspace {
  display: flex;
  min-height: 100vh;
  background: var(--bg);
}

.sidebar {
  width: 260px;
  background: var(--surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  z-index: 20;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
}

.sidebar-brand .brand-icon {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 10px;
  object-fit: contain;
}

.sidebar-brand .brand-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
}

.sidebar-toggle {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all 0.2s;
}

.sidebar-toggle:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.sidebar.collapsed .sidebar-toggle svg {
  transform: rotate(180deg);
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: grid;
  gap: 4px;
  align-content: start;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.nav-item.active {
  background: var(--accent-bg);
  color: var(--accent);
}

.nav-icon {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 12px;
}

.sidebar-footer {
  padding: 12px 10px;
  border-top: 1px solid var(--border);
  display: grid;
  gap: 4px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--bg-secondary);
  margin-bottom: 4px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 10px;
  background: var(--accent);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 600;
  font-size: 15px;
}

.user-info {
  display: grid;
  gap: 2px;
  overflow: hidden;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-btn:hover {
  color: var(--danger);
  background: var(--danger-bg);
}

.main-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  padding: 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
  background: var(--surface);
  position: sticky;
  top: 0;
  z-index: 10;
}

.topbar-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.topbar-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.3px;
}

.topbar-breadcrumb {
  font-size: 14px;
  color: var(--text-muted);
}

.breadcrumb-sep {
  margin-right: 4px;
  color: var(--text-light);
}

.topbar-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  font-size: 13px;
  color: var(--text-muted);
}

.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  z-index: 1000;
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(8px);
}

.toast.info {
  background: rgba(94, 120, 143, 0.95);
  color: #fff;
}

.toast.success {
  background: rgba(92, 130, 104, 0.95);
  color: #fff;
}

.toast.error {
  background: rgba(194, 65, 12, 0.95);
  color: #fff;
}

.toast-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255,255,255,0.2);
  flex-shrink: 0;
}

.toast-enter-active { animation: toast-in 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.toast-leave-active { animation: toast-out 0.3s ease; }

@keyframes toast-in {
  from { opacity: 0; transform: translateX(40px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes toast-out {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(40px); }
}

.content {
  flex: 1;
  padding: 28px 32px;
}

@media (max-width: 1024px) {
  .sidebar { width: 72px; }
  .sidebar .nav-label,
  .sidebar .brand-text,
  .sidebar .user-card { display: none !important; }
  .sidebar .nav-item { justify-content: center; padding: 12px; }
  .sidebar .sidebar-header { justify-content: center; }
  .sidebar .sidebar-toggle { display: none; }
  .content { padding: 20px 16px; }
  .topbar { padding: 16px; }
}

@media (max-width: 768px) {
  .sidebar { position: fixed; left: 0; top: 0; }
  .workspace { padding-left: 72px; }
}
</style>
