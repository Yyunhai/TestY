<template>
  <div class="auth-page">
    <!-- Animated background -->
    <div class="auth-bg">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
      <div class="grid-lines"></div>
    </div>

    <div class="auth-container">
      <!-- Left decorative panel -->
      <div class="auth-hero">
        <div class="hero-content">
          <div class="hero-brand">
            <div class="hero-logo">
              <img src="../assets/logo2.png" alt="TestY" class="hero-logo-img" />
            </div>
            <h2 class="hero-name">TestY</h2>
          </div>
          <h1 class="hero-title">
            <span class="title-line">高效协作</span>
            <span class="title-line">智慧管理</span>
          </h1>
          <p class="hero-desc">一站式文档管理与团队协作平台，让知识流转更高效</p>
          <div class="hero-features">
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>Markdown 实时编辑</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>精细化权限管理</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot"></div>
              <span>版本历史追溯</span>
            </div>
          </div>
        </div>
        <div class="hero-decoration">
          <div class="deco-ring deco-ring-1"></div>
          <div class="deco-ring deco-ring-2"></div>
          <div class="deco-ring deco-ring-3"></div>
        </div>
      </div>

      <!-- Right form panel -->
      <div class="auth-card">
        <div class="card-inner">
          <div class="auth-header">
            <h1 class="auth-title">{{ authTitle }}</h1>
            <p class="auth-subtitle">{{ authSubtitle }}</p>
          </div>

          <div class="auth-tabs">
            <button
              v-for="tab in authTabs"
              :key="tab.key"
              type="button"
              class="auth-tab"
              :class="{ active: mode === tab.key }"
              @click="$emit('update:mode', tab.key)"
            >
              <span class="tab-icon">{{ tab.icon }}</span>
              {{ tab.label }}
              <span v-if="mode === tab.key" class="tab-indicator"></span>
            </button>
          </div>

          <div v-if="message.text" class="auth-notice" :class="message.type">
            <span class="notice-icon">{{ message.type === 'error' ? '!' : message.type === 'success' ? '✓' : 'i' }}</span>
            {{ message.text }}
          </div>

          <form class="auth-form" @submit.prevent="$emit('submit')">
            <template v-if="mode === 'login'">
              <div class="form-group" style="--delay: 0">
                <label class="form-label">用户名或邮箱</label>
                <div class="input-wrapper">
                  <input
                    class="form-input"
                    :value="loginForm.usernameOrEmail"
                    @input="$emit('update:loginForm', { ...loginForm, usernameOrEmail: $event.target.value })"
                    type="text"
                    autocomplete="username"
                    placeholder="请输入用户名或邮箱"
                  />
                  <div class="input-glow"></div>
                </div>
              </div>
              <div class="form-group" style="--delay: 1">
                <label class="form-label">密码</label>
                <div class="input-wrapper">
                  <input
                    class="form-input has-toggle"
                    :value="loginForm.password"
                    @input="$emit('update:loginForm', { ...loginForm, password: $event.target.value })"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    placeholder="请输入密码"
                  />
                  <button type="button" class="password-toggle" @click="showPassword = !showPassword" tabindex="-1">
                    {{ showPassword ? '🙈' : '👁' }}
                  </button>
                  <div class="input-glow"></div>
                </div>
              </div>
              <label class="form-checkbox" style="--delay: 2">
                <input type="checkbox" :checked="rememberPassword" @change="$emit('update:rememberPassword', $event.target.checked)" />
                <span class="checkbox-mark"></span>
                <span class="checkbox-label">记住密码</span>
              </label>
            </template>

            <template v-else-if="mode === 'register'">
              <div class="form-row">
                <div class="form-group" style="--delay: 0">
                  <label class="form-label">用户名</label>
                  <div class="input-wrapper">
                    <input
                      class="form-input"
                      :value="registerForm.username"
                      @input="$emit('update:registerForm', { ...registerForm, username: $event.target.value })"
                      type="text"
                      autocomplete="username"
                      placeholder="请输入用户名"
                    />
                    <div class="input-glow"></div>
                  </div>
                </div>
                <div class="form-group" style="--delay: 1">
                  <label class="form-label">邮箱</label>
                  <div class="input-wrapper">
                    <input
                      class="form-input"
                      :value="registerForm.email"
                      @input="$emit('update:registerForm', { ...registerForm, email: $event.target.value })"
                      type="email"
                      autocomplete="email"
                      placeholder="请输入邮箱"
                    />
                    <div class="input-glow"></div>
                  </div>
                </div>
              </div>
              <div class="form-group" style="--delay: 2">
                <label class="form-label">密码</label>
                <div class="input-wrapper">
                  <input
                    class="form-input has-toggle"
                    :value="registerForm.password"
                    @input="$emit('update:registerForm', { ...registerForm, password: $event.target.value })"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder="请设置密码"
                  />
                  <button type="button" class="password-toggle" @click="showPassword = !showPassword" tabindex="-1">
                    {{ showPassword ? '🙈' : '👁' }}
                  </button>
                  <div class="input-glow"></div>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group" style="--delay: 3">
                  <label class="form-label">显示名称</label>
                  <div class="input-wrapper">
                    <input
                      class="form-input"
                      :value="registerForm.displayName"
                      @input="$emit('update:registerForm', { ...registerForm, displayName: $event.target.value })"
                      type="text"
                      placeholder="您的显示名称"
                    />
                    <div class="input-glow"></div>
                  </div>
                </div>
                <div class="form-group" style="--delay: 4">
                  <label class="form-label">手机号</label>
                  <div class="input-wrapper">
                    <input
                      class="form-input"
                      :value="registerForm.phoneNumber"
                      @input="$emit('update:registerForm', { ...registerForm, phoneNumber: $event.target.value })"
                      type="text"
                      placeholder="请输入手机号"
                    />
                    <div class="input-glow"></div>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="form-group" style="--delay: 0">
                <label class="form-label">用户名</label>
                <div class="input-wrapper">
                  <input
                    class="form-input"
                    :value="resetForm.username"
                    @input="$emit('update:resetForm', { ...resetForm, username: $event.target.value })"
                    type="text"
                    autocomplete="username"
                    placeholder="请输入用户名"
                  />
                  <div class="input-glow"></div>
                </div>
              </div>
              <div class="form-group" style="--delay: 1">
                <label class="form-label">邮箱</label>
                <div class="input-wrapper">
                  <input
                    class="form-input"
                    :value="resetForm.email"
                    @input="$emit('update:resetForm', { ...resetForm, email: $event.target.value })"
                    type="email"
                    autocomplete="email"
                    placeholder="请输入注册邮箱"
                  />
                  <div class="input-glow"></div>
                </div>
              </div>
              <div class="form-group" style="--delay: 2">
                <label class="form-label">新密码</label>
                <div class="input-wrapper">
                  <input
                    class="form-input has-toggle"
                    :value="resetForm.newPassword"
                    @input="$emit('update:resetForm', { ...resetForm, newPassword: $event.target.value })"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder="请设置新密码"
                  />
                  <button type="button" class="password-toggle" @click="showPassword = !showPassword" tabindex="-1">
                    {{ showPassword ? '🙈' : '👁' }}
                  </button>
                  <div class="input-glow"></div>
                </div>
              </div>
            </template>

            <button class="auth-submit" type="submit" :disabled="busy">
              <span v-if="busy" class="spinner"></span>
              <span class="btn-text">{{ busy ? '处理中...' : authButton }}</span>
              <span v-if="!busy" class="btn-arrow">→</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "AuthPage",
  props: {
    mode: { type: String, default: "login" },
    message: { type: Object, default: () => ({ type: "info", text: "" }) },
    loginForm: { type: Object, default: () => ({ usernameOrEmail: "", password: "" }) },
    registerForm: { type: Object, default: () => ({ username: "", email: "", password: "", displayName: "", phoneNumber: "" }) },
    resetForm: { type: Object, default: () => ({ username: "", email: "", newPassword: "" }) },
    rememberPassword: { type: Boolean, default: false },
    busy: { type: Boolean, default: false }
  },
  emits: ["update:mode", "update:loginForm", "update:registerForm", "update:resetForm", "update:rememberPassword", "submit"],
  data() {
    return { showPassword: false };
  },
  computed: {
    authTitle() {
      if (this.mode === "register") return "创建新账号";
      if (this.mode === "reset") return "重置密码";
      return "欢迎回来";
    },
    authSubtitle() {
      if (this.mode === "register") return "填写以下信息完成注册";
      if (this.mode === "reset") return "通过邮箱验证重置密码";
      return "登录您的 TestY 账号";
    },
    authButton() {
      if (this.mode === "register") return "创建账号";
      if (this.mode === "reset") return "重置密码";
      return "登录";
    },
    authTabs() {
      return [
        { key: "login", label: "登录", icon: "→" },
        { key: "register", label: "注册", icon: "+" },
        { key: "reset", label: "重置密码", icon: "↻" }
      ];
    }
  }
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
  position: relative;
  overflow: hidden;
}

