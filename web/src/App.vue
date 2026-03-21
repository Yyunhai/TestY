<template>
  <main class="page" :class="themeClass">
    <section v-if="!currentUser" class="auth">
      <section class="panel">
        <p class="eyebrow">TestY</p>
        <h1>登录后进入工作台，再进入独立文档页写 Markdown。</h1>
        <p class="muted">工作台只展示账户和项目概览，文档页单独负责列表、编辑和实时预览。</p>
      </section>

      <section class="panel">
        <div class="header-row">
          <div>
            <p class="eyebrow">账户中心</p>
            <h2>{{ authTitle }}</h2>
          </div>
          <button class="ghost" type="button" @click="toggleTheme">{{ isDarkMode ? "浅色模式" : "深色模式" }}</button>
        </div>

        <div class="tabs">
          <button v-for="item in tabs" :key="item.key" class="tab" :class="{ active: activeTab === item.key }" @click="switchTab(item.key)">
            {{ item.label }}
          </button>
        </div>

        <p v-if="message.text" class="message" :class="message.type">{{ message.text }}</p>

        <form v-if="activeTab === 'login'" class="form" @submit.prevent="submitLogin">
          <input v-model.trim="loginForm.usernameOrEmail" type="text" placeholder="用户名或邮箱" />
          <input v-model="loginForm.password" type="password" placeholder="密码" />
          <button class="primary" type="submit" :disabled="busy">{{ busy ? "登录中..." : "登录" }}</button>
        </form>

        <form v-else-if="activeTab === 'register'" class="form" @submit.prevent="submitRegister">
          <input v-model.trim="registerForm.username" type="text" placeholder="用户名，不少于 6 位" />
          <input v-model.trim="registerForm.email" type="email" placeholder="邮箱" />
          <input v-model.trim="registerForm.displayName" type="text" placeholder="显示名" />
          <input v-model.trim="registerForm.phoneNumber" type="text" placeholder="手机号（可选）" />
          <input v-model="registerForm.password" type="password" placeholder="密码，不少于 6 位" />
          <button class="primary" type="submit" :disabled="busy">{{ busy ? "提交中..." : "注册账户" }}</button>
        </form>

        <form v-else class="form" @submit.prevent="submitReset">
          <input v-model.trim="resetForm.username" type="text" placeholder="用户名" />
          <input v-model.trim="resetForm.email" type="email" placeholder="注册邮箱" />
          <input v-model="resetForm.newPassword" type="password" placeholder="新密码" />
          <button class="primary" type="submit" :disabled="busy">{{ busy ? "提交中..." : "重置密码" }}</button>
        </form>
      </section>
    </section>

    <section v-else-if="currentPage === 'dashboard'" class="workspace">
      <section class="panel">
        <div class="header-row">
          <div>
            <p class="eyebrow">Workspace</p>
            <h1>{{ currentUser.displayName || currentUser.username }} 的工作台</h1>
            <p class="muted">角色：{{ currentUser.roles.join(" / ") }}，最近登录 IP：{{ currentUser.lastLoginIp || "暂无" }}</p>
          </div>
          <div class="actions">
            <button class="ghost" type="button" @click="toggleTheme">{{ isDarkMode ? "浅色模式" : "深色模式" }}</button>
            <button class="ghost" type="button" @click="refreshWorkspace" :disabled="workspaceLoading">{{ workspaceLoading ? "刷新中..." : "刷新" }}</button>
            <button class="danger" type="button" @click="logout">退出</button>
          </div>
        </div>
      </section>

      <section class="dashboard-grid">
        <section class="panel">
          <p class="eyebrow">账户</p>
          <div class="metric"><span>用户名</span><strong>{{ currentUser.username }}</strong></div>
          <div class="metric"><span>邮箱</span><strong>{{ currentUser.email }}</strong></div>
          <div class="chips">
            <span v-for="permission in currentUser.permissions" :key="permission" class="chip">{{ permission }}</span>
          </div>
        </section>

        <section class="panel">
          <div class="header-row compact">
            <div>
              <p class="eyebrow">项目概览</p>
              <h2>运行状态</h2>
            </div>
            <button class="ghost small" type="button" @click="loadOverview" :disabled="overviewLoading">{{ overviewLoading ? "加载中..." : "刷新概览" }}</button>
          </div>
          <p v-if="overviewError" class="message error">{{ overviewError }}</p>
          <div v-else class="form">
            <div class="metric"><span>应用名称</span><strong>{{ overview.applicationName || "暂无" }}</strong></div>
            <div class="metric"><span>Java 版本</span><strong>{{ overview.javaVersion || "暂无" }}</strong></div>
            <p class="muted">{{ overview.message || "暂无" }}</p>
            <div class="chips">
              <span v-for="moduleName in overview.modules" :key="moduleName" class="chip">{{ moduleName }}</span>
            </div>
          </div>
        </section>

        <section class="panel docs-entry">
          <p class="eyebrow">Markdown 文档</p>
          <h2>进入文档页</h2>
          <p class="muted">当前共有 {{ documents.length }} 篇文档。进入后可以新建草稿、继续编辑和保存。</p>
          <div class="doc-summary-list">
            <div v-for="item in recentDocuments" :key="item.id" class="doc-summary">
              <strong>{{ item.title }}</strong>
            </div>
            <div v-if="!recentDocuments.length" class="empty">还没有文档，点击下方按钮开始。</div>
          </div>
          <button class="primary full" type="button" @click="openDocsPage(false)">编写文档</button>
        </section>
      </section>
    </section>

    <section v-else class="workspace">
      <section class="panel">
        <div class="header-row">
          <div>
            <p class="eyebrow">Markdown Workspace</p>
            <h1>文档编写页</h1>
            <p class="muted">单独的写作页面，左侧是文档列表，右侧是编辑器和预览。</p>
          </div>
          <div class="actions">
            <button class="ghost" type="button" @click="goToPage('dashboard')">返回工作台</button>
            <button class="ghost" type="button" @click="toggleTheme">{{ isDarkMode ? "浅色模式" : "深色模式" }}</button>
            <button class="danger" type="button" @click="logout">退出</button>
          </div>
        </div>
      </section>

      <section class="docs-layout">
        <aside class="panel">
          <div class="header-row compact">
            <div>
              <p class="eyebrow">我的文档</p>
              <h2>文档列表</h2>
            </div>
            <button class="primary small" type="button" @click="startDraft">新建</button>
          </div>

          <p v-if="documentMessage.text" class="message" :class="documentMessage.type">{{ documentMessage.text }}</p>

          <div class="doc-list">
            <button v-for="item in documents" :key="item.id" class="doc-card" :class="{ active: selectedDocumentId === item.id }" @click="selectDocument(item.id)">
              <strong>{{ item.title }}</strong>
            </button>
            <div v-if="!documents.length" class="empty">还没有文档，点击“新建”开始。</div>
          </div>
        </aside>

        <section class="panel">
          <div class="header-row compact">
            <div>
              <p class="eyebrow">编辑器</p>
              <h2>{{ selectedDocumentId ? "编辑文档" : "新建文档" }}</h2>
            </div>
            <div class="actions">
              <span v-if="draftNotice" class="badge">{{ draftNotice }}</span>
              <button class="primary small" type="button" @click="saveDocument" :disabled="documentBusy">{{ documentBusy ? "保存中..." : "保存文档" }}</button>
            </div>
          </div>

          <div class="editor-grid">
            <section class="form">
              <input ref="titleInput" v-model.trim="documentForm.title" type="text" maxlength="120" placeholder="文档标题" />
              <textarea v-model="documentForm.content" placeholder="# 开始写作&#10;&#10;在这里输入 Markdown 内容"></textarea>
            </section>

            <section>
              <div class="preview-head">
                <span>实时预览</span>
                <small>{{ documentForm.content.length }} 个字符</small>
              </div>
              <div class="preview" v-html="renderedMarkdown"></div>
            </section>
          </div>
        </section>
      </section>
    </section>
  </main>
