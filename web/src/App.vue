<template>
  <main class="page" :class="themeClass">
    <section v-if="!currentUser" class="auth-layout">
      <aside class="brand-panel">
        <div class="brand-top">
          <p class="eyebrow">TestY</p>
          <button class="theme-button" type="button" @click="toggleTheme">
            {{ isDarkMode ? "浅色模式" : "暗色模式" }}
          </button>
        </div>
        <h1>简洁一点，更像真正的登录页。</h1>
        <p class="lead">
          这里只保留账号操作本身。注册、登录、找回密码都集中在一个区域里，不再堆叠多余说明。
        </p>

        <div class="brand-footer">
          <span class="mini-chip">Vue</span>
          <span class="mini-chip">Spring Boot</span>
          <span class="mini-chip">MySQL</span>
        </div>
      </aside>

      <section class="auth-card fade-up">
        <div class="auth-head">
          <div>
            <p class="section-tag">账号中心</p>
            <h2>{{ authTitle }}</h2>
          </div>
          <span class="status-badge">安全连接</span>
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
            <input
              v-model.trim="loginForm.usernameOrEmail"
              type="text"
              placeholder="请输入用户名或邮箱"
            />
          </label>
          <label>
            <span>密码</span>
            <input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
            />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在登录..." : "登录" }}
          </button>
        </form>

        <form v-else-if="activeTab === 'register'" class="form-grid" @submit.prevent="submitRegister">
          <label>
            <span>用户名</span>
            <input
              v-model.trim="registerForm.username"
              type="text"
              minlength="6"
              placeholder="不少于 6 位"
            />
          </label>
          <label>
            <span>邮箱</span>
            <input
              v-model.trim="registerForm.email"
              type="email"
              placeholder="name@example.com"
            />
          </label>
          <label>
            <span>密码</span>
            <input
              v-model="registerForm.password"
              type="password"
              minlength="6"
              placeholder="不少于 6 位"
            />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在提交..." : "注册账号" }}
          </button>
        </form>

        <form v-else class="form-grid" @submit.prevent="submitReset">
          <label>
            <span>用户名</span>
            <input
              v-model.trim="resetForm.username"
              type="text"
              minlength="6"
              placeholder="请输入用户名"
            />
          </label>
          <label>
            <span>邮箱</span>
            <input
              v-model.trim="resetForm.email"
              type="email"
              placeholder="请输入注册邮箱"
            />
          </label>
          <label>
            <span>新密码</span>
            <input
              v-model="resetForm.newPassword"
              type="password"
              minlength="6"
              placeholder="请输入新密码"
            />
          </label>
          <button class="primary-button" type="submit" :disabled="busy">
            {{ busy ? "正在提交..." : "重置密码" }}
          </button>
        </form>

        <p class="hint-text">
          用户名与密码长度均不少于 6 位，邮箱用于找回密码。
        </p>
      </section>
    </section>

    <section v-else class="dashboard fade-up">
      <header class="dashboard-head">
        <div>
          <p class="eyebrow">Workspace</p>
          <h1>欢迎回来，{{ currentUser.username }}</h1>
          <p class="lead small">
            当前页面只保留登录后的核心信息，方便继续开发和调试。
          </p>
        </div>

        <div class="head-actions">
          <button class="theme-button" type="button" @click="toggleTheme">
            {{ isDarkMode ? "浅色模式" : "暗色模式" }}
          </button>
          <button class="secondary-button" type="button" @click="loadOverview" :disabled="overviewLoading">
            {{ overviewLoading ? "刷新中..." : "刷新概览" }}
          </button>
          <button class="danger-button" type="button" @click="logout">退出登录</button>
        </div>
      </header>

      <section class="dashboard-grid">
        <article class="panel account-panel">
          <div class="panel-head">
            <div>
              <p class="section-tag">当前账号</p>
              <h2>登录信息</h2>
            </div>
            <span class="online-mark">已登录</span>
          </div>

          <div class="account-item">
            <span>用户名</span>
            <strong>{{ currentUser.username }}</strong>
          </div>

          <div class="account-item">
            <span>邮箱</span>
            <strong>{{ currentUser.email }}</strong>
          </div>
        </article>

        <article class="panel overview-panel">
          <div class="panel-head">
            <div>
              <p class="section-tag">项目概览</p>
              <h2>运行状态</h2>
            </div>
          </div>

          <p v-if="overviewError" class="message-bar error">
            {{ overviewError }}
          </p>

          <template v-else>
            <div class="metric-grid">
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
                <strong>{{ overview.message || "正在获取后端状态..." }}</strong>
              </article>
            </div>

            <div class="module-box">
              <div class="module-head">
                <span class="section-tag module-tag">模块列表</span>
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
      </section>
    </section>
  </main>