/* ===== Animated Background ===== */
.auth-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(194, 65, 12, 0.25) 0%, transparent 70%);
  top: -10%;
  left: -5%;
  animation: float-1 18s ease-in-out infinite;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(186, 140, 60, 0.2) 0%, transparent 70%);
  top: 60%;
  right: -5%;
  animation: float-2 22s ease-in-out infinite;
}

.orb-3 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, rgba(92, 130, 104, 0.2) 0%, transparent 70%);
  bottom: -5%;
  left: 30%;
  animation: float-3 20s ease-in-out infinite;
}

.orb-4 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(194, 65, 12, 0.15) 0%, transparent 70%);
  top: 30%;
  left: 50%;
  animation: float-4 16s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(60px, 40px) scale(1.1); }
  66% { transform: translate(-30px, 80px) scale(0.95); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-50px, -60px) scale(1.05); }
  66% { transform: translate(40px, -30px) scale(0.9); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(70px, -40px) scale(1.08); }
  66% { transform: translate(-50px, -20px) scale(0.92); }
}

@keyframes float-4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-80px, 60px) scale(1.15); }
}

.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,0,0,0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.02) 1px, transparent 1px);
  background-size: 100px 100px;
  animation: grid-shift 30s linear infinite;
}

@keyframes grid-shift {
  to { transform: translate(60px, 60px); }
}

