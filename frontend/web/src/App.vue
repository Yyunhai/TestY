<template>
  <div class="app">
    <AuthPage
      v-if="!authenticated"
      :mode="authMode"
      :message="message"
      :login-form="loginForm"
      :register-form="registerForm"
      :reset-form="resetForm"
      :remember-password="loginRememberPassword"
      :busy="authBusy"
      @update:mode="authMode = $event"
      @update:login-form="loginForm = $event"
      @update:register-form="registerForm = $event"
      @update:reset-form="resetForm = $event"
      @update:remember-password="loginRememberPassword = $event"
      @submit="submitAuth"
    />

    <WorkspaceShell
      v-else
      ref="workspaceShell"
      :user="user"
      :current-page="currentPage"
      :admin-section="adminSection"
      :message="message"
      :sidebar-collapsed="sidebarCollapsed"
      :can-read-docs="canReadDocs"
      :can-read-discussion="canReadDiscussion"
      :can-access-admin="canAccessAdmin"
      @navigate="setPage"
      @logout="logout"
      @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
    >
      <template v-slot:default>
        <div class="page-transition" ref="pageTransition">
          <DashboardPage
            v-if="currentPage === 'dashboard'"
            :user="user"
            :documents="documents"
            :can-read-docs="canReadDocs"
            @navigate="setPage"
            @open-doc="openDocumentFromDashboard"
          />

          <DocsPage
            v-else-if="currentPage === 'docs' && canReadDocs"
            :documents="documents"
            :search-keyword="documentSearchKeyword"
            :current-document-id="currentDocumentId"
            :document-form="documentForm"
            :document-versions="documentVersions"
            :save-hint="saveHint"
            :rendered-markdown="renderedMarkdown"
            :document-word-count="documentWordCount"
            @update:search-keyword="documentSearchKeyword = $event; updateHash()"
            @update:document-form="onDocumentFormUpdate"
            @new-document="startNewDocument"
            @open-document="openDocument"
            @save="saveDocument"
            @delete-document="deleteDocument"
            @refresh="loadDocuments"
            @refresh-versions="loadVersions"
            @restore-version="restoreVersion"
          />

          <CommunityPage
            v-else-if="currentPage === 'community'"
            :user="user"
          />

          <MyProfilePage
            v-else-if="currentPage === 'profile'"
            :user="user"
          />

          <AdminPage
            v-else-if="currentPage === 'admin' && canAccessAdmin"
            :section="adminSection"
            :user="user"
            :users="adminUsers"
            :roles="adminRoles"
            :permissions="adminPermissions"
            :errors="adminErrors"
            :audit-alerts="auditAlerts"
            :audit-page="auditPage"
            :operation-page="adminOperationPage"
            :user-filters="userFilters"
            :user-role-selections="userRoleSelections"
            :user-status-selections="userStatusSelections"
            :role-form="roleForm"
            :permission-keyword="permissionKeyword"
            :audit-filters="auditFilters"
            :operation-filters="operationFilters"
            :account-status-options="accountStatusOptions"
            :can-read-users="canReadAdminUsers"
            :can-write-users="canWriteAdminUsers"
            :can-create-users="canCreateAdminUsers"
            :can-delete-users="canDeleteAdminUsers"
            :can-reset-user-passwords="canResetAdminUserPasswords"
            :can-read-roles="canReadAdminRoles"
            :can-write-roles="canWriteAdminRoles"
            :can-delete-roles="canDeleteAdminRoles"
            :can-read-permissions="canReadAdminPermissions"
            :can-read-logins="canReadAdminLogins"
            :can-read-operation-logs="canReadAdminOperationLogs"
            :create-user-form="createUserForm"
            :reset-password-form="resetPasswordForm"
            :show-create-user-dialog="showCreateUserDialog"
            :show-reset-password-dialog="showResetPasswordDialog"
            @change-section="changeAdminSection"
            @refresh="loadAdminData"
            @update:user-filters="userFilters = $event"
            @save-user-roles="saveUserRoles"
            @save-user-status="saveUserStatus"
            @create-user="createUser"
            @delete-user="deleteUser"
            @reset-user-password="openResetPasswordDialog"
            @submit-reset-password="resetUserPassword"
            @edit-role="editRole"
            @reset-role-form="resetRoleForm"
            @submit-role="submitRoleForm"
            @delete-role="deleteRole"
            @update:permission-keyword="permissionKeyword = $event"
            @update:role-form="roleForm = $event"
            @update:user-role-selections="userRoleSelections = $event"
            @update:user-status-selections="userStatusSelections = $event"
            @update:audit-filters="auditFilters = $event"
            @load-audits="loadAudits"
            @reset-audit-filters="resetAuditFilters"
            @update:operation-filters="operationFilters = $event"
            @load-operations="loadOperationLogs"
            @reset-operation-filters="resetOperationFilters"
            @update:create-user-form="createUserForm = $event"
            @update:reset-password-form="resetPasswordForm = $event"
            @update:show-create-user-dialog="showCreateUserDialog = $event"
            @update:show-reset-password-dialog="showResetPasswordDialog = $event"
          />
        </div>
      </template>
    </WorkspaceShell>
  </div>
