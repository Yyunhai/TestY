<template>
  <div class="dashboard" ref="dashboard">
    <div class="welcome-banner" ref="welcomeBanner">
      <div class="welcome-content">
        <h2 class="welcome-title" ref="welcomeTitle">你好，{{ user.displayName || user.username }}！</h2>
        <p class="welcome-text" ref="welcomeText">欢迎回到 TestY 工作台，这是你的工作概览。</p>
      </div>
      <div class="welcome-decoration" ref="welcomeDeco">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="stats-row" ref="statsRow">
        <div class="stat-card" ref="statDocs">
          <div class="stat-icon docs-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M5 3C5 2.44772 5.44772 2 6 2H13L19 8V21C19 21.5523 18.5523 22 18 22H6C5.44772 22 5 21.5523 5 21V3Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M13 2V8H19" stroke="currentColor" stroke-width="1.5"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value" ref="statDocsValue">{{ documents.length }}</span>
            <span class="stat-label">文档总数</span>
          </div>
        </div>
        <div class="stat-card" ref="statRoles">
          <div class="stat-icon roles-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M16 21V19C16 16.7909 14.2091 15 12 15H5C2.79086 15 1 16.7909 1 19V21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="8.5" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              <path d="M20 8V14M23 11H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value" ref="statRolesValue">{{ (user.roles || []).length }}</span>
            <span class="stat-label">当前角色</span>
          </div>
        </div>
        <div class="stat-card" ref="statPerms">
          <div class="stat-icon perm-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L3 7V12C3 16.97 6.84 21.61 12 22.74C17.16 21.61 21 16.97 21 12V7L12 2Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value" ref="statPermsValue">{{ (user.permissions || []).length }}</span>
            <span class="stat-label">权限数量</span>
          </div>
        </div>
      </div>

      <div class="content-row">
        <div class="card recent-docs" ref="recentDocs">
          <div class="card-header">
            <div>
              <p class="card-eyebrow">最近文档</p>
              <h3 class="card-title">文档动态</h3>
            </div>
            <button v-if="canReadDocs" class="btn-ghost" type="button" @click="$emit('navigate', 'docs')">查看全部</button>
          </div>
          <div class="doc-list">
            <div v-for="(doc, i) in recentDocuments" :key="doc.id" class="doc-item" :ref="el => setDocItemRef(el, i)" @click="$emit('open-doc', doc.id)">
              <div class="doc-icon">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <path d="M3 2C3 1.44772 3.44772 1 4 1H10L15 6V16C15 16.5523 14.5523 17 14 17H4C3.44772 17 3 16.5523 3 16V2Z" stroke="currentColor" stroke-width="1.2"/>
                  <path d="M10 1V6H15" stroke="currentColor" stroke-width="1.2"/>
                </svg>
              </div>
              <div class="doc-info">
                <span class="doc-title">{{ doc.title || '未命名文档' }}</span>
              </div>
            </div>
            <div v-if="!documents.length" class="empty-state">
              <p>当前还没有文档，去文档中心创建第一篇吧！</p>
            </div>
          </div>
        </div>

        <div class="card quick-info" ref="quickInfo">
          <div class="card-header">
            <div>
              <p class="card-eyebrow">账号信息</p>
              <h3 class="card-title">当前会话</h3>
            </div>
          </div>
          <div class="info-list">
            <div class="info-row" v-for="(row, i) in infoRows" :key="i" :ref="el => setInfoRowRef(el, i)">
              <span class="info-label">{{ row.label }}</span>
              <span class="info-value" v-html="row.value"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { gsap } from "gsap";

