<template>
  <div class="app-shell">
    <section v-if="!authenticated" class="auth-shell">
      <div class="panel auth-panel">
        <p class="eyebrow">TESTY</p>
        <h1 class="hero-title">{{ authTitle }}</h1>
        <p class="auth-subtitle">统一认证、文档协作与后台管理工作台。</p>
        <div class="tab-row">
          <button type="button" :class="tabClass('login')" @click="authMode = 'login'">登录</button>
          <button type="button" :class="tabClass('register')" @click="authMode = 'register'">注册</button>
          <button type="button" :class="tabClass('reset')" @click="authMode = 'reset'">重置密码</button>
        </div>
        <div v-if="message.text" class="notice" :class="message.type">{{ message.text }}</div>
        <form class="stack" @submit.prevent="submitAuth">
          <template v-if="authMode === 'login'">
            <label class="field">
              <span>用户名或邮箱</span>
              <input v-model.trim="loginForm.usernameOrEmail" type="text" autocomplete="username" />
            </label>
            <label class="field">
              <span>密码</span>
              <input v-model="loginForm.password" type="password" autocomplete="current-password" />
            </label>
          </template>
          <template v-else-if="authMode === 'register'">
            <label class="field">
              <span>用户名</span>
              <input v-model.trim="registerForm.username" type="text" autocomplete="username" />
            </label>
            <label class="field">
              <span>邮箱</span>
              <input v-model.trim="registerForm.email" type="email" autocomplete="email" />
            </label>
            <label class="field">
              <span>密码</span>
              <input v-model="registerForm.password" type="password" autocomplete="new-password" />
            </label>
            <label class="field">
              <span>显示名称</span>
              <input v-model.trim="registerForm.displayName" type="text" />
            </label>
            <label class="field">
              <span>手机号</span>
              <input v-model.trim="registerForm.phoneNumber" type="text" />
            </label>
          </template>
          <template v-else>
            <label class="field">
              <span>用户名</span>
              <input v-model.trim="resetForm.username" type="text" autocomplete="username" />
            </label>
            <label class="field">
              <span>邮箱</span>
              <input v-model.trim="resetForm.email" type="email" autocomplete="email" />
            </label>
            <label class="field">
              <span>新密码</span>
              <input v-model="resetForm.newPassword" type="password" autocomplete="new-password" />
            </label>
          </template>
          <button class="primary-button" type="submit" :disabled="authBusy">{{ authBusy ? '处理中...' : authButton }}</button>
        </form>
      </div>
    </section>

    <section v-else class="workspace-shell">
      <header class="panel topbar">
        <div>
          <p class="eyebrow">TESTY WORKSPACE</p>
          <h1 class="hero-title compact">你好，{{ user.displayName || user.username }}</h1>
          <p class="muted">角色：{{ (user.roles || []).join(' / ') || '--' }}<span v-if="user.lastLoginAt"> · 最近登录：{{ formatDateTime(user.lastLoginAt) }}</span></p>
        </div>
        <div class="button-row">
          <button :class="navClass('dashboard')" type="button" @click="setPage('dashboard')">概览</button>
          <button v-if="canReadDocs" :class="navClass('docs')" type="button" @click="setPage('docs')">文档</button>
          <button v-if="canAccessAdmin" :class="navClass('admin')" type="button" @click="setPage('admin')">后台管理</button>
          <button class="ghost danger" type="button" @click="logout">退出登录</button>
        </div>
      </header>

      <div v-if="message.text" class="notice inline" :class="message.type">{{ message.text }}</div>

      <main class="main-grid">
        <section v-if="currentPage === 'dashboard'" class="dashboard-layout">
          <article class="panel page dashboard-hero">
            <div class="section-head">
              <div>
                <p class="eyebrow">项目概览</p>
                <h2>{{ overview.applicationName || 'TestY' }}</h2>
              </div>
              <button class="ghost" type="button" @click="loadOverview">刷新概览</button>
            </div>
            <p class="hero-copy">{{ overview.message || '欢迎进入统一认证、文档协作与后台管理工作台。' }}</p>
            <div class="stats compact-stats">
              <div class="stat"><span>Java 版本</span><strong>{{ overview.javaVersion || '--' }}</strong></div>
              <div class="stat"><span>文档数量</span><strong>{{ documents.length }}</strong></div>
              <div class="stat"><span>后台权限</span><strong>{{ adminPermissionCount }}</strong></div>
            </div>
          </article>

          <article class="panel page dashboard-side">
            <div class="section-head">
              <div>
                <p class="eyebrow">最近文档</p>
                <h3>文档动态</h3>
              </div>
              <button v-if="canReadDocs" class="ghost" type="button" @click="setPage('docs')">进入文档页</button>
            </div>
            <ul class="plain-list">
              <li v-for="doc in recentDocuments" :key="doc.id">{{ doc.title || '未命名文档' }}</li>
              <li v-if="!recentDocuments.length" class="muted">当前还没有文档。</li>
            </ul>
          </article>

          <article class="panel page dashboard-modules">
            <div class="section-head">
              <div>
                <p class="eyebrow">项目模块</p>
                <h3>后端与前端组成</h3>
              </div>
            </div>
            <div class="module-grid">
              <div v-for="module in overview.modules || []" :key="module" class="module-chip">{{ module }}</div>
              <p v-if="!(overview.modules || []).length" class="muted">暂无模块信息。</p>
            </div>
          </article>
        </section>

        <section v-if="currentPage === 'docs' && canReadDocs" class="docs-layout">
          <aside class="panel page sidebar">
            <div class="section-head">
              <h2>文档列表</h2>
              <button class="ghost" type="button" @click="loadDocuments">刷新</button>
            </div>
            <button class="primary-button" type="button" @click="startNewDocument">新建文档</button>
            <div class="doc-list">
              <button v-for="doc in documents" :key="doc.id" type="button" class="doc-item" :class="{ active: currentDocumentId === doc.id }" @click="openDocument(doc.id)">{{ doc.title || '未命名文档' }}</button>
              <p v-if="!documents.length" class="muted">暂无文档。</p>
            </div>
          </aside>
          <div class="docs-main">
            <section class="panel page docs-editor-surface">
              <div class="section-head">
                <div>
                  <p class="eyebrow">Markdown 编辑器</p>
                  <h2>{{ currentDocumentId ? '编辑文档' : '新建草稿' }}</h2>
                </div>
                <div class="button-row">
                  <span class="muted">{{ saveHint }}</span>
                  <button class="ghost" type="button" @click="saveDocument('MANUAL')">保存</button>
                  <button class="ghost danger" type="button" @click="deleteDocument">删除</button>
                </div>
              </div>
              <label class="field">
                <span>标题</span>
                <input ref="titleInput" v-model="documentForm.title" type="text" />
              </label>

              <div class="docs-workbench">
                <div class="editor-pane">
                  <label class="field editor-textarea">
                    <span>Markdown 内容</span>
                    <textarea
                      v-model="documentForm.content"
                      rows="20"
                      placeholder="开始编写&#10;在这里输入 Markdown 内容。"
                    ></textarea>
                  </label>
                  <p class="muted">字数：{{ documentWordCount }}</p>
                </div>

                <section class="preview-pane">
                  <div class="pane-label">实时预览</div>
                  <div class="markdown-body" v-html="renderedMarkdown"></div>
                </section>
              </div>
            </section>

            <section class="panel page versions-pane">
              <div class="section-head">
                <div>
                  <p class="eyebrow">版本历史</p>
                  <h2>恢复历史版本</h2>
                </div>
                <button class="ghost" type="button" :disabled="!currentDocumentId" @click="loadVersions">刷新</button>
              </div>
              <div class="version-list">
                <article v-for="version in documentVersions" :key="version.id" class="card version-card">
                  <div>
                    <strong>#{{ version.versionNumber }}</strong>
                    <p>{{ version.title || '未命名文档' }}</p>
                    <p class="muted">{{ version.sourceType }} · {{ formatDateTime(version.createdAt) }}</p>
                  </div>
                  <button class="ghost" type="button" @click="restoreVersion(version.id)">恢复</button>
                </article>
                <p v-if="!documentVersions.length" class="muted">暂无历史版本。</p>
              </div>
            </section>
          </div>
        </section>

        <section v-if="currentPage === 'admin' && canAccessAdmin" class="panel page">
          <div class="section-head">
            <div>
              <p class="eyebrow">管理员后台</p>
              <h2>后台管理</h2>
            </div>
            <button class="ghost" type="button" @click="loadAdminData">刷新数据</button>
          </div>

          <div class="tab-row">
            <button v-for="tab in adminTabs" :key="tab.key" :class="tabClass(tab.key, adminSection)" type="button" @click="changeAdminSection(tab.key)">{{ tab.label }}</button>
          </div>

          <div v-if="adminSection === 'overview'" class="stack">
            <div v-if="adminErrors.overview" class="notice error">{{ adminErrors.overview }}</div>
            <div class="stats">
              <div class="stat"><span>Users</span><strong>{{ canReadAdminUsers ? adminUsers.length : '--' }}</strong></div>
              <div class="stat"><span>Roles</span><strong>{{ canReadAdminRoles ? adminRoles.length : '--' }}</strong></div>
              <div class="stat"><span>Permissions</span><strong>{{ canReadAdminPermissions ? adminPermissions.length : '--' }}</strong></div>
              <div class="stat"><span>Failed 24h</span><strong>{{ canReadAdminLogins ? auditAlerts.failedAttemptsLast24Hours : '--' }}</strong></div>
            </div>
          </div>

          <div v-else-if="adminSection === 'users'" class="stack">
            <div class="section-head">
              <h3>用户管理</h3>
              <button class="ghost" type="button" @click="changeAdminSection('overview')">退出用户管理</button>
            </div>
            <div v-if="adminErrors.users" class="notice error">{{ adminErrors.users }}</div>
            <div class="filters">
              <input v-model.trim="userFilters.keyword" type="text" placeholder="搜索用户、邮箱或角色" />
              <select v-model="userFilters.status">
                <option value="">全部状态</option>
                <option v-for="status in accountStatusOptions" :key="status" :value="status">{{ status }}</option>
              </select>
            </div>
            <div class="stack">
              <article v-for="adminUser in filteredAdminUsers" :key="adminUser.id" class="card">
                <div class="section-head">
                  <div>
                    <h3>{{ adminUser.displayName || adminUser.username }}</h3>
                    <p class="muted">{{ adminUser.username }} · {{ adminUser.email || '--' }}</p>
                  </div>
                  <strong>{{ adminUser.accountStatus || 'UNKNOWN' }}</strong>
                </div>
                <div class="two-col">
                  <label class="field">
                    <span>角色分配</span>
                    <select v-model="userRoleSelections[adminUser.id]" multiple size="4">
                      <option v-for="role in adminRoles" :key="role.id" :value="role.id">{{ role.name }} ({{ role.code }})</option>
                    </select>
                  </label>
                  <label class="field">
                    <span>账户状态</span>
                    <select v-model="userStatusSelections[adminUser.id]">
                      <option v-for="status in accountStatusOptions" :key="status" :value="status">{{ status }}</option>
                    </select>
                  </label>
                </div>
                <div class="button-row">
                  <button class="ghost" type="button" @click="saveUserRoles(adminUser)">保存角色</button>
                  <button class="ghost" type="button" @click="saveUserStatus(adminUser)">保存状态</button>
                </div>
              </article>
            </div>
          </div>
          <div v-else-if="adminSection === 'roles'" class="two-col">
            <section class="stack">
              <div v-if="adminErrors.roles" class="notice error">{{ adminErrors.roles }}</div>
              <article v-for="role in adminRoles" :key="role.id" class="card">
                <div class="section-head">
                  <div>
                    <h3>{{ role.name }}</h3>
                    <p class="muted">{{ role.code }}</p>
                  </div>
                  <button class="ghost" type="button" :disabled="role.code === 'ROOT'" @click="editRole(role)">编辑</button>
                </div>
                <p class="muted">{{ role.description || '暂无描述' }}</p>
                <p class="muted">{{ (role.permissions || []).join(', ') || '暂无权限' }}</p>
              </article>
            </section>
            <section class="card page">
              <div class="section-head">
                <h3>{{ roleForm.id ? '编辑角色' : '新建角色' }}</h3>
                <button class="ghost" type="button" @click="resetRoleForm">清空表单</button>
              </div>
              <form class="stack" @submit.prevent="submitRoleForm">
                <label class="field"><span>角色编码</span><input v-model.trim="roleForm.code" type="text" :disabled="Boolean(roleForm.id)" /></label>
                <label class="field"><span>角色名称</span><input v-model.trim="roleForm.name" type="text" /></label>
                <label class="field"><span>角色描述</span><textarea v-model.trim="roleForm.description" rows="4"></textarea></label>
                <label class="field"><span>权限筛选</span><input v-model.trim="permissionKeyword" type="text" placeholder="按编码或名称筛选权限" /></label>
                <div class="permission-grid">
                  <label v-for="permission in filteredPermissions" :key="permission.id" class="permission-item">
                    <input v-model="roleForm.permissionIds" type="checkbox" :value="permission.id" />
                    <span>{{ permission.code }}</span>
                  </label>
                </div>
                <button class="primary-button" type="submit">{{ roleForm.id ? '保存角色' : '创建角色' }}</button>
              </form>
            </section>
          </div>

          <div v-else-if="adminSection === 'audits'" class="stack">
            <div v-if="adminErrors.audits" class="notice error">{{ adminErrors.audits }}</div>
            <div class="filters filters-wide">
              <input v-model.trim="auditFilters.principal" type="text" placeholder="登录主体" />
              <select v-model="auditFilters.success">
                <option value="">全部结果</option>
                <option value="true">成功</option>
                <option value="false">失败</option>
              </select>
              <input v-model.trim="auditFilters.remoteIp" type="text" placeholder="IP 地址" />
              <select v-model.number="auditFilters.size">
                <option :value="10">10</option>
                <option :value="20">20</option>
                <option :value="50">50</option>
              </select>
            </div>
            <div class="button-row">
              <button class="ghost" type="button" @click="loadAudits(0)">应用筛选</button>
              <button class="ghost" type="button" @click="resetAuditFilters">重置筛选</button>
            </div>
            <article v-for="audit in auditPage.content" :key="audit.id" class="card">
              <div class="section-head">
                <div>
                  <h3>{{ audit.principal || '未知主体' }}</h3>
                  <p class="muted">{{ formatDateTime(audit.loggedInAt) }} · {{ audit.remoteIp || '--' }}</p>
                </div>
                <strong>{{ audit.success ? '登录成功' : '登录失败' }}</strong>
              </div>
              <p class="muted">{{ audit.message || '无附加信息' }}</p>
            </article>
            <div class="button-row">
              <button class="ghost" type="button" :disabled="auditPage.number <= 0" @click="loadAudits(auditPage.number - 1)">上一页</button>
              <span class="muted">第 {{ (auditPage.number || 0) + 1 }} / {{ auditPage.totalPages || 1 }} 页</span>
              <button class="ghost" type="button" :disabled="(auditPage.number || 0) + 1 >= (auditPage.totalPages || 1)" @click="loadAudits((auditPage.number || 0) + 1)">下一页</button>
            </div>
          </div>
        </section>
      </main>
    </section>
  </div>