/* ===== Container ===== */
.auth-container {
  display: flex;
  width: min(1100px, 100%);
  min-height: 640px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0,0,0,0.12), 0 8px 24px rgba(0,0,0,0.06);
  position: relative;
  animation: container-enter 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes container-enter {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* ===== Left Hero Panel ===== */
.auth-hero {
  flex: 1.1;
  background: linear-gradient(135deg, #1a1612 0%, #2d2520 50%, #1a1612 100%);
  padding: 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 40px;
}

.hero-logo {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(194, 65, 12, 0.4);
  animation: logo-pulse 3s ease-in-out infinite;
}

.hero-logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

@keyframes logo-pulse {
  0%, 100% { box-shadow: 0 8px 24px rgba(194, 65, 12, 0.4); }
  50% { box-shadow: 0 8px 32px rgba(194, 65, 12, 0.6), 0 0 0 8px rgba(194, 65, 12, 0.08); }
}

.hero-name {
  color: #fff;
  font-size: 34px;
  font-weight: 700;
  letter-spacing: -0.5px;
  margin: 0;
}

.hero-title {
  margin: 0 0 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-line {
  display: block;
  font-size: 44px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
  line-height: 1.3;
  animation: title-slide 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.title-line:nth-child(2) {
  background: linear-gradient(135deg, #e8732a, #ba8c3c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation-delay: 0.1s;
}

@keyframes title-slide {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.hero-desc {
  color: rgba(255,255,255,0.55);
  font-size: 17px;
  line-height: 1.7;
  margin: 0 0 32px;
  animation: fade-in 0.6s 0.3s both;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.hero-features {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.7);
  font-size: 16px;
  font-weight: 500;
  animation: feature-enter 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.feature-item:nth-child(1) { animation-delay: 0.4s; }
.feature-item:nth-child(2) { animation-delay: 0.5s; }
.feature-item:nth-child(3) { animation-delay: 0.6s; }

@keyframes feature-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 10px rgba(194, 65, 12, 0.5);
  flex-shrink: 0;
}

/* Decorative rings */
.hero-decoration {
  position: absolute;
  top: 50%;
  right: -10%;
  transform: translateY(-50%);
  width: 400px;
  height: 400px;
}

.deco-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.06);
}

.deco-ring-1 {
  inset: 0;
  animation: ring-spin 25s linear infinite;
}

.deco-ring-2 {
  inset: 50px;
  border-color: rgba(194, 65, 12, 0.1);
  animation: ring-spin 20s linear infinite reverse;
}

.deco-ring-3 {
  inset: 100px;
  border-color: rgba(186, 140, 60, 0.08);
  animation: ring-spin 15s linear infinite;
}

@keyframes ring-spin {
  to { transform: rotate(360deg); }
}

/* ===== Right Form Card ===== */
.auth-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  position: relative;
}

.auth-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.5) 0%, rgba(255,255,255,0) 100%);
  pointer-events: none;
}

.card-inner {
  width: 100%;
  padding: 48px;
  position: relative;
  z-index: 1;
}

