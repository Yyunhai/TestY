<template>
  <main class="app-shell">
    <section v-if="!currentUser" class="auth-view">
      <div class="auth-hero">
        <p class="eyebrow">TestY Account Center</p>
        <h1>一个界面专注登录，一个界面专注使用。</h1>
        <p class="hero-text">
          未登录时只展示账号相关操作；登录成功后，再进入项目工作台，页面逻辑更清晰，也更接近真实产品体验。
        </p>

        <div class="hero-points">
          <article class="point-card">
            <span class="point-label">账号规则</span>
            <strong>用户名至少 6 位</strong>
            <p>邮箱用于找回密码，密码当前规则为至少 6 位。</p>
          </article>
          <article class="point-card">
            <span class="point-label">开发方式</span>
            <strong>Vue + Spring Boot</strong>
            <p>前端位于 <code>web/</code>，后端通过本地 MySQL 保存账号信息。</p>
          </article>
          <article class="point-card">
            <span class="point-label">当前能力</span>
            <strong>注册、登录、找回密码</strong>
            <p>所有认证能力都通过同一套后端接口完成。</p>
          </article>
        </div>
      </div>

      <div class="auth-card fade-up">
        <div class="card-headline">
          <div>
            <p class="section-tag">欢迎使用</p>
            <h2>{{ authPanelTitle }}</h2>
          </div>
          <span class="state-pill">安全连接</span>
        </div>

        <div class="tab-row">
          <button
            v-for="item in tabs"
            :key="item.key"
            type="button"
            class="tab-button"
            :class="{ active: activeTab === item.key }"
            @click="switchTab(item.key)"
          >
            {{ item.label }}
          </button>
        </div>

        <p v-if="message.text" class="message-bar" :class="message.type">
          {{ message.text }}
        </p>

        <form v-if="activeTab === 'login'" class="form-grid" @submit.prevent="submitLogin">
          <label>
            <span>用户名或邮箱</span>
            <input v-model.trim="loginForm.usernameOrEmail" type="text" placeholder="请输入用户名或邮箱" />
          </label>
          <label>
            <span>密码</span>
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在登录..." : "登录" }}
          </button>
        </form>

        <form v-else-if="activeTab === 'register'" class="form-grid" @submit.prevent="submitRegister">
          <label>
            <span>用户名</span>
            <input v-model.trim="registerForm.username" type="text" minlength="6" placeholder="不少于 6 位" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model.trim="registerForm.email" type="email" placeholder="name@example.com" />
          </label>
          <label>
            <span>密码</span>
            <input v-model="registerForm.password" type="password" minlength="6" placeholder="不少于 6 位" />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在提交..." : "注册账号" }}
          </button>
        </form>

        <form v-else class="form-grid" @submit.prevent="submitReset">
          <label>
            <span>用户名</span>
            <input v-model.trim="resetForm.username" type="text" minlength="6" placeholder="请输入用户名" />
          </label>
          <label>
            <span>邮箱</span>
            <input v-model.trim="resetForm.email" type="email" placeholder="请输入注册邮箱" />
          </label>
          <label>
            <span>新密码</span>
            <input v-model="resetForm.newPassword" type="password" minlength="6" placeholder="请输入新密码" />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在提交..." : "重置密码" }}
          </button>
        </form>

        <div class="api-hint">
          <span>接口地址</span>
          <code>/api/auth/register</code>
          <code>/api/auth/login</code>
          <code>/api/auth/reset-password</code>
        </div>
      </div>
    </section>

    <section v-else class="workspace-view fade-up">
      <header class="workspace-header">
        <div>
          <p class="eyebrow">TestY Workspace</p>
          <h1>欢迎回来，{{ currentUser.username }}</h1>
          <p class="hero-text">
            你已经完成登录。这里展示当前账号、项目概览和可继续操作的内容。
          </p>
        </div>

        <div class="header-actions">
          <button class="ghost-button" type="button" @click="loadOverview" :disabled="overviewLoading">
            {{ overviewLoading ? "正在刷新..." : "刷新概览" }}
          </button>
          <button class="danger-button" type="button" @click="logout">退出登录</button>
        </div>
      </header>

      <section class="workspace-grid">
        <article class="panel profile-panel">
          <div class="panel-title">
            <div>
              <p class="section-tag">当前账号</p>
              <h2>登录信息</h2>
            </div>
            <span class="status-chip">已登录</span>
          </div>

          <div class="profile-card">
            <div class="profile-item">
              <span>用户名</span>
              <strong>{{ currentUser.username }}</strong>
            </div>
            <div class="profile-item">
              <span>邮箱</span>
              <strong>{{ currentUser.email }}</strong>
            </div>
          </div>

          <div class="tip-box">
            <strong>当前状态</strong>
            <p>登录状态保存在浏览器本地，刷新页面后仍会保留，直到你主动退出登录。</p>
          </div>
        </article>

        <article class="panel overview-panel">
          <div class="panel-title">
            <div>
              <p class="section-tag">项目概览</p>
              <h2>后端运行信息</h2>
            </div>
          </div>

          <p v-if="overviewError" class="overview-error">{{ overviewError }}</p>

          <template v-else>
            <div class="overview-metrics">
              <article class="metric-card">
                <span>应用名称</span>
                <strong>{{ overview.applicationName || "加载中" }}</strong>
              </article>
              <article class="metric-card">
                <span>Java 版本</span>
                <strong>{{ overview.javaVersion || "加载中" }}</strong>
              </article>
              <article class="metric-card metric-wide">
                <span>运行说明</span>
                <strong>{{ overview.message || "正在获取项目状态..." }}</strong>
              </article>
            </div>

            <div class="module-panel">
              <div class="module-panel-head">
                <span class="section-tag">模块列表</span>
                <span class="module-count">{{ overview.modules.length }} 个模块</span>
              </div>
              <div class="module-list">
                <span v-for="moduleName in overview.modules" :key="moduleName" class="module-chip">
                  {{ moduleName }}
                </span>
              </div>
            </div>
          </template>
        </article>

        <article class="panel action-panel">
          <div class="panel-title">
            <div>
              <p class="section-tag">下一步</p>
              <h2>你现在可以做什么</h2>
            </div>
          </div>

          <div class="action-list">
            <div class="action-item">
              <strong>查看接口状态</strong>
              <p>访问 <code>/api/overview</code>，确认后端接口和模块信息正常返回。</p>
            </div>
            <div class="action-item">
              <strong>继续扩展业务</strong>
              <p>可以在当前登录态基础上继续接入用户中心、权限控制或业务页面。</p>
            </div>
            <div class="action-item">
              <strong>打包部署</strong>
              <p>前端构建后会进入 Spring Boot 静态资源目录，也可以继续使用 Docker 部署。</p>
            </div>
          </div>
        </article>
      </section>
    </section>
  </main>