</template>

<script>
import { gsap } from "gsap";
import MarkdownIt from "markdown-it";
import markdownItTaskLists from "markdown-it-task-lists";
import hljs from "highlight.js";

import AuthPage from "./components/AuthPage.vue";
import WorkspaceShell from "./components/WorkspaceShell.vue";
import DashboardPage from "./components/DashboardPage.vue";
import DocsPage from "./components/DocsPage.vue";
import AdminPage from "./components/AdminPage.vue";
import CommunityPage from "./components/CommunityPage.vue";
import MyProfilePage from "./components/MyProfilePage.vue";

const LOGIN_USERNAME_STORAGE_KEY = "testy.login.username";
const LOGIN_PASSWORD_STORAGE_KEY = "testy.login.password";

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

const emptyUser = () => ({ id: null, username: "", displayName: "", roles: [], permissions: [], rootAdmin: false, lastLoginAt: null });
const emptyDoc = () => ({ title: "", content: "", createdAt: null, updatedAt: null });
const emptyRole = () => ({ id: null, code: "", name: "", description: "", permissionIds: [] });
const emptyAuditPage = () => ({ content: [], number: 0, totalPages: 1, totalElements: 0 });

export default {
  name: "AppRoot",
  components: { AuthPage, WorkspaceShell, DashboardPage, DocsPage, AdminPage, CommunityPage, MyProfilePage },
  data() {
    return {
      authMode: "login",
      authBusy: false,
      authenticated: false,
      user: emptyUser(),
      message: { type: "info", text: "" },
      loginForm: { usernameOrEmail: "", password: "" },
      loginRememberPassword: false,
      registerForm: { username: "", email: "", password: "", displayName: "", phoneNumber: "" },
      resetForm: { username: "", email: "", newPassword: "" },
      currentPage: "dashboard",
      sidebarCollapsed: false,
      overview: { applicationName: "", javaVersion: "", message: "", modules: [] },
      documents: [],
      documentSearchKeyword: "",
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
      adminErrors: { overview: "", users: "", roles: "", permissions: "", audits: "", operations: "" },
      auditAlerts: { failedAttemptsLast24Hours: 0, suspiciousPrincipals: [] },
      auditPage: emptyAuditPage(),
      adminOperationPage: emptyAuditPage(),
      userFilters: { keyword: "", status: "" },
      userRoleSelections: {},
      userStatusSelections: {},
      roleForm: emptyRole(),
      permissionKeyword: "",
      auditFilters: { page: 0, size: 10, principal: "", success: "", remoteIp: "" },
      operationFilters: { page: 0, size: 10, module: "", action: "", operatorUsername: "", success: "" },
      accountStatusOptions: ["ACTIVE", "LOCKED", "DISABLED"],
      createUserForm: { username: "", email: "", password: "", displayName: "", phoneNumber: "", roleIds: [] },
      resetPasswordForm: { userId: null, newPassword: "" },
      showCreateUserDialog: false,
      showResetPasswordDialog: false
    };
  },
  computed: {
    canReadDocs() { return this.hasPermission("docs:read"); },
    canReadDiscussion() { return this.hasPermission("discussion:read"); },
    canWriteDocs() { return this.hasPermission("docs:write"); },
    canReadAdminUsers() { return this.hasPermission("admin:users:read"); },
    canWriteAdminUsers() { return this.hasPermission("admin:users:write"); },
    canReadAdminRoles() { return this.hasPermission("admin:roles:read"); },
    canWriteAdminRoles() { return this.hasPermission("admin:roles:write"); },
    canReadAdminPermissions() { return this.hasPermission("admin:permissions:read"); },
    canReadAdminLogins() { return this.hasPermission("admin:logins:read"); },
    canReadAdminOperationLogs() { return this.hasPermission("admin:operation-logs:read"); },
    canCreateAdminUsers() { return this.hasPermission("admin:users:create"); },
    canDeleteAdminUsers() { return this.hasPermission("admin:users:delete"); },
    canResetAdminUserPasswords() { return this.hasPermission("admin:users:reset-password"); },
    canDeleteAdminRoles() { return this.hasPermission("admin:roles:delete"); },
    canAccessAdmin() {
      return this.canReadAdminUsers
        || this.canWriteAdminUsers
        || this.canCreateAdminUsers
        || this.canDeleteAdminUsers
        || this.canResetAdminUserPasswords
        || this.canReadAdminRoles
        || this.canWriteAdminRoles
        || this.canDeleteAdminRoles
        || this.canReadAdminPermissions
        || this.canReadAdminLogins
        || this.canReadAdminOperationLogs;
    },
    renderedMarkdown() { return md.render(this.documentForm.content || ""); },
    documentWordCount() { return (this.documentForm.content || "").replace(/\s+/g, "").length; }
  },
  watch: {
    "loginForm.usernameOrEmail"(value) {
      this.persistLoginUsername(value);
    },
    "loginForm.password"(value) {
      if (this.loginRememberPassword) this.persistLoginPassword(value);
    },
    loginRememberPassword(value) {
      if (value) {
        this.persistLoginPassword(this.loginForm.password);
      } else {
        this.removeStoredValue(LOGIN_PASSWORD_STORAGE_KEY);
      }
    },
    "documentForm.title"() { this.queueAutosave(); },
    "documentForm.content"() { this.queueAutosave(); },
    currentPage() {
      this.$nextTick(() => this.animatePageEnter());
    }
  },
  async mounted() {
    this.restoreLoginPreferences();
    window.addEventListener("hashchange", this.handleHashChange);
    await this.bootstrap();
  },
  beforeUnmount() {
    window.removeEventListener("hashchange", this.handleHashChange);
    if (this.autosaveTimer) clearTimeout(this.autosaveTimer);
  },
  methods: {
    hasPermission(code) { return Boolean(this.user.rootAdmin) || (this.user.permissions || []).includes(code); },
    setMessage(text, type = "info") {
      this.message = { text, type };
      if (this._messageTimer) clearTimeout(this._messageTimer);
      this._messageTimer = setTimeout(() => { this.message = { text: "", type: "info" }; }, 3000);
    },
    clearMessage() {
      if (this._messageTimer) clearTimeout(this._messageTimer);
      this.message = { text: "", type: "info" };
    },
    animatePageEnter() {
      const el = this.$refs.pageTransition;
      if (!el) return;
      gsap.fromTo(el, { opacity: 0, y: 20 }, { opacity: 1, y: 0, duration: 0.5, ease: "power2.out", clearProps: "transform" });
    },
    readStoredValue(key) {
      try { return window.localStorage.getItem(key) || ""; } catch (e) { return ""; }
    },
    persistStoredValue(key, value) {
      try {
        if (!value) { window.localStorage.removeItem(key); return; }
        window.localStorage.setItem(key, value);
      } catch (e) { void e; }
    },
    removeStoredValue(key) {
      try { window.localStorage.removeItem(key); } catch (e) { void e; }
    },
    persistLoginUsername(value) {
      this.persistStoredValue(LOGIN_USERNAME_STORAGE_KEY, String(value || "").trim());
    },
    persistLoginPassword(value) {
      this.persistStoredValue(LOGIN_PASSWORD_STORAGE_KEY, value || "");
    },
    restoreLoginPreferences() {
      const rememberedUsername = this.readStoredValue(LOGIN_USERNAME_STORAGE_KEY);
      const rememberedPassword = this.readStoredValue(LOGIN_PASSWORD_STORAGE_KEY);
      this.loginRememberPassword = Boolean(rememberedPassword);
      this.loginForm.usernameOrEmail = rememberedUsername;
      this.loginForm.password = rememberedPassword;
    },
    syncLoginPreferences() {
      this.persistLoginUsername(this.loginForm.usernameOrEmail);
      if (this.loginRememberPassword) {
        this.persistLoginPassword(this.loginForm.password);
      } else {
        this.removeStoredValue(LOGIN_PASSWORD_STORAGE_KEY);
      }
    },
    isAuthRequiredError(error) {
      return Boolean(error && /Authentication required/i.test(error.message || ""));
    },
    resetClientSession(message = "", type = "info") {
      this.authenticated = false;
      this.user = emptyUser();
      this.documents = [];
      this.documentSearchKeyword = "";
      this.currentDocumentId = null;
      this.documentForm = emptyDoc();
      this.documentVersions = [];
      this.adminUsers = [];
      this.adminRoles = [];
      this.adminPermissions = [];
      this.auditAlerts = { failedAttemptsLast24Hours: 0, suspiciousPrincipals: [] };
      this.auditPage = emptyAuditPage();
      this.adminOperationPage = emptyAuditPage();
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
        this.$nextTick(() => this.animatePageEnter());
      } catch (error) {
        this.authenticated = false;
      }
    },
    applyAuth(data) {
      this.user = {
        id: data.id || null,
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
          this.syncLoginPreferences();
          this.applyAuth(result);
          this.authenticated = true;
          this.syncFromHash();
          await this.loadCurrentPage();
          this.setMessage("登录成功。", "success");
        } else if (this.authMode === "register") {
          await this.apiRequest("/api/auth/register", { method: "POST", body: this.registerForm });
          this.loginForm.usernameOrEmail = this.registerForm.username || this.registerForm.email || this.loginForm.usernameOrEmail;
          this.loginForm.password = "";
          this.authMode = "login";
          this.setMessage("注册成功，请直接登录。", "success");
        } else {
          await this.apiRequest("/api/auth/reset-password", { method: "POST", body: this.resetForm });
          this.loginForm.usernameOrEmail = this.resetForm.username || this.loginForm.usernameOrEmail;
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
      const prev = this.currentPage;
      const prevAdmin = this.adminSection;
      this.syncFromHash();
      if (prev !== this.currentPage || prevAdmin !== this.adminSection) {
        this.loadCurrentPage();
      }
    },
    parseHash() {
      const rawHash = (window.location.hash || "").replace(/^#/, "");
      const [pathPart, queryPart = ""] = rawHash.split("?");
      const [page = "", section = ""] = pathPart.split("/");
      return { page, section, query: new URLSearchParams(queryPart) };
    },
    syncFromHash() {
      const { page, section, query } = this.parseHash();
      if (page === "docs" && this.canReadDocs) {
        this.currentPage = "docs";
        this.documentSearchKeyword = query.get("q") || "";
      } else if (page === "community" && this.canReadDiscussion) {
        this.currentPage = "community";
        this.documentSearchKeyword = "";
      } else if (page === "profile") {
        this.currentPage = "profile";
        this.documentSearchKeyword = "";
      } else if (page === "admin" && this.canAccessAdmin) {
        this.currentPage = "admin";
        const validSections = ["overview", "users", "roles", "audits", "operations"];
        this.adminSection = validSections.includes(section) ? section : "overview";
        this.documentSearchKeyword = "";
      } else {
        this.currentPage = "dashboard";
        this.adminSection = "overview";
        this.documentSearchKeyword = "";
      }
    },
    updateHash() {
      let nextHash = "";
      if (this.currentPage === "docs") {
        const params = new URLSearchParams();
        if (this.documentSearchKeyword) params.set("q", this.documentSearchKeyword);
        nextHash = params.toString() ? `#docs?${params.toString()}` : "#docs";
      } else if (this.currentPage === "community") {
        nextHash = "#community";
      } else if (this.currentPage === "profile") {
        nextHash = "#profile";
      } else if (this.currentPage === "admin") {
        nextHash = `#admin/${this.adminSection}`;
      }
      if (window.location.hash !== nextHash) {
        window.location.hash = nextHash;
      }
    },
    async setPage(page) {
      if (page === "admin" && !this.canAccessAdmin) {
        this.currentPage = "dashboard";
        this.adminSection = "overview";
        this.updateHash();
        return;
      }
      if (page === "community" && !this.canReadDiscussion) {
        return;
      }
      this.currentPage = page;
      if (page !== "admin") this.adminSection = "overview";
      if (page !== "docs") this.documentSearchKeyword = "";
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
    onDocumentFormUpdate(form) {
      this.documentForm = form;
    },
    startNewDocument(showMessage = true) {
      this.currentDocumentId = null;
      this.documentVersions = [];
      this.suppressAutosave = true;
      this.documentForm = emptyDoc();
      this.$nextTick(() => { this.suppressAutosave = false; });
      this.saveHint = "已开启自动保存";
      if (showMessage) this.setMessage("已创建空白草稿。", "success");
    },
    openDocumentFromDashboard(docId) {
      this.setPage("docs").then(() => this.openDocument(docId));
    },
    async openDocument(id) {
      try {
        const doc = await this.apiRequest(`/api/docs/${id}`);
        this.currentDocumentId = id;
        this.suppressAutosave = true;
        this.documentForm = { title: doc.title || "", content: doc.content || "", createdAt: doc.createdAt || null, updatedAt: doc.updatedAt || null };
        this.$nextTick(() => { this.suppressAutosave = false; });
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
        this.suppressAutosave = true;
        this.documentForm = { title: saved.title || "", content: saved.content || "", createdAt: saved.createdAt || null, updatedAt: saved.updatedAt || null };
        this.$nextTick(() => { this.suppressAutosave = false; });
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
        this.suppressAutosave = true;
        this.documentForm = emptyDoc();
        this.$nextTick(() => { this.suppressAutosave = false; });
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
        this.suppressAutosave = true;
        this.documentForm = { title: restored.title || "", content: restored.content || "", createdAt: restored.createdAt || null, updatedAt: restored.updatedAt || null };
        this.$nextTick(() => { this.suppressAutosave = false; });
        await this.loadDocuments();
        await this.loadVersions();
        this.setMessage("版本已恢复。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "恢复版本失败。", "error");
      }
    },
    async loadAdminData() {
      this.adminErrors = { overview: "", users: "", roles: "", permissions: "", audits: "", operations: "" };
      if (this.canReadAdminRoles) await this.loadRoles();
      if (this.canReadAdminPermissions) await this.loadPermissions();
      if (this.canReadAdminUsers) await this.loadUsers();
      if (this.canReadAdminLogins) {
        await this.loadAuditAlerts();
        if (this.adminSection === "audits") await this.loadAudits(this.auditFilters.page || 0);
      }
      if (this.canReadAdminOperationLogs && this.adminSection === "operations") {
        await this.loadOperationLogs(this.operationFilters.page || 0);
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
        this.adminErrors.overview = this.adminErrors.roles;
      }
    },
    async loadPermissions() {
      try { this.adminPermissions = await this.apiRequest("/api/admin/permissions"); }
      catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.permissions = error.message || "权限列表加载失败。";
        this.adminErrors.roles = this.adminErrors.permissions;
        this.adminErrors.overview = this.adminErrors.permissions;
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
    async loadOperationLogs(page = 0) {
      try {
        this.operationFilters.page = Math.max(page, 0);
        const params = new URLSearchParams({ page: String(this.operationFilters.page), size: String(this.operationFilters.size) });
        if (this.operationFilters.module) params.set("module", this.operationFilters.module);
        if (this.operationFilters.action) params.set("action", this.operationFilters.action);
        if (this.operationFilters.operatorUsername) params.set("operatorUsername", this.operationFilters.operatorUsername);
        if (this.operationFilters.success !== "") params.set("success", this.operationFilters.success);
        this.adminOperationPage = await this.apiRequest(`/api/admin/operation-logs?${params.toString()}`);
      } catch (error) {
        if (this.isAuthRequiredError(error)) return;
        this.adminErrors.operations = error.message || "操作日志加载失败。";
      }
    },
    resetOperationFilters() {
      this.operationFilters = { page: 0, size: 10, module: "", action: "", operatorUsername: "", success: "" };
      this.loadOperationLogs(0);
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
    resetCreateUserForm() {
      this.createUserForm = { username: "", email: "", password: "", displayName: "", phoneNumber: "", roleIds: [] };
    },
    async createUser() {
      try {
        await this.apiRequest("/api/admin/users", { method: "POST", body: this.createUserForm });
        this.showCreateUserDialog = false;
        this.resetCreateUserForm();
        await this.loadUsers();
        this.setMessage("用户创建成功。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "用户创建失败。", "error");
      }
    },
    async deleteUser(userId) {
      if (!window.confirm("确定删除该用户吗？此操作不可撤销。")) return;
      try {
        await this.apiRequest(`/api/admin/users/${userId}`, { method: "DELETE" });
        await this.loadUsers();
        this.setMessage("用户已删除。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "用户删除失败。", "error");
      }
    },
    openResetPasswordDialog(user) {
      this.resetPasswordForm = { userId: user.id, newPassword: "" };
      this.showResetPasswordDialog = true;
    },
    async resetUserPassword() {
      if (!this.resetPasswordForm.newPassword) { this.setMessage("请输入新密码。", "error"); return; }
      try {
        await this.apiRequest(`/api/admin/users/${this.resetPasswordForm.userId}/reset-password`, { method: "POST", body: { newPassword: this.resetPasswordForm.newPassword } });
        this.showResetPasswordDialog = false;
        this.resetPasswordForm = { userId: null, newPassword: "" };
        this.setMessage("密码重置成功。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "密码重置失败。", "error");
      }
    },
    async deleteRole(roleId) {
      if (!window.confirm("确定删除该角色吗？此操作不可撤销。")) return;
      try {
        await this.apiRequest(`/api/admin/roles/${roleId}`, { method: "DELETE" });
        await this.loadRoles();
        this.setMessage("角色已删除。", "success");
      } catch (error) {
        if (!this.isAuthRequiredError(error)) this.setMessage(error.message || "角色删除失败。", "error");
      }
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
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap');

:root {
  --bg: #f7f5f2;
  --bg-secondary: #efece7;
  --surface: #ffffff;
  --border: rgba(0, 0, 0, 0.08);
  --border-hover: rgba(0, 0, 0, 0.15);

  --text: #1a1612;
  --text-secondary: #4a4540;
  --text-muted: #8a857e;
  --text-light: #b5b0a8;

  --accent: #c2410c;
  --accent-hover: #a83608;
  --accent-bg: rgba(194, 65, 12, 0.08);

  --success: #3a6b4a;
  --danger: #c2410c;
  --danger-bg: rgba(194, 65, 12, 0.06);

  --shadow-sm: 0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.02);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.06), 0 2px 4px rgba(0,0,0,0.03);
  --shadow-lg: 0 12px 40px rgba(0,0,0,0.08), 0 4px 12px rgba(0,0,0,0.04);
}

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

html { -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale; }

body {
  font-family: "DM Sans", -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif;
  background: var(--bg);
  color: var(--text);
  line-height: 1.5;
}

button { cursor: pointer; font: inherit; }
input, select, textarea { font: inherit; }

.markdown-body { line-height: 1.75; color: var(--text); }
.markdown-body h1, .markdown-body h2, .markdown-body h3 { margin: 1.2em 0 0.5em; font-weight: 700; }
.markdown-body h1 { font-size: 1.6em; }
.markdown-body h2 { font-size: 1.35em; }
.markdown-body h3 { font-size: 1.15em; }
.markdown-body p { margin: 0.6em 0; }
.markdown-body ul, .markdown-body ol { padding-left: 1.8em; margin: 0.6em 0; }
.markdown-body li { margin: 0.3em 0; }
.markdown-body code {
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--bg-secondary);
  font-family: "JetBrains Mono", "Fira Code", monospace;
  font-size: 0.9em;
}
.markdown-body pre { margin: 1em 0; border-radius: 12px; overflow-x: auto; }
.markdown-body pre code { padding: 0; background: none; }
.markdown-body pre.hljs {
  padding: 18px 20px;
  background: #faf8f5;
  border: 1px solid var(--border);
  font-size: 13px;
  line-height: 1.6;
}
.markdown-body blockquote {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 3px solid var(--accent);
  color: var(--text-secondary);
  background: var(--accent-bg);
  border-radius: 0 8px 8px 0;
}
.markdown-body table { width: 100%; border-collapse: collapse; margin: 1em 0; }
.markdown-body th, .markdown-body td { padding: 8px 12px; border: 1px solid var(--border); text-align: left; }
.markdown-body th { background: var(--bg-secondary); font-weight: 600; }
.markdown-body a { color: var(--accent); text-decoration: none; }
.markdown-body a:hover { text-decoration: underline; }
.markdown-body hr { border: none; border-top: 1px solid var(--border); margin: 1.5em 0; }
.markdown-body img { max-width: 100%; border-radius: 8px; }
.markdown-body input[type="checkbox"] { accent-color: var(--accent); margin-right: 6px; }

.app { min-height: 100vh; }
</style>