.auth-header {
  margin-bottom: 28px;
  animation: content-enter 0.5s 0.2s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes content-enter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.auth-title {
  margin: 0 0 8px;
  font-size: 34px;
  font-weight: 800;
  color: var(--text);
  letter-spacing: -0.5px;
}

.auth-subtitle {
  margin: 0;
  color: var(--text-muted);
  font-size: 16px;
}

/* Tabs */
.auth-tabs {
  display: flex;
  gap: 5px;
  padding: 4px;
  background: var(--bg-secondary);
  border-radius: 14px;
  margin-bottom: 24px;
  animation: content-enter 0.5s 0.3s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.auth-tab {
  flex: 1;
  padding: 12px 18px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--text-muted);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-icon {
  font-size: 16px;
  opacity: 0.6;
  transition: all 0.3s;
}

.auth-tab.active {
  background: var(--surface);
  color: var(--text);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.auth-tab.active .tab-icon {
  opacity: 1;
  color: var(--accent);
}

.auth-tab:hover:not(.active) {
  color: var(--text-secondary);
  background: rgba(255,255,255,0.5);
}

.tab-indicator {
  position: absolute;
  bottom: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 22px;
  height: 2.5px;
  background: var(--accent);
  border-radius: 1px;
  animation: indicator-in 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes indicator-in {
  from { width: 0; opacity: 0; }
  to { width: 20px; opacity: 1; }
}

/* Notice */
.auth-notice {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
  animation: notice-enter 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes notice-enter {
  from { opacity: 0; transform: translateY(-10px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.auth-notice.info {
  background: rgba(94, 120, 143, 0.1);
  color: #3d6b8e;
  border: 1px solid rgba(94, 120, 143, 0.15);
}

.auth-notice.success {
  background: rgba(92, 130, 104, 0.1);
  color: #3a6b4a;
  border: 1px solid rgba(92, 130, 104, 0.15);
}

.auth-notice.error {
  background: rgba(194, 65, 12, 0.08);
  color: #a83a10;
  border: 1px solid rgba(194, 65, 12, 0.12);
}

.notice-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.info .notice-icon { background: rgba(94, 120, 143, 0.15); }
.success .notice-icon { background: rgba(92, 130, 104, 0.15); }
.error .notice-icon { background: rgba(194, 65, 12, 0.12); }

/* Form */
.auth-form {
  display: grid;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-group {
  display: grid;
  gap: 7px;
  animation: form-field-enter 0.5s calc(0.4s + var(--delay, 0) * 0.06s) cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes form-field-enter {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}

.form-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-secondary);
  letter-spacing: 0.04em;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 14px 18px;
  border: 1.5px solid var(--border);
  border-radius: 12px;
  background: var(--bg);
  color: var(--text);
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  outline: none;
}

.form-input:focus {
  border-color: var(--accent);
  background: var(--surface);
  box-shadow: 0 0 0 4px rgba(194, 65, 12, 0.08);
  transform: translateY(-1px);
}

.form-input::placeholder {
  color: var(--text-light);
  font-size: 15px;
}

.input-glow {
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2.5px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
  border-radius: 1px;
  transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  pointer-events: none;
}

.form-input:focus ~ .input-glow {
  width: 100%;
}

.form-input.has-toggle {
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  line-height: 1;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.password-toggle:hover {
  opacity: 1;
}

/* Checkbox */
.form-checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  animation: form-field-enter 0.5s calc(0.4s + var(--delay, 0) * 0.06s) cubic-bezier(0.16, 1, 0.3, 1) both;
}

.form-checkbox input {
  display: none;
}

.checkbox-mark {
  width: 20px;
  height: 20px;
  border: 1.5px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.form-checkbox input:checked + .checkbox-mark {
  background: var(--accent);
  border-color: var(--accent);
  transform: scale(1.05);
}

.form-checkbox input:checked + .checkbox-mark::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 6px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  animation: check-in 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes check-in {
  from { transform: rotate(45deg) scale(0); }
  to { transform: rotate(45deg) scale(1); }
}

.checkbox-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Submit button */
.auth-submit {
  width: 100%;
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent), #e8732a);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 6px;
  position: relative;
  overflow: hidden;
  animation: form-field-enter 0.5s 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.auth-submit::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.15) 50%, transparent 100%);
  transform: translateX(-100%);
  transition: transform 0.5s;
}

.auth-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(194, 65, 12, 0.35);
}

.auth-submit:hover:not(:disabled)::before {
  transform: translateX(100%);
}

.auth-submit:active:not(:disabled) {
  transform: translateY(0);
}

.auth-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-arrow {
  font-size: 20px;
  transition: transform 0.3s;
}

.auth-submit:hover:not(:disabled) .btn-arrow {
  transform: translateX(4px);
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .auth-container {
    flex-direction: column;
    min-height: auto;
    width: min(560px, 100%);
  }

  .auth-hero {
    padding: 40px 36px;
    min-height: auto;
  }

  .hero-title .title-line {
    font-size: 32px;
  }

  .hero-desc {
    font-size: 15px;
    margin-bottom: 24px;
  }

  .hero-decoration {
    display: none;
  }

  .card-inner {
    padding: 40px 36px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .auth-page {
    padding: 12px;
  }

  .auth-hero {
    padding: 28px 24px;
  }

  .card-inner {
    padding: 28px 24px;
  }

  .auth-title {
    font-size: 26px;
  }

  .form-input {
    font-size: 15px;
    padding: 12px 16px;
  }
}
</style>