</template>

<script>
const SESSION_KEY = "testy-user";

function emptyOverview() {
  return {
    applicationName: "",
    javaVersion: "",
    message: "",
    modules: []
  };
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
      busy: false,
      overviewLoading: false,
      overviewError: "",
      message: {
        type: "info",
        text: ""
      },
      currentUser: null,
      overview: emptyOverview(),
      loginForm: {
        usernameOrEmail: "",
        password: ""
      },
      registerForm: {
        username: "",
        email: "",
        password: ""
      },
      resetForm: {
        username: "",
        email: "",
        newPassword: ""
      }
    };
  },
  computed: {
    authPanelTitle() {
      if (this.activeTab === "register") {
        return "创建一个新账号";
      }
      if (this.activeTab === "reset") {
        return "通过邮箱找回密码";
      }
      return "登录到你的账号";
    }
  },
  mounted() {
    const savedUser = window.localStorage.getItem(SESSION_KEY);
    if (!savedUser) {
      return;
    }

    try {
      this.currentUser = JSON.parse(savedUser);
      this.loadOverview();
    } catch (error) {
      window.localStorage.removeItem(SESSION_KEY);
    }
  },
  methods: {
    switchTab(tab) {
      this.activeTab = tab;
      this.clearMessage();
    },
    setMessage(type, text) {
      this.message = { type, text };
    },
    clearMessage() {
      this.message = { type: "info", text: "" };
    },
    async submitLogin() {
      this.busy = true;
      this.clearMessage();

      try {
        const data = await this.postJson("/api/auth/login", this.loginForm);
        this.currentUser = { username: data.username, email: data.email };
        window.localStorage.setItem(SESSION_KEY, JSON.stringify(this.currentUser));
        this.loginForm.password = "";
        this.setMessage("success", data.message);
        await this.loadOverview();
      } catch (error) {
        this.setMessage("error", error.message);
      } finally {
        this.busy = false;
      }
    },
    async submitRegister() {
      this.busy = true;
      this.clearMessage();

      try {
        const data = await this.postJson("/api/auth/register", this.registerForm);
        this.setMessage("success", data.message + " 现在可以直接登录。");
        this.loginForm.usernameOrEmail = this.registerForm.username;
        this.loginForm.password = "";
        this.resetForm.username = this.registerForm.username;
        this.resetForm.email = this.registerForm.email;
        this.registerForm.password = "";
        this.activeTab = "login";
      } catch (error) {
        this.setMessage("error", error.message);
      } finally {
        this.busy = false;
      }
    },
    async submitReset() {
      this.busy = true;
      this.clearMessage();

      try {
        const data = await this.postJson("/api/auth/reset-password", this.resetForm);
        this.setMessage("success", data.message + " 请使用新密码重新登录。");
        this.loginForm.usernameOrEmail = this.resetForm.username;
        this.loginForm.password = "";
        this.activeTab = "login";
      } catch (error) {
        this.setMessage("error", error.message);
      } finally {
        this.busy = false;
      }
    },
    async loadOverview() {
      this.overviewLoading = true;
      this.overviewError = "";

      try {
        const response = await fetch("/api/overview");
        if (!response.ok) {
          throw new Error("项目概览加载失败，请稍后重试。");
        }
        this.overview = await response.json();
      } catch (error) {
        this.overview = emptyOverview();
        this.overviewError = error.message;
      } finally {
        this.overviewLoading = false;
      }
    },
    logout() {
      this.currentUser = null;
      this.overview = emptyOverview();
      this.overviewError = "";
      window.localStorage.removeItem(SESSION_KEY);
      this.setMessage("info", "你已退出登录。");
      this.activeTab = "login";
    },
    async postJson(url, payload) {
      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });

      const contentType = response.headers.get("content-type") || "";
      let data = null;
      let text = "";

      if (contentType.includes("application/json")) {
        data = await response.json();
      } else {
        text = await response.text();
      }

      if (!response.ok) {
        throw new Error((data && data.message) || text || "请求失败，请稍后再试。");
      }

      if (!data || !data.success) {
        throw new Error((data && data.message) || "请求失败，请稍后再试。");
      }

      return data;
    }
  }
};
</script>