</template>

<script>
import { nextTick } from "vue";

// 本地缓存的主题键，用于在刷新页面后恢复明暗模式。

const THEME_KEY = "testy-theme";

// 工作台概览的默认空状态，避免页面初次渲染时出现空引用。

function emptyOverview() {
  return { applicationName: "", javaVersion: "", message: "", modules: [] };
}

// 新建文档时使用的默认草稿内容。

function emptyDocumentForm() {
  return { title: "未命名文档", content: "# 新文档\n\n在这里开始写 Markdown。" };
}

// 将 401 场景显式标记出来，便于统一走登录态失效流程。

function createAuthError(message) {
  const error = new Error(message || "Authentication required.");
  error.isAuthError = true;
  return error;
}

// 预览渲染前先做 HTML 转义，避免用户输入被当作原始 HTML 注入页面。

function escapeHtml(value) {
  return (value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

// 只允许安全链接协议进入预览区域。

function sanitizeUrl(url) {
  const value = (url || "").trim();
  return /^(https?:\/\/|mailto:|#)/i.test(value) ? value : "#";
}

// 处理行内 Markdown 语法，例如粗体、斜体、行内代码和链接。

function inlineMarkdown(value) {
  let html = escapeHtml(value);
  html = html.replace(/`([^`]+)`/g, "<code>$1</code>");
  html = html.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  html = html.replace(/\*([^*]+)\*/g, "<em>$1</em>");
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_, text, url) => `<a href="${sanitizeUrl(url)}" target="_blank" rel="noreferrer">${text}</a>`);
  return html;
}

// 轻量级 Markdown 渲染器，覆盖当前编辑器所需的常见语法。

function renderMarkdown(markdown) {
  const source = (markdown || "").replace(/\r\n/g, "\n");
  if (!source.trim()) return "<p>这里会显示你正在编写的 Markdown 预览。</p>";

  const lines = source.split("\n");
  const html = [];
  let paragraph = [];
  let bullets = [];
  let ordered = [];
  let inCode = false;
  let codeLines = [];

  function flushParagraph() {
    if (!paragraph.length) return;
    html.push(`<p>${paragraph.map(inlineMarkdown).join("<br />")}</p>`);
    paragraph = [];
  }

  function flushBullets() {
    if (!bullets.length) return;
    html.push(`<ul>${bullets.join("")}</ul>`);
    bullets = [];
  }

  function flushOrdered() {
    if (!ordered.length) return;
    html.push(`<ol>${ordered.join("")}</ol>`);
    ordered = [];
  }

  function flushCode() {
    if (!inCode) return;
    html.push(`<pre><code>${escapeHtml(codeLines.join("\n"))}</code></pre>`);
    inCode = false;
    codeLines = [];
  }

  lines.forEach((line) => {
    if (line.trim().startsWith("```")) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      if (inCode) flushCode();
      else {
        inCode = true;
        codeLines = [];
      }
      return;
    }

    if (inCode) {
      codeLines.push(line);
      return;
    }

    if (!line.trim()) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      return;
    }

    if (/^---+$/.test(line.trim())) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      html.push("<hr />");
      return;
    }

    const heading = line.match(/^(#{1,6})\s+(.*)$/);
    if (heading) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      const level = heading[1].length;
      html.push(`<h${level}>${inlineMarkdown(heading[2].trim())}</h${level}>`);
      return;
    }

    const quote = line.match(/^>\s?(.*)$/);
    if (quote) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      html.push(`<blockquote><p>${inlineMarkdown(quote[1])}</p></blockquote>`);
      return;
    }

    const bullet = line.match(/^[-*]\s+(.*)$/);
    if (bullet) {
      flushParagraph();
      flushOrdered();
      bullets.push(`<li>${inlineMarkdown(bullet[1])}</li>`);
      return;
    }

    const num = line.match(/^\d+\.\s+(.*)$/);
    if (num) {
      flushParagraph();
      flushBullets();
      ordered.push(`<li>${inlineMarkdown(num[1])}</li>`);
      return;
    }

    paragraph.push(line.trim());
  });

  if (inCode) flushCode();
  flushParagraph();
  flushBullets();
  flushOrdered();
  return html.join("");
}