</template>

<script>
const SESSION_KEY = "testy-user";
const THEME_KEY = "testy-theme";

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
      theme: "light",
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
    authTitle() {
      if (this.activeTab === "register") {
        return "创建你的账号";
      }
      if (this.activeTab === "reset") {
        return "找回密码";
      }
      return "登录到 TestY";
    },
    isDarkMode() {
      return this.theme === "dark";
    },
    themeClass() {
      return this.isDarkMode ? "theme-dark" : "theme-light";
    }
  },
  mounted() {
    this.theme = this.resolveTheme();
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
    resolveTheme() {
      const savedTheme = window.localStorage.getItem(THEME_KEY);
      if (savedTheme === "dark" || savedTheme === "light") {
        return savedTheme;
      }

      return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light";
    },
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
    toggleTheme() {
      this.theme = this.isDarkMode ? "light" : "dark";
      window.localStorage.setItem(THEME_KEY, this.theme);
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
  --bg: #f6efe6;
  --paper: rgba(255, 252, 247, 0.9);
  --paper-strong: #fffdfa;
  --ink: #1a2128;
  --muted: #66707a;
  --line: rgba(26, 33, 40, 0.1);
  --primary: #0e6b69;
  --primary-deep: #17394d;
  --accent: #ca6b2c;
  --success: #176946;
  --success-soft: rgba(23, 105, 70, 0.12);
  --danger: #b42318;
  --danger-soft: rgba(180, 35, 24, 0.12);
}

.theme-light {
  color-scheme: light;
}

.theme-dark {
  color-scheme: dark;
  --bg: #11161c;
  --paper: rgba(24, 31, 38, 0.88);
  --paper-strong: #1b232b;
  --ink: #edf3f7;
  --muted: #9ca9b5;
  --line: rgba(237, 243, 247, 0.1);
  --primary: #7ae0d4;
  --primary-deep: #9bd8ff;
  --accent: #ffab6b;
  --success: #7fe0a0;
  --success-soft: rgba(127, 224, 160, 0.14);
  --danger: #ff8f84;
  --danger-soft: rgba(255, 143, 132, 0.14);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Trebuchet MS", "Segoe UI", "Microsoft YaHei", sans-serif;
  background: var(--bg);
}

button,
input {
  font: inherit;
}

code {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(23, 57, 77, 0.08);
}

#app {
  min-height: 100vh;
}

.page {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
  padding: 36px 0 52px;
  min-height: 100vh;
  color: var(--ink);
  background:
    radial-gradient(circle at top left, rgba(14, 107, 105, 0.16), transparent 22%),
    radial-gradient(circle at bottom right, rgba(202, 107, 44, 0.12), transparent 28%),
    linear-gradient(135deg, rgba(245, 238, 228, 0.96) 0%, rgba(248, 247, 242, 0.9) 55%, rgba(237, 244, 242, 0.86) 100%);
}

.theme-dark.page {
  background:
    radial-gradient(circle at top left, rgba(70, 181, 165, 0.18), transparent 22%),
    radial-gradient(circle at bottom right, rgba(255, 171, 107, 0.16), transparent 28%),
    linear-gradient(135deg, rgba(14, 18, 24, 0.98) 0%, rgba(18, 24, 31, 0.94) 55%, rgba(15, 22, 29, 0.96) 100%);
}

.auth-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 430px);
  gap: 24px;
  align-items: stretch;
}

.brand-panel,
.auth-card,
.dashboard-head,
.panel {
  border: 1px solid var(--line);
  border-radius: 28px;
  background: var(--paper);
  box-shadow: 0 22px 60px rgba(26, 33, 40, 0.08);
  backdrop-filter: blur(12px);
}

.brand-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 34px;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.76), rgba(248, 241, 233, 0.92)),
    var(--paper);
}

.theme-dark .brand-panel {
  background:
    linear-gradient(160deg, rgba(30, 38, 47, 0.88), rgba(18, 25, 31, 0.94)),
    var(--paper);
}