<style>
:root {
  color-scheme: light;
  --bg-base: #f6f1e8;
  --bg-soft: #fbf7f1;
  --surface: rgba(255, 252, 247, 0.9);
  --surface-strong: #fffefa;
  --ink: #18212a;
  --muted: #6c756d;
  --line: rgba(24, 33, 42, 0.1);
  --primary: #0f6a68;
  --primary-deep: #173d52;
  --accent: #d46c2f;
  --accent-soft: rgba(212, 108, 47, 0.12);
  --success: #136a49;
  --success-soft: rgba(19, 106, 73, 0.12);
  --danger: #b42318;
  --danger-soft: rgba(180, 35, 24, 0.12);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Trebuchet MS", "Segoe UI", "Microsoft YaHei", sans-serif;
  color: var(--ink);
  background:
    radial-gradient(circle at left top, rgba(15, 106, 104, 0.18), transparent 24%),
    radial-gradient(circle at right bottom, rgba(212, 108, 47, 0.14), transparent 26%),
    linear-gradient(135deg, #f4ede2 0%, #f9f6ef 48%, #eef4f2 100%);
}

button,
input {
  font: inherit;
}

code {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(23, 61, 82, 0.08);
}

#app {
  min-height: 100vh;
}

.app-shell {
  width: min(1220px, calc(100% - 32px));
  margin: 0 auto;
  padding: 34px 0 56px;
}

.auth-view {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 460px);
  gap: 26px;
  align-items: start;
}

.workspace-view {
  display: grid;
  gap: 24px;
}

.auth-hero,
.auth-card,
.panel,
.workspace-header {
  border: 1px solid var(--line);
  border-radius: 30px;
  background: var(--surface);
  box-shadow: 0 24px 60px rgba(24, 33, 42, 0.08);
  backdrop-filter: blur(12px);
}

.auth-hero {
  padding: 36px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.74), rgba(255, 248, 239, 0.92)),
    var(--surface);
}

.auth-card,
.panel {
  padding: 26px;
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  padding: 28px 30px;
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: 22px;
}

.overview-panel {
  grid-row: span 2;
}