// 通过 hash 切换工作台和文档页，避免引入额外路由依赖。

function resolvePageFromHash() {
  return window.location.hash === "#/docs" ? "docs" : "dashboard";
}

export default {
  name: "App",
  data() {
    return {
      tabs: [
        { key: "login", label: "登录" },
        { key: "register", label: "注册" },
        { key: "reset", label: "找回密码" }
      ],
      activeTab: "login",
      currentPage: "dashboard",
      busy: false,
      workspaceLoading: false,
      overviewLoading: false,
      documentBusy: false,
      theme: "light",
      message: { type: "info", text: "" },
      documentMessage: { type: "info", text: "" },
      draftNotice: "",
      currentUser: null,
      overview: emptyOverview(),
      overviewError: "",
      documents: [],
      selectedDocumentId: null,
      documentForm: emptyDocumentForm(),
      loginForm: { usernameOrEmail: "", password: "" },
      registerForm: { username: "", email: "", displayName: "", phoneNumber: "", password: "" },
      resetForm: { username: "", email: "", newPassword: "" }
    };
  },
  computed: {
    authTitle() {
      if (this.activeTab === "register") return "创建账户";
      if (this.activeTab === "reset") return "重置密码";
      return "登录到 TestY";
    },
    isDarkMode() {
      return this.theme === "dark";
    },
    themeClass() {
      return this.isDarkMode ? "theme-dark" : "theme-light";
    },
    renderedMarkdown() {
      return renderMarkdown(this.documentForm.content);
    },
    recentDocuments() {
      return this.documents.slice(0, 3);
    }
  },
  mounted() {
    this.theme = this.resolveTheme();
    this.syncPageFromHash();
    this._hashHandler = () => this.syncPageFromHash();
    window.addEventListener("hashchange", this._hashHandler);
    this.restoreSession();
  },
  beforeUnmount() {
    window.removeEventListener("hashchange", this._hashHandler);
  },
  methods: {
    resolveTheme() {
      const savedTheme = window.localStorage.getItem(THEME_KEY);
      if (savedTheme === "dark" || savedTheme === "light") return savedTheme;
      return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    },
    toggleTheme() {
      this.theme = this.isDarkMode ? "light" : "dark";
      window.localStorage.setItem(THEME_KEY, this.theme);
    },
    switchTab(tab) {
      this.activeTab = tab;
      this.message = { type: "info", text: "" };
    },
    syncPageFromHash() {
      this.currentPage = resolvePageFromHash();
    },
    goToPage(page) {
      window.location.hash = page === "docs" ? "#/docs" : "#/dashboard";
      this.currentPage = page;
    },
    async openDocsPage(createDraft) {
      this.goToPage("docs");
      await this.loadDocuments();
      if (createDraft) {
        await this.startDraft();
      }
    },
    async focusTitle() {
      await nextTick();
      if (this.$refs.titleInput) {
        this.$refs.titleInput.focus();
        this.$refs.titleInput.select();
      }
    },
    mapUser(data) {
      return {
        username: data.username,
        email: data.email,
        displayName: data.displayName,
        roles: data.roles || [],
        permissions: data.permissions || [],
        lastLoginIp: data.lastLoginIp
      };
    },
    clearWorkspaceState() {
      this.currentUser = null;
      this.overview = emptyOverview();
      this.overviewError = "";
      this.documents = [];
      this.selectedDocumentId = null;
      this.documentForm = emptyDocumentForm();
      this.documentMessage = { type: "info", text: "" };
      this.draftNotice = "";
    },
    async restoreSession() {
      try {
        const data = await this.requestJson("/api/auth/me", { method: "GET", expectEnvelope: true });
        this.currentUser = this.mapUser(data);
        await this.refreshWorkspace();
      } catch (error) {
        if (error.isAuthError) {
          this.clearWorkspaceState();
          return;
        }
        this.message = { type: "error", text: error.message };
      }
    },
    async refreshWorkspace() {
      this.workspaceLoading = true;
      try {
        await Promise.all([this.loadOverview(), this.loadDocuments()]);
      } finally {
        this.workspaceLoading = false;
      }
    },
    async submitLogin() {
      this.busy = true;
      this.message = { type: "info", text: "" };
      try {
        const data = await this.postJson("/api/auth/login", this.loginForm);
        this.currentUser = this.mapUser(data);
        this.loginForm.password = "";
        this.message = { type: "success", text: data.message };
        this.goToPage("dashboard");
        await this.refreshWorkspace();
      } catch (error) {
        this.message = { type: "error", text: error.message };
      } finally {
        this.busy = false;
      }
    },
    async submitRegister() {
      this.busy = true;
      this.message = { type: "info", text: "" };
      try {
        const data = await this.postJson("/api/auth/register", this.registerForm);
        this.message = { type: "success", text: data.message + " 现在可以直接登录。" };
        this.loginForm.usernameOrEmail = this.registerForm.username;
        this.resetForm.username = this.registerForm.username;
        this.resetForm.email = this.registerForm.email;
        this.registerForm = { username: "", email: "", displayName: "", phoneNumber: "", password: "" };
        this.activeTab = "login";
      } catch (error) {
        this.message = { type: "error", text: error.message };
      } finally {
        this.busy = false;
      }
    },
    async submitReset() {
      this.busy = true;
      this.message = { type: "info", text: "" };
      try {
        const data = await this.postJson("/api/auth/reset-password", this.resetForm);
        this.message = { type: "success", text: data.message + " 请使用新密码重新登录。" };
        this.loginForm.usernameOrEmail = this.resetForm.username;
        this.activeTab = "login";
      } catch (error) {
        this.message = { type: "error", text: error.message };
      } finally {
        this.busy = false;
      }
    },
    async loadOverview() {
      this.overviewLoading = true;
      this.overviewError = "";
      try {
        this.overview = await this.requestJson("/api/overview", { method: "GET" });
      } catch (error) {
        this.overview = emptyOverview();
        this.overviewError = error.message;
      } finally {
        this.overviewLoading = false;
      }
    },
    async loadDocuments() {
      try {
        this.documents = await this.requestJson("/api/docs", { method: "GET" });
      } catch (error) {
        this.documents = [];
        this.documentMessage = { type: "error", text: error.message };
      }
    },
    async selectDocument(documentId) {
      this.documentMessage = { type: "info", text: "" };
      try {
        const data = await this.requestJson(`/api/docs/${documentId}`, { method: "GET" });
        this.selectedDocumentId = data.id;
        this.documentForm = { title: data.title, content: data.content };
        this.draftNotice = "";
        this.goToPage("docs");
      } catch (error) {
        this.documentMessage = { type: "error", text: error.message };
      }
    },
    async startDraft() {
      this.goToPage("docs");
      this.selectedDocumentId = null;
      this.documentForm = emptyDocumentForm();
      this.draftNotice = "新草稿";
      this.documentMessage = { type: "info", text: "已创建空白草稿，请直接输入标题和内容。" };
      await this.focusTitle();
    },
    async saveDocument() {
      this.documentBusy = true;
      this.documentMessage = { type: "info", text: "" };
      try {
        const url = this.selectedDocumentId ? `/api/docs/${this.selectedDocumentId}` : "/api/docs";
        const method = this.selectedDocumentId ? "PUT" : "POST";
        const data = await this.writeJson(url, method, this.documentForm);
        this.selectedDocumentId = data.id;
        this.documentForm = { title: data.title, content: data.content };
        this.draftNotice = "";
        await this.loadDocuments();
        this.documentMessage = { type: "success", text: "文档已保存。" };
      } catch (error) {
        this.documentMessage = { type: "error", text: error.message };
      } finally {
        this.documentBusy = false;
      }
    },
    async logout() {
      try {
        await this.postJson("/api/auth/logout", {});
      } catch (error) {
        if (!error.isAuthError) {
          this.message = { type: "error", text: error.message };
          return;
        }
      }
      this.clearWorkspaceState();
      this.goToPage("dashboard");
      this.message = { type: "info", text: "你已退出登录。" };
      this.activeTab = "login";
    },
    formatDate(value) {
      return value ? value.replace("T", " ") : "未知";
    },
    async requestJson(url, options = {}) {
      const { expectEnvelope = false, headers = {}, ...fetchOptions } = options;
      const response = await fetch(url, { credentials: "include", ...fetchOptions, headers });
      const contentType = response.headers.get("content-type") || "";
      let data = null;
      let text = "";
      if (contentType.includes("application/json")) data = await response.json();
      else text = await response.text();
      const message = (data && data.message) || text || "请求失败，请稍后再试。";
      if (response.status === 401) throw createAuthError(message);
      if (!response.ok) throw new Error(message);
      if (expectEnvelope && (!data || !data.success)) throw new Error(message);
      return data;
    },
    async postJson(url, payload) {
      return this.writeJson(url, "POST", payload);
    },
    async writeJson(url, method, payload) {
      const response = await fetch(url, {
        method,
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const contentType = response.headers.get("content-type") || "";
      let data = null;
      let text = "";
      if (contentType.includes("application/json")) data = await response.json();
      else text = await response.text();
      if (response.status === 401) throw createAuthError((data && data.message) || text);
      if (!response.ok) throw new Error((data && data.message) || text || "请求失败，请稍后再试。");
      if (data && Object.prototype.hasOwnProperty.call(data, "success") && !data.success) {
        throw new Error(data.message || "请求失败，请稍后再试。");
      }
      return data;
    }
  }
};
</script>

<style>
:root{--bg:#f3ecdf;--paper:rgba(255,252,247,.94);--paper-strong:#fffdfa;--ink:#172028;--muted:#5d6973;--line:rgba(23,32,40,.1);--primary:#0f6b67;--primary-deep:#23465c;--success:#176946;--danger:#b42318;--success-soft:rgba(23,105,70,.12);--danger-soft:rgba(180,35,24,.12)}
.theme-dark{color-scheme:dark;--bg:#10161c;--paper:rgba(22,30,37,.95);--paper-strong:#182129;--ink:#edf3f7;--muted:#98a5af;--line:rgba(237,243,247,.1);--primary:#73ddd2;--primary-deep:#92d7ff;--success-soft:rgba(23,105,70,.18);--danger-soft:rgba(180,35,24,.2)}
*{box-sizing:border-box}body{margin:0;font-family:"Trebuchet MS","Segoe UI","Microsoft YaHei",sans-serif;background:var(--bg)}button,input,textarea{font:inherit}#app{min-height:100vh}
.page{width:min(1280px,calc(100% - 24px));margin:0 auto;padding:24px 0 36px;color:var(--ink)}
.auth,.workspace,.dashboard-grid,.docs-layout,.editor-grid,.form,.doc-list,.doc-summary-list{display:grid;gap:20px}.auth{grid-template-columns:1.05fr .95fr}.dashboard-grid{grid-template-columns:repeat(3,minmax(0,1fr))}.docs-layout{grid-template-columns:340px minmax(0,1fr)}.editor-grid{grid-template-columns:minmax(0,1fr) minmax(320px,420px)}.form{gap:12px}
.panel{border:1px solid var(--line);border-radius:24px;background:var(--paper);padding:22px;box-shadow:0 18px 44px rgba(23,32,40,.08)}
.eyebrow{margin:0 0 10px;color:var(--primary);font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase}
h1,h2,p{margin:0}h1{font-size:clamp(30px,4vw,50px);line-height:1.05}h2{font-size:24px}.muted,.metric span,.empty,.preview-head small{color:var(--muted);line-height:1.7}
.header-row,.actions,.tabs,.chips,.preview-head{display:flex;gap:10px}.header-row{justify-content:space-between;align-items:flex-start}.header-row.compact{align-items:center}.actions,.chips{flex-wrap:wrap}
.tab,.ghost,.primary,.danger,.doc-card{transition:transform .18s ease,box-shadow .18s ease}.tab,.ghost{border:1px solid var(--line);background:rgba(255,255,255,.7);color:var(--ink)}.tab,.ghost,.primary,.danger{min-height:42px;padding:0 16px;border-radius:999px;cursor:pointer;font-weight:700}.tab.active,.primary{border:0;color:#fff;background:linear-gradient(135deg,#172028,var(--primary-deep))}.small{min-height:36px;padding:0 12px;font-size:13px}.danger{border:0;color:#fff;background:linear-gradient(135deg,#8c2d23,#b42318)}.tab:hover,.ghost:hover,.primary:hover,.danger:hover,.doc-card:hover{transform:translateY(-1px)}
input,textarea{width:100%;border:1px solid var(--line);border-radius:16px;padding:12px 14px;background:rgba(255,255,255,.92);color:var(--ink)}textarea{min-height:520px;resize:vertical;line-height:1.7}input:focus,textarea:focus{outline:none;border-color:rgba(15,107,103,.45);box-shadow:0 0 0 4px rgba(15,107,103,.08)}
.message{padding:12px 14px;border-radius:16px;font-size:14px}.message.info{background:rgba(15,107,103,.08)}.message.success{background:var(--success-soft);color:var(--success)}.message.error{background:var(--danger-soft);color:var(--danger)}
.metric{display:grid;gap:8px;padding:14px 0;border-bottom:1px solid var(--line)}.metric:last-of-type{border-bottom:0}.metric strong{font-size:20px;line-height:1.5}
.chip{display:inline-flex;align-items:center;min-height:34px;padding:0 12px;border-radius:999px;border:1px solid var(--line)}.full{width:100%}
.doc-summary{display:grid;gap:6px;padding:14px 16px;border:1px solid var(--line);border-radius:18px;background:rgba(255,255,255,.62)}.doc-summary strong{font-size:15px;line-height:1.5}.empty{padding:20px 16px;border:1px dashed var(--line);border-radius:18px}
.doc-card{width:100%;padding:16px;text-align:left;border:1px solid var(--line);border-radius:20px;background:linear-gradient(135deg,rgba(255,255,255,.92),rgba(247,244,239,.88));cursor:pointer}.doc-card strong{display:block;font-size:16px;line-height:1.5}.doc-card.active{border-color:transparent;color:#fff;background:linear-gradient(135deg,rgba(20,56,78,.98),rgba(15,107,103,.92));box-shadow:0 16px 34px rgba(20,56,78,.22)}
.badge{display:inline-flex;align-items:center;min-height:36px;padding:0 12px;border-radius:999px;background:rgba(15,107,103,.1);color:var(--primary);font-size:13px;font-weight:700}
.preview-head{justify-content:space-between;align-items:center;margin-bottom:12px}.preview{min-height:520px;padding:14px;border:1px solid var(--line);border-radius:16px;background:var(--paper-strong);overflow:auto;line-height:1.8;word-break:break-word}.preview h1,.preview h2,.preview h3,.preview h4,.preview h5,.preview h6{margin:0 0 12px;line-height:1.3}.preview p,.preview ul,.preview ol,.preview blockquote,.preview pre{margin:0 0 14px}.preview ul,.preview ol{padding-left:20px}.preview hr{border:0;border-top:1px solid var(--line);margin:18px 0}.preview blockquote{padding:8px 12px;border-left:4px solid rgba(15,107,103,.35);background:rgba(15,107,103,.06);border-radius:12px}.preview pre,.preview code{font-family:"Consolas","Courier New",monospace}.preview pre{padding:12px;border-radius:12px;background:rgba(20,56,78,.08)}.preview code{padding:2px 6px;border-radius:8px;background:rgba(20,56,78,.08)}.preview pre code{padding:0;background:transparent}.preview a{color:var(--primary)}
@media (max-width:1080px){.dashboard-grid,.docs-layout,.editor-grid{grid-template-columns:1fr}}@media (max-width:920px){.auth{grid-template-columns:1fr}}@media (max-width:720px){.header-row{flex-direction:column;align-items:flex-start}.tabs{flex-wrap:wrap}.actions{width:100%}.ghost,.danger{width:100%}}
</style>