.auth-card,
.panel {
  padding: 24px;
}

.dashboard {
  display: grid;
  gap: 22px;
}

.dashboard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(280px, 320px) minmax(0, 1fr);
  gap: 22px;
}

.brand-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow,
.section-tag {
  display: block;
  margin: 0 0 10px;
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-panel h1,
.dashboard-head h1 {
  margin: 0;
  font-size: clamp(36px, 5.8vw, 68px);
  line-height: 0.95;
  letter-spacing: -0.04em;
}

.dashboard-head h1 {
  font-size: clamp(30px, 4vw, 48px);
}

.lead {
  margin: 18px 0 0;
  color: var(--muted);
  font-size: 18px;
  line-height: 1.8;
}

.lead.small {
  font-size: 16px;
}

.brand-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.mini-chip,
.status-badge,
.online-mark,
.module-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.mini-chip {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--line);
}

.theme-button {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--ink);
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.18s ease, background 0.18s ease;
}

.theme-dark .theme-button,
.theme-dark .mini-chip,
.theme-dark .tab-button,
.theme-dark .secondary-button,
.theme-dark .account-item,
.theme-dark .metric-card,
.theme-dark .module-box {
  background: rgba(255, 255, 255, 0.04);
}

.theme-button:hover {
  transform: translateY(-1px);
}

.auth-head,
.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.auth-head h2,
.panel-head h2 {
  margin: 0;
  font-size: 28px;
}

.status-badge {
  background: rgba(14, 107, 105, 0.1);
  color: var(--primary);
}

.online-mark {
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
  background: rgba(255, 255, 255, 0.82);
  color: var(--muted);
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.tab-button:hover {
  transform: translateY(-1px);
}

.tab-button.active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ink), var(--primary-deep));
  color: #fff;
}

.message-bar {
  margin: 0 0 16px;
  padding: 14px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
}

.message-bar.info {
  background: rgba(23, 57, 77, 0.08);
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
  background: rgba(255, 255, 255, 0.94);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.theme-dark .form-grid input {
  background: rgba(255, 255, 255, 0.04);
  color: var(--ink);
}

.form-grid input:focus {
  outline: none;
  border-color: rgba(14, 107, 105, 0.45);
  box-shadow: 0 0 0 4px rgba(14, 107, 105, 0.08);
  transform: translateY(-1px);
}

.primary-button,
.secondary-button,
.danger-button {
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.primary-button:hover,
.secondary-button:hover,
.danger-button:hover {
  transform: translateY(-1px);
}

.primary-button:disabled,
.secondary-button:disabled,
.danger-button:disabled {
  opacity: 0.7;
  cursor: wait;
}

.primary-button {
  border: 0;
  color: #fff;
  background: linear-gradient(135deg, #18212a, #244559);
  box-shadow: 0 18px 34px rgba(26, 33, 40, 0.16);
}

.secondary-button {
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.88);
  color: var(--ink);
}

.danger-button {
  border: 0;
  color: #fff;
  background: linear-gradient(135deg, #8c2d23, #b42318);
}

.hint-text {
  margin: 18px 0 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.head-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.account-panel {
  display: grid;
  align-content: start;
  gap: 14px;
}

.account-item,
.metric-card,
.module-box {
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.8);
}

.account-item {
  padding: 18px;
}

.account-item span,
.metric-card span {
  color: var(--muted);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.account-item strong,
.metric-card strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  line-height: 1.5;
}

.metric-grid {
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

.module-box {
  margin-top: 16px;
  padding: 18px;
}

.module-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.module-tag {
  margin-bottom: 0;
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
  border: 1px solid var(--line);
  background: var(--paper-strong);
  font-weight: 700;
}

.theme-dark .module-chip {
  background: rgba(255, 255, 255, 0.06);
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

@media (max-width: 980px) {
  .auth-layout,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page {
    width: min(100% - 20px, 1120px);
    padding: 20px 0 38px;
  }

  .brand-panel,
  .auth-card,
  .dashboard-head,
  .panel {
    padding: 20px;
    border-radius: 24px;
  }

  .dashboard-head,
  .auth-head,
  .panel-head,
  .module-head,
  .brand-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .tab-row {
    grid-template-columns: 1fr;
  }

  .head-actions {
    width: 100%;
  }

  .secondary-button,
  .danger-button {
    width: 100%;
  }
}
</style>