.eyebrow,
.section-tag,
.point-label {
  margin: 0 0 10px;
  display: block;
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-hero h1,
.workspace-header h1 {
  margin: 0;
  font-size: clamp(36px, 6vw, 72px);
  line-height: 0.96;
  letter-spacing: -0.04em;
}

.workspace-header h1 {
  font-size: clamp(30px, 4vw, 52px);
}

.hero-text {
  margin: 18px 0 0;
  color: var(--muted);
  font-size: 18px;
  line-height: 1.8;
  max-width: 760px;
}

.hero-points {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 28px;
}

.point-card {
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.75);
}

.point-card strong {
  display: block;
  font-size: 20px;
  line-height: 1.4;
}

.point-card p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.card-headline,
.panel-title {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.card-headline h2,
.panel-title h2 {
  margin: 0;
  font-size: 30px;
}

.state-pill,
.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.state-pill {
  background: rgba(15, 106, 104, 0.1);
  color: var(--primary);
}

.status-chip {
  background: var(--success-soft);
  color: var(--success);
}

.tab-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.tab-button {
  min-height: 44px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.84);
  color: var(--muted);
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.tab-button:hover {
  transform: translateY(-1px);
}

.tab-button.active {
  background: linear-gradient(135deg, var(--ink), var(--primary-deep));
  border-color: transparent;
  color: #fff;
}

.message-bar {
  margin: 0 0 18px;
  padding: 14px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
}

.message-bar.info {
  background: rgba(23, 61, 82, 0.08);
  color: var(--primary-deep);
}

.message-bar.success {
  background: var(--success-soft);
  color: var(--success);
}

.message-bar.error {
  background: var(--danger-soft);
  color: var(--danger);
}

.form-grid {
  display: grid;
  gap: 14px;
}

.form-grid label {
  display: grid;
  gap: 8px;
}

.form-grid span {
  font-size: 14px;
  font-weight: 700;
}

.form-grid input {
  width: 100%;
  min-height: 50px;
  padding: 0 16px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.form-grid input:focus {
  outline: none;
  border-color: rgba(15, 106, 104, 0.4);
  box-shadow: 0 0 0 4px rgba(15, 106, 104, 0.08);
  transform: translateY(-1px);
}

.primary-button,
.ghost-button,
.danger-button {
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.primary-button:hover,
.ghost-button:hover,
.danger-button:hover {
  transform: translateY(-1px);
}

.primary-button:disabled,
.ghost-button:disabled,
.danger-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.primary-button {
  border: 0;
  color: #fff;
  background: linear-gradient(135deg, #17212a, #214154);
  box-shadow: 0 18px 36px rgba(24, 33, 42, 0.16);
}

.ghost-button {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.88);
  color: var(--ink);
}

.danger-button {
  border: 0;
  color: #fff;
  background: linear-gradient(135deg, #8f2e22, #b42318);
}

.api-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
  color: var(--muted);
  font-size: 13px;
}

.api-hint span {
  width: 100%;
  font-weight: 700;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

.profile-card,
.tip-box,
.module-panel,
.metric-card,
.action-item {
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.78);
}

.profile-card {
  padding: 18px;
  display: grid;
  gap: 14px;
}

.profile-item span,
.metric-card span,
.module-count {
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.profile-item strong,
.metric-card strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  line-height: 1.5;
}

.tip-box {
  margin-top: 16px;
  padding: 18px;
}

.tip-box strong {
  display: block;
  font-size: 18px;
}

.tip-box p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.overview-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  padding: 18px;
}

.metric-wide {
  grid-column: 1 / -1;
}

.module-panel {
  margin-top: 16px;
  padding: 18px;
}

.module-panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.module-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.module-chip {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--line);
  font-weight: 700;
}

.action-list {
  display: grid;
  gap: 14px;
}

.action-item {
  padding: 18px;
}

.action-item strong {
  display: block;
  font-size: 18px;
}

.action-item p {
  margin: 8px 0 0;
  color: var(--muted);
  line-height: 1.7;
}

.overview-error {
  margin: 0;
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--danger-soft);
  color: var(--danger);
  line-height: 1.7;
}

.fade-up {
  animation: fadeUp 0.42s ease;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1040px) {
  .auth-view,
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .hero-points,
  .overview-metrics {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .app-shell {
    width: min(100% - 20px, 1220px);
    padding: 20px 0 40px;
  }

  .auth-hero,
  .auth-card,
  .panel,
  .workspace-header {
    padding: 20px;
    border-radius: 24px;
  }

  .workspace-header,
  .card-headline,
  .panel-title,
  .module-panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .tab-row,
  .header-actions {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .tab-row {
    display: grid;
  }

  .ghost-button,
  .danger-button {
    width: 100%;
  }
}
</style>