export default {
  name: "DashboardPage",
  props: {
    user: { type: Object, default: () => ({}) },
    documents: { type: Array, default: () => [] },
    canReadDocs: { type: Boolean, default: false }
  },
  emits: ["navigate", "open-doc"],
  data() {
    return {
      docItems: [],
      infoRowRefs: []
    };
  },
  computed: {
    recentDocuments() {
      return this.documents.slice(0, 5);
    },
    infoRows() {
      return [
        { label: "用户名", value: this.user.username || '--' },
        { label: "显示名称", value: this.user.displayName || '--' },
        { label: "角色", value: (this.user.roles || []).length ? (this.user.roles || []).map(r => `<span class="tag">${r}</span>`).join('') : '--' },
        { label: "权限数", value: `${(this.user.permissions || []).length} 项` },
        { label: "超级管理员", value: `<span class="badge ${this.user.rootAdmin ? 'yes' : 'no'}">${this.user.rootAdmin ? '是' : '否'}</span>` }
      ];
    }
  },
  mounted() {
    this.animateDashboard();
  },
  methods: {
    setDocItemRef(el, i) {
      if (el) this.docItems[i] = el;
    },
    setInfoRowRef(el, i) {
      if (el) this.infoRowRefs[i] = el;
    },
    animateDashboard() {
      const tl = gsap.timeline({ defaults: { ease: "power2.out" } });

      tl.fromTo(this.$refs.welcomeBanner, { opacity: 0, scale: 0.95 }, { opacity: 1, scale: 1, duration: 0.5, clearProps: "transform" })
        .fromTo(this.$refs.welcomeTitle, { opacity: 0, y: 20 }, { opacity: 1, y: 0, duration: 0.4 }, "-=0.3")
        .fromTo(this.$refs.welcomeText, { opacity: 0, y: 10 }, { opacity: 1, y: 0, duration: 0.3 }, "-=0.2");

      const stats = [this.$refs.statDocs, this.$refs.statRoles, this.$refs.statPerms].filter(Boolean);
      tl.fromTo(stats, { opacity: 0, y: 30 }, { opacity: 1, y: 0, stagger: 0.1, duration: 0.45, clearProps: "transform" }, "-=0.1");

      const statValues = [this.$refs.statDocsValue, this.$refs.statRolesValue, this.$refs.statPermsValue].filter(Boolean);
      statValues.forEach((el) => {
        const finalVal = parseInt(el.textContent, 10) || 0;
        el.textContent = "0";
        tl.call(() => {
          gsap.to(el, { duration: 0.8, ease: "power2.out", innerText: finalVal, snap: { innerText: 1 } });
        }, [], "-=0.6");
      });

      const cards = [this.$refs.recentDocs, this.$refs.quickInfo].filter(Boolean);
      tl.fromTo(cards, { opacity: 0, y: 20 }, { opacity: 1, y: 0, stagger: 0.12, duration: 0.4, clearProps: "transform" }, "-=0.1");

      const docItems = this.docItems.filter(Boolean);
      if (docItems.length) {
        tl.fromTo(docItems, { opacity: 0, x: -15 }, { opacity: 1, x: 0, stagger: 0.06, duration: 0.3, clearProps: "transform" }, "-=0.1");
      }

      const infoRows = this.infoRowRefs.filter(Boolean);
      if (infoRows.length) {
        tl.fromTo(infoRows, { opacity: 0, x: -10 }, { opacity: 1, x: 0, stagger: 0.05, duration: 0.25, clearProps: "transform" }, "-=0.1");
      }
    }
  }
};
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 24px;
}

.welcome-banner {
  background: linear-gradient(135deg, var(--accent) 0%, #d4622a 100%);
  border-radius: 20px;
  padding: 36px 32px;
  position: relative;
  overflow: hidden;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}

.welcome-text {
  margin: 0;
  font-size: 15px;
  color: rgba(255,255,255,0.85);
}

.welcome-decoration {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 200px;
  height: 200px;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
}

.c1 { width: 160px; height: 160px; right: 0; top: 0; }
.c2 { width: 100px; height: 100px; right: 40px; top: 60px; }
.c3 { width: 60px; height: 60px; right: 10px; top: 100px; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.docs-icon { background: rgba(194, 65, 12, 0.1); color: var(--accent); }
.roles-icon { background: rgba(92, 130, 104, 0.1); color: var(--success); }
.perm-icon { background: rgba(94, 120, 143, 0.1); color: #5e788f; }

.stat-info {
  display: grid;
  gap: 2px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
}

.content-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
}

.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.card-eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.btn-ghost {
  padding: 8px 16px;
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

.doc-list {
  display: grid;
  gap: 8px;
}

.doc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.doc-item:hover {
  background: var(--bg-secondary);
}

.doc-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--accent-bg);
  color: var(--accent);
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  padding: 32px 16px;
  text-align: center;
}

.empty-state p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}

.info-list {
  display: grid;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: var(--text-muted);
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 10px;
  border-radius: 8px;
  background: var(--accent-bg);
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
}

.badge {
  padding: 3px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
}

.badge.yes {
  background: rgba(92, 130, 104, 0.1);
  color: var(--success);
}

.badge.no {
  background: var(--bg-secondary);
  color: var(--text-muted);
}

@media (max-width: 1024px) {
  .stats-row { grid-template-columns: 1fr; }
  .content-row { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .welcome-banner { padding: 24px 20px; }
  .welcome-title { font-size: 22px; }
}
</style>