</template>

<script>
import MarkdownIt from "markdown-it";
import markdownItTaskLists from "markdown-it-task-lists";
import hljs from "highlight.js";

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(code, language) {
    if (language && hljs.getLanguage(language)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language, ignoreIllegals: true }).value}</code></pre>`;
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(code)}</code></pre>`;
  }
}).use(markdownItTaskLists);

const emptyUser = () => ({ username: "", displayName: "", roles: [], permissions: [], rootAdmin: false, lastLoginAt: null });
const emptyDoc = () => ({ title: "", content: "", createdAt: null, updatedAt: null });
const emptyRole = () => ({ id: null, code: "", name: "", description: "", permissionIds: [] });
const emptyAuditPage = () => ({ content: [], number: 0, totalPages: 1, totalElements: 0 });

export default {
  name: "AppRoot",
  data() {
    return {
      authMode: "login",
      authBusy: false,
      authenticated: false,
      user: emptyUser(),
      message: { type: "info", text: "" },
      loginForm: { usernameOrEmail: "", password: "" },
      registerForm: { username: "", email: "", password: "", displayName: "", phoneNumber: "" },
      resetForm: { username: "", email: "", newPassword: "" },
      currentPage: "dashboard",
      overview: { applicationName: "", javaVersion: "", message: "", modules: [] },
      documents: [],
      currentDocumentId: null,
      documentForm: emptyDoc(),
      documentVersions: [],
      saveHint: "已开启自动保存",
      autosaveTimer: null,
      suppressAutosave: false,
      adminSection: "overview",
      adminUsers: [],
      adminRoles: [],
      adminPermissions: [],
      adminErrors: { overview: "", users: "", roles: "", audits: "" },
      auditAlerts: { failedAttemptsLast24Hours: 0, suspiciousPrincipals: [] },
      auditPage: emptyAuditPage(),
      userFilters: { keyword: "", status: "" },
      userRoleSelections: {},
      userStatusSelections: {},
      roleForm: emptyRole(),
      permissionKeyword: "",
      auditFilters: { page: 0, size: 10, principal: "", success: "", remoteIp: "" },
      accountStatusOptions: ["ACTIVE", "LOCKED", "DISABLED"]
    };
  },
  computed: {
    authTitle() {
      if (this.authMode === "register") return "创建账号";
      if (this.authMode === "reset") return "重置密码";
      return "登录系统";
    },
    authButton() {
      if (this.authMode === "register") return "提交注册";
      if (this.authMode === "reset") return "重置密码";
      return "登录";
    },
    canReadDocs() { return this.hasPermission("docs:read"); },
    canWriteDocs() { return this.hasPermission("docs:write"); },
    canReadAdminUsers() { return this.hasPermission("admin:users:read"); },
    canReadAdminRoles() { return this.hasPermission("admin:roles:read"); },
    canReadAdminPermissions() { return this.hasPermission("admin:permissions:read"); },
    canReadAdminLogins() { return this.hasPermission("admin:logins:read"); },
    canAccessAdmin() { return this.adminTabs.length > 0; },
    adminTabs() {
      const tabs = [{ key: "overview", label: "安全态势" }];
      if (this.canReadAdminUsers) tabs.push({ key: "users", label: "用户管理" });
      if (this.canReadAdminRoles || this.canReadAdminPermissions) tabs.push({ key: "roles", label: "角色权限" });
      if (this.canReadAdminLogins) tabs.push({ key: "audits", label: "登录审计" });
      return tabs;
    },
    recentDocuments() { return this.documents.slice(0, 5); },
    renderedMarkdown() { return md.render(this.documentForm.content || ""); },
    documentWordCount() { return (this.documentForm.content || "").replace(/\s+/g, "").length; },
    adminPermissionCount() { return (this.user.permissions || []).filter((item) => item.startsWith("admin:")).length; },
    filteredAdminUsers() {
      const keyword = (this.userFilters.keyword || "").toLowerCase();
      const status = this.userFilters.status;
      return this.adminUsers.filter((item) => {
        const hit = !keyword || [item.username, item.email, item.displayName, ...(item.roles || [])].some((value) => String(value || "").toLowerCase().includes(keyword));
        return hit && (!status || item.accountStatus === status);
      });
    },
    filteredPermissions() {
      const keyword = (this.permissionKeyword || "").toLowerCase();
      return this.adminPermissions.filter((item) => !keyword || [item.code, item.name, item.description].some((value) => String(value || "").toLowerCase().includes(keyword)));
    }
  },
  watch: {
    "documentForm.title"() { this.queueAutosave(); },
    "documentForm.content"() { this.queueAutosave(); }
  },
  async mounted() {
    window.addEventListener("hashchange", this.handleHashChange);
    await this.bootstrap();
  },
  beforeUnmount() {
    window.removeEventListener("hashchange", this.handleHashChange);
    if (this.autosaveTimer) clearTimeout(this.autosaveTimer);
  },
  methods: {
    tabClass(key, current = this.authMode) { return ["ghost", current === key ? "active" : ""]; },
    navClass(key) { return ["ghost", this.currentPage === key ? "active" : ""]; },
    hasPermission(code) { return Boolean(this.user.rootAdmin) || (this.user.permissions || []).includes(code); },
    setMessage(text, type = "info") { this.message = { text, type }; },
    clearMessage() { this.message = { text: "", type: "info" }; },
    isAuthRequiredError(error) {
      return Boolean(error && /Authentication required/i.test(error.message || ""));
    },
    resetClientSession(message = "", type = "info") {
      this.authenticated = false;
      this.user = emptyUser();
      this.documents = [];
      this.currentDocumentId = null;
      this.documentForm = emptyDoc();
      this.documentVersions = [];
      this.adminUsers = [];
      this.adminRoles = [];
      this.adminPermissions = [];
      this.auditPage = emptyAuditPage();
      this.currentPage = "dashboard";
      this.adminSection = "overview";
      this.saveHint = "已开启自动保存";
      window.history.replaceState({}, document.title, `${window.location.pathname}${window.location.search}`);
      if (message) this.setMessage(message, type);
    },
    async bootstrap() {
      try {
        const me = await this.apiRequest("/api/auth/me");
        this.applyAuth(me);
        this.authenticated = true;
        this.syncFromHash();
        await this.loadCurrentPage();
      } catch (error) {
        this.authenticated = false;
      }
    },
    applyAuth(data) {
      this.user = {
        username: data.username || "",
        displayName: data.displayName || data.username || "",
        roles: data.roles || [],
        permissions: data.permissions || [],
        rootAdmin: Boolean(data.rootAdmin),
        lastLoginAt: data.lastLoginAt || null
      };
    },
    async submitAuth() {
      this.authBusy = true;
      this.clearMessage();
      try {
        if (this.authMode === "login") {
          const result = await this.apiRequest("/api/auth/login", { method: "POST", body: this.loginForm });
          this.applyAuth(result);
          this.authenticated = true;
          this.syncFromHash();
          await this.loadCurrentPage();
          this.setMessage("登录成功。", "success");
        } else if (this.authMode === "register") {
          await this.apiRequest("/api/auth/register", { method: "POST", body: this.registerForm });
          this.authMode = "login";
          this.setMessage("注册成功，请直接登录。", "success");
        } else {
          await this.apiRequest("/api/auth/reset-password", { method: "POST", body: this.resetForm });
          this.authMode = "login";
          this.setMessage("密码重置成功，请使用新密码登录。", "success");
        }
      } catch (error) {
        this.setMessage(error.message || "请求失败，请稍后再试。", "error");
      } finally {
        this.authBusy = false;
      }
    },
    async logout() {
      try { await this.apiRequest("/api/auth/logout", { method: "POST" }); } catch (error) { void error; }
      this.resetClientSession("已退出登录。", "success");
    },
    handleHashChange() {
      if (!this.authenticated) return;
      this.syncFromHash();
      this.loadCurrentPage();
    },
    syncFromHash() {
      const hash = (window.location.hash || "").replace(/^#/, "");
      const [page, section] = hash.split("/");
      if (page === "docs" && this.canReadDocs) {
        this.currentPage = "docs";
      } else if (page === "admin" && this.canAccessAdmin) {
        this.currentPage = "admin";
        this.adminSection = this.adminTabs.some((item) => item.key === section) ? section : "overview";
      } else {
        this.currentPage = "dashboard";
        this.adminSection = "overview";
      }
    },
    updateHash() {
      if (this.currentPage === "dashboard") window.location.hash = "";
      if (this.currentPage === "docs") window.location.hash = "#docs";
      if (this.currentPage === "admin") window.location.hash = `#admin/${this.adminSection}`;
    },
    async setPage(page) {
      this.currentPage = page;
      if (page !== "admin") this.adminSection = "overview";
      this.updateHash();
      await this.loadCurrentPage();
    },
    async changeAdminSection(section) {
      this.adminSection = section;
      this.updateHash();
      await this.loadAdminData();
    },
    async loadCurrentPage() {
      if (!this.authenticated) return;
      await this.loadOverview();
      if (this.canReadDocs) await this.loadDocuments();
      if (!this.authenticated) return;
      if (this.currentPage === "docs" && !this.currentDocumentId && !this.documentForm.title && !this.documentForm.content) this.startNewDocument(false);
      if (this.currentPage === "admin" && this.canAccessAdmin) await this.loadAdminData();
    },
    async loadOverview() {
      try {
        this.overview = await this.apiRequest("/api/overview");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) void error;
      }
    },
    async loadDocuments() {
      if (!this.canReadDocs || !this.authenticated) return;
      try {
        this.documents = await this.apiRequest("/api/docs");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "文档列表加载失败。", "error");
      }
    },
    patchDoc(doc) {
      this.suppressAutosave = true;
      this.documentForm = { title: doc.title || "", content: doc.content || "", createdAt: doc.createdAt || null, updatedAt: doc.updatedAt || null };
      this.$nextTick(() => { this.suppressAutosave = false; });
    },
    startNewDocument(showMessage = true) {
      this.currentDocumentId = null;
      this.documentVersions = [];
      this.patchDoc(emptyDoc());
      this.saveHint = "已开启自动保存";
      if (showMessage) this.setMessage("已创建空白草稿。", "success");
      this.$nextTick(() => this.$refs.titleInput && this.$refs.titleInput.focus());
    },
    async openDocument(id) {
      try {
        const doc = await this.apiRequest(`/api/docs/${id}`);
        this.currentDocumentId = id;
        this.patchDoc(doc);
        await this.loadVersions();
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "文档加载失败。", "error");
      }
    },
    queueAutosave() {
      if (!this.authenticated || !this.canWriteDocs || this.currentPage !== "docs" || this.suppressAutosave) return;
      if (this.autosaveTimer) clearTimeout(this.autosaveTimer);
      this.saveHint = "等待自动保存...";
      this.autosaveTimer = setTimeout(() => this.saveDocument("AUTOSAVE"), 1200);
    },
    async saveDocument(mode) {
      if (!this.canWriteDocs) return;
      const hasContent = (this.documentForm.title || "").trim() || (this.documentForm.content || "").trim();
      if (!hasContent) { this.saveHint = "草稿为空"; return; }
      if (this.autosaveTimer) clearTimeout(this.autosaveTimer);
      try {
        const path = this.currentDocumentId ? `/api/docs/${this.currentDocumentId}` : "/api/docs";
        const method = this.currentDocumentId ? "PUT" : "POST";
        const saved = await this.apiRequest(`${path}?mode=${mode}`, { method, body: { title: this.documentForm.title, content: this.documentForm.content } });
        this.currentDocumentId = saved.id;
        this.patchDoc(saved);
        await this.loadDocuments();
        await this.loadVersions();
        this.saveHint = mode === "AUTOSAVE" ? "已自动保存" : "已保存";
        if (mode !== "AUTOSAVE") this.setMessage("文档已保存。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "文档保存失败。", "error");
      }
    },
    async deleteDocument() {
      if (!this.currentDocumentId) { this.startNewDocument(false); return; }
      if (!window.confirm("确定删除当前文档吗？")) return;
      try {
        await this.apiRequest(`/api/docs/${this.currentDocumentId}`, { method: "DELETE" });
        this.currentDocumentId = null;
        this.patchDoc(emptyDoc());
        this.documentVersions = [];
        await this.loadDocuments();
        this.setMessage("文档已删除。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "删除文档失败。", "error");
      }
    },
    async loadVersions() {
      if (!this.currentDocumentId) { this.documentVersions = []; return; }
      try {
        this.documentVersions = await this.apiRequest(`/api/docs/${this.currentDocumentId}/versions`);
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "版本列表加载失败。", "error");
      }
    },
    async restoreVersion(versionId) {
      try {
        const restored = await this.apiRequest(`/api/docs/${this.currentDocumentId}/versions/${versionId}/restore`, { method: "POST" });
        this.patchDoc(restored);
        await this.loadDocuments();
        await this.loadVersions();
        this.setMessage("版本已恢复。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "恢复版本失败。", "error");
      }
    },
    async loadAdminData() {
      this.adminErrors = { overview: "", users: "", roles: "", audits: "" };
      if (this.canReadAdminRoles) await this.loadRoles();
      if (this.canReadAdminPermissions) await this.loadPermissions();
      if (this.canReadAdminUsers) await this.loadUsers();
      if (this.canReadAdminLogins) {
        await this.loadAuditAlerts();
        if (this.adminSection === "audits") await this.loadAudits(this.auditFilters.page || 0);
      }
    },
    async loadUsers() {
      try { this.adminUsers = await this.apiRequest("/api/admin/users"); this.hydrateUserSelections(); }
      catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.users = error.message || "用户列表加载失败。";
        this.adminErrors.overview = this.adminErrors.users;
      }
    },
    async loadRoles() {
      try { this.adminRoles = await this.apiRequest("/api/admin/roles"); this.hydrateUserSelections(); }
      catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.roles = error.message || "角色列表加载失败。";
      }
    },
    async loadPermissions() {
      try { this.adminPermissions = await this.apiRequest("/api/admin/permissions"); }
      catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.roles = error.message || "权限列表加载失败。";
      }
    },
    async loadAuditAlerts() {
      try { this.auditAlerts = await this.apiRequest("/api/admin/login-audits/alerts"); }
      catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.overview = error.message || "审计告警加载失败。";
      }
    },
    async loadAudits(page = 0) {
      try {
        this.auditFilters.page = Math.max(page, 0);
        const params = new URLSearchParams({ page: String(this.auditFilters.page), size: String(this.auditFilters.size) });
        if (this.auditFilters.principal) params.set("principal", this.auditFilters.principal);
        if (this.auditFilters.success !== "") params.set("success", this.auditFilters.success);
        if (this.auditFilters.remoteIp) params.set("remoteIp", this.auditFilters.remoteIp);
        this.auditPage = await this.apiRequest(`/api/admin/login-audits?${params.toString()}`);
      } catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.audits = error.message || "审计列表加载失败。";
      }
    },
    resetAuditFilters() {
      this.auditFilters = { page: 0, size: 10, principal: "", success: "", remoteIp: "" };
      this.loadAudits(0);
    },
    hydrateUserSelections() {
      const codeToId = {};
      this.adminRoles.forEach((role) => { codeToId[role.code] = role.id; });
      const roleSelections = {};
      const statusSelections = {};
      this.adminUsers.forEach((item) => {
        roleSelections[item.id] = (item.roles || []).map((code) => codeToId[code]).filter((id) => id !== undefined);
        statusSelections[item.id] = item.accountStatus || "ACTIVE";
      });
      this.userRoleSelections = roleSelections;
      this.userStatusSelections = statusSelections;
    },
    async saveUserRoles(user) {
      const roleIds = this.userRoleSelections[user.id] || [];
      if (!roleIds.length) { this.setMessage("至少需要保留一个角色。", "error"); return; }
      try {
        const updated = await this.apiRequest(`/api/admin/users/${user.id}/roles`, { method: "PUT", body: { roleIds } });
        this.replaceUser(updated);
        this.hydrateUserSelections();
        this.setMessage("角色已更新。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "角色更新失败。", "error");
      }
    },
    async saveUserStatus(user) {
      try {
        const updated = await this.apiRequest(`/api/admin/users/${user.id}/status`, { method: "PUT", body: { accountStatus: this.userStatusSelections[user.id] } });
        this.replaceUser(updated);
        this.hydrateUserSelections();
        this.setMessage("账户状态已更新。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "状态更新失败。", "error");
      }
    },
    replaceUser(updated) { this.adminUsers = this.adminUsers.map((item) => (item.id === updated.id ? updated : item)); },
    editRole(role) {
      const codeToPermissionId = {};
      this.adminPermissions.forEach((permission) => { codeToPermissionId[permission.code] = permission.id; });
      this.roleForm = { id: role.id, code: role.code, name: role.name, description: role.description || "", permissionIds: (role.permissions || []).map((code) => codeToPermissionId[code]).filter((id) => id !== undefined) };
    },
    resetRoleForm() { this.roleForm = emptyRole(); this.permissionKeyword = ""; },
    async submitRoleForm() {
      if (!this.roleForm.permissionIds.length) { this.setMessage("请至少选择一个权限。", "error"); return; }
      try {
        const endpoint = this.roleForm.id ? `/api/admin/roles/${this.roleForm.id}` : "/api/admin/roles";
        const method = this.roleForm.id ? "PUT" : "POST";
        await this.apiRequest(endpoint, { method, body: this.roleForm });
        await this.loadRoles();
        this.resetRoleForm();
        this.setMessage("角色信息已保存。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "角色保存失败。", "error");
      }
    },
    formatDateTime(value) {
      if (!value) return "--";
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return value;
      const datePart = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
      const timePart = `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
      return `${datePart} ${timePart}`;
    },
    async apiRequest(url, options = {}) {
      const config = { method: options.method || "GET", credentials: "include", headers: { Accept: "application/json" } };
      if (options.body !== undefined) {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(options.body);
      }
      const response = await fetch(url, config);
      const raw = await response.text();
      let data = null;
      if (raw) {
        try { data = JSON.parse(raw); } catch (error) { data = raw; }
      }
      if (!response.ok) {
        const message = data && data.message ? data.message : "请求失败，请稍后再试。";
        if (response.status === 401 && this.authenticated) {
          this.resetClientSession("登录状态已失效，请重新登录。", "error");
        }
        throw new Error(message);
      }
      return data;
    }
  }
};
</script>

<style>
:root {
  --bg: linear-gradient(135deg, #f3b38f 0%, #f6efe5 50%, #dde7e3 100%);
  --panel: rgba(255, 251, 246, 0.9);
  --line: rgba(17, 24, 39, 0.08);
  --text: #1f2933;
  --muted: #6b7280;
  --accent: #0b7a75;
  --accent-soft: rgba(11, 122, 117, 0.12);
  --danger: #cb4b37;
  --danger-soft: rgba(203, 75, 55, 0.12);
}
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--text); font-family: "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif; }
button, input, select, textarea { font: inherit; }
button { cursor: pointer; }
.app-shell { min-height: 100vh; padding: 24px; }
.auth-shell { min-height: calc(100vh - 48px); display: grid; place-items: center; }
.workspace-shell, .auth-shell { max-width: 1800px; margin: 0 auto; }
.panel, .card { background: var(--panel); border: 1px solid var(--line); border-radius: 24px; box-shadow: 0 24px 60px rgba(54, 40, 24, 0.12); }
.page, .auth-panel, .topbar { padding: 24px; }
.auth-panel { width: min(560px, 100%); }
.eyebrow { margin: 0 0 10px; color: var(--accent); font-size: 13px; letter-spacing: 0.14em; }
.hero-title { margin: 0; font-size: clamp(38px, 7vw, 68px); line-height: 0.95; font-family: Georgia, "Times New Roman", serif; }
.hero-title.compact { font-size: clamp(34px, 5vw, 56px); }
.muted { color: var(--muted); }
.main-grid { display: grid; gap: 20px; margin-top: 20px; }
.dashboard-layout { display: grid; gap: 20px; grid-template-columns: 1.45fr 0.95fr; }
.stack { display: grid; gap: 16px; }
.two-col { display: grid; gap: 16px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.button-row, .tab-row, .section-head { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; }
.section-head { justify-content: space-between; }
.ghost, .primary-button { border: 1px solid var(--line); border-radius: 999px; padding: 11px 16px; background: rgba(255, 255, 255, 0.72); color: var(--text); }
.ghost.active, .primary-button { background: var(--accent-soft); border-color: rgba(11, 122, 117, 0.28); }
.ghost.danger { color: var(--danger); background: var(--danger-soft); border-color: rgba(203, 75, 55, 0.22); }
.auth-subtitle { margin: 12px 0 0; color: var(--muted); line-height: 1.7; }
.field { display: grid; gap: 8px; }
.field span { color: var(--muted); font-size: 14px; }
.field input, .field select, .field textarea, .filters input, .filters select { width: 100%; border: 1px solid rgba(17, 24, 39, 0.12); border-radius: 16px; padding: 13px 14px; background: rgba(255, 255, 255, 0.95); color: var(--text); }
.notice { padding: 14px 16px; border-radius: 16px; border: 1px solid transparent; }
.notice.inline { margin-top: 16px; }
.notice.info { background: rgba(75, 118, 183, 0.12); color: #31507e; }
.notice.success { background: rgba(11, 122, 117, 0.12); color: #0b6a64; }
.notice.error { background: rgba(203, 75, 55, 0.14); color: #b04031; }
.topbar { display: flex; justify-content: space-between; gap: 20px; align-items: center; }
.stats { display: grid; gap: 14px; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.stat { border: 1px solid var(--line); border-radius: 18px; padding: 16px; background: rgba(255, 255, 255, 0.62); display: grid; gap: 8px; }
.stat span { color: var(--muted); font-size: 13px; }
.compact-stats { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.dashboard-hero { grid-column: 1; }
.dashboard-side { grid-column: 2; }
.dashboard-modules { grid-column: 1 / span 2; }
.hero-copy { margin: 0 0 18px; max-width: 760px; color: var(--muted); line-height: 1.8; }
.module-grid { display: flex; gap: 12px; flex-wrap: wrap; }
.module-chip { padding: 12px 14px; border-radius: 16px; background: rgba(255, 255, 255, 0.72); border: 1px solid var(--line); }
.plain-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.docs-layout { display: grid; gap: 20px; grid-template-columns: 300px minmax(0, 1fr); }
.sidebar { display: grid; gap: 16px; align-content: start; }
.doc-list, .version-list, .permission-grid { display: grid; gap: 12px; }
.doc-item { width: 100%; text-align: left; border: 1px solid var(--line); border-radius: 16px; background: rgba(255, 255, 255, 0.72); padding: 13px 14px; }
.doc-item.active { background: var(--accent-soft); border-color: rgba(11, 122, 117, 0.28); }
.docs-main { display: grid; gap: 20px; }
.docs-editor-surface { display: grid; gap: 18px; }
.docs-workbench { display: grid; gap: 20px; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); align-items: stretch; }
.editor-pane, .preview-pane, .versions-pane { height: 100%; }
.editor-pane { display: grid; gap: 10px; }
.editor-textarea, .editor-textarea textarea { height: 100%; }
.editor-textarea textarea { min-height: 560px; }
.preview-pane {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 10px;
  overflow: hidden;
}
.pane-label {
  color: var(--muted);
  font-size: 14px;
  line-height: 1.5;
}
.markdown-body { line-height: 1.75; min-height: 260px; }
.preview-pane .markdown-body {
  min-height: 560px;
  max-height: 560px;
  overflow: auto;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  padding: 18px 20px;
}
.markdown-body pre { overflow-x: auto; background: #1f2933; color: #f9fafb; padding: 14px; border-radius: 16px; }
.version-card { display: flex; justify-content: space-between; gap: 12px; align-items: center; }
.filters { display: grid; gap: 12px; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.filters-wide { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.permission-grid { max-height: 260px; overflow: auto; }
.permission-item { display: flex; gap: 10px; align-items: center; border: 1px solid var(--line); border-radius: 14px; padding: 12px; background: rgba(255, 255, 255, 0.64); }
@media (max-width: 1200px) {
  .dashboard-layout, .docs-layout, .docs-workbench, .two-col, .filters-wide { grid-template-columns: 1fr; }
  .dashboard-hero, .dashboard-side, .dashboard-modules { grid-column: auto; }
  .stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .compact-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .preview-pane .markdown-body, .editor-textarea textarea { min-height: 360px; max-height: none; }
}
@media (max-width: 860px) {
  .app-shell { padding: 16px; }
  .topbar, .section-head { flex-direction: column; align-items: stretch; }
  .stats, .filters { grid-template-columns: 1fr; }
  .compact-stats { grid-template-columns: 1fr; }
}
</style>
