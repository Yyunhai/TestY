<template>
  <div class="admin-page" ref="adminPage">
    <div class="admin-tabs" ref="adminTabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="admin-tab"
        :class="{ active: section === tab.key }"
        @click="$emit('change-section', tab.key)"
      >
        <span class="tab-icon" v-html="tab.icon"></span>
        {{ tab.label }}
      </button>
      <button class="btn-ghost ml-auto" type="button" @click="$emit('refresh')">刷新数据</button>
    </div>

    <div v-if="section === 'overview'" class="admin-content" ref="adminContent">
      <div v-if="errors.overview" class="notice error">{{ errors.overview }}</div>

      <div class="stats-grid" ref="statsGrid">
        <div class="stat-card" v-for="(stat, i) in overviewStats" :key="i" :ref="el => setStatRef(el, i)">
          <span class="stat-label">{{ stat.label }}</span>
          <span class="stat-value">{{ stat.value }}</span>
        </div>
      </div>

      <div class="overview-grid">
        <div class="card" ref="alertCard">
          <div class="card-header">
            <p class="card-eyebrow">风险提醒</p>
            <h3 class="card-title">异常登录主体</h3>
          </div>
          <div class="alert-list">
            <div v-for="p in latestSuspicious" :key="p.principal" class="alert-item">
              <strong class="alert-name">{{ p.principal || '未知主体' }}</strong>
              <span class="alert-meta">24h 失败 {{ p.failureCount }} 次 · IP {{ p.latestRemoteIp || '--' }}</span>
            </div>
            <p v-if="!latestSuspicious.length" class="empty-text">当前没有高频失败登录主体</p>
          </div>
        </div>
        <div class="card" ref="permCard">
          <div class="card-header">
            <p class="card-eyebrow">权限概览</p>
            <h3 class="card-title">当前后台能力</h3>
          </div>
          <div class="chip-grid">
            <span v-for="perm in adminPermissions" :key="perm" class="chip">{{ perm }}</span>
            <p v-if="!adminPermissions.length" class="empty-text">当前账号没有后台权限</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="section === 'users'" class="admin-content" ref="adminContent">
      <div v-if="errors.users" class="notice error">{{ errors.users }}</div>

      <div class="section-toolbar">
        <div class="filter-bar">
          <input
            class="filter-input"
            type="text"
            placeholder="搜索用户、邮箱或角色..."
            :value="userFilters.keyword"
            @input="$emit('update:userFilters', { ...userFilters, keyword: $event.target.value })"
          />
          <select
            class="filter-select"
            :value="userFilters.status"
            @change="$emit('update:userFilters', { ...userFilters, status: $event.target.value })"
          >
            <option value="">全部状态</option>
            <option v-for="s in accountStatusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <button
          v-if="canCreateUsers"
          class="btn-primary"
          type="button"
          @click="$emit('update:showCreateUserDialog', true)"
        >
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1V13M1 7H13" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          创建用户
        </button>
      </div>

      <div class="user-list">
        <div v-for="(adminUser, i) in filteredUsers" :key="adminUser.id" class="card user-card" :ref="el => setUserCardRef(el, i)">
          <div class="card-header">
            <div class="user-header-info">
              <div class="user-avatar">{{ (adminUser.displayName || adminUser.username || '?').charAt(0).toUpperCase() }}</div>
              <div>
                <h3 class="card-title">{{ adminUser.displayName || adminUser.username }}</h3>
                <p class="card-subtitle">{{ adminUser.username }} · {{ adminUser.email || '--' }}</p>
              </div>
            </div>
            <span class="status-badge" :class="String(adminUser.accountStatus || '').toLowerCase()">
              {{ adminUser.accountStatus || 'UNKNOWN' }}
            </span>
          </div>

          <div class="user-meta">
            <span>最近登录：{{ formatDateTime(adminUser.lastLoginAt) }}</span>
            <span>IP：{{ adminUser.lastLoginIp || '--' }}</span>
          </div>

          <div class="chip-grid">
            <span v-for="roleCode in adminUser.roles || []" :key="`${adminUser.id}-${roleCode}`" class="chip">{{ roleCode }}</span>
            <span v-if="!(adminUser.roles || []).length" class="empty-text">无角色</span>
          </div>

          <p class="user-perms">权限：{{ (adminUser.permissions || []).join(', ') || '暂无权限' }}</p>

          <div class="user-controls">
            <div class="control-group">
              <label class="control-label">角色分配</label>
              <select
                class="control-select"
                multiple
                size="4"
                :value="userRoleSelections[adminUser.id] || []"
                @change="onUserRoleChange(adminUser.id, $event)"
                :disabled="!canWriteUsers || !canReadRoles"
              >
                <option v-for="role in roles" :key="role.id" :value="role.id">{{ role.name }} ({{ role.code }})</option>
              </select>
            </div>
            <div class="control-group">
              <label class="control-label">账户状态</label>
              <select
                class="control-select"
                :value="userStatusSelections[adminUser.id] || 'ACTIVE'"
                @change="onUserStatusChange(adminUser.id, $event)"
                :disabled="!canWriteUsers"
              >
                <option v-for="s in accountStatusOptions" :key="s" :value="s">{{ s }}</option>
              </select>
            </div>
          </div>

          <div class="card-actions">
            <button class="btn-ghost" type="button" :disabled="!canWriteUsers || !canReadRoles" @click="$emit('save-user-roles', adminUser)">保存角色</button>
            <button class="btn-ghost" type="button" :disabled="!canWriteUsers" @click="$emit('save-user-status', adminUser)">保存状态</button>
            <button
              v-if="canResetUserPasswords"
              class="btn-ghost"
              type="button"
              @click="$emit('reset-user-password', adminUser)"
            >重置密码</button>
            <button
              v-if="canDeleteUsers"
              class="btn-ghost btn-danger"
              type="button"
              @click="$emit('delete-user', adminUser.id)"
            >删除用户</button>
          </div>
        </div>
        <p v-if="!filteredUsers.length" class="empty-text">没有符合筛选条件的用户</p>
      </div>
    </div>

    <div v-else-if="section === 'roles'" class="admin-content roles-layout" ref="adminContent">
      <div v-if="errors.roles" class="notice error">{{ errors.roles }}</div>

      <div class="roles-list" v-if="canReadRoles">
        <div v-for="(role, i) in roles" :key="role.id" class="card role-card" :ref="el => setRoleCardRef(el, i)">
          <div class="card-header">
            <div>
              <h3 class="card-title">{{ role.name }}</h3>
              <p class="card-subtitle">{{ role.code }}</p>
            </div>
            <div class="role-actions">
              <button class="btn-ghost" type="button" :disabled="role.builtIn || !canWriteRoles || !canReadPermissions" @click="$emit('edit-role', role)">编辑</button>
              <button
                v-if="canDeleteRoles && !role.builtIn"
                class="btn-ghost btn-danger"
                type="button"
                @click="$emit('delete-role', role.id)"
              >删除</button>
            </div>
          </div>
          <p class="role-desc">{{ role.description || '暂无描述' }}</p>
          <div class="chip-grid">
            <span v-for="perm in role.permissions || []" :key="`${role.id}-${perm}`" class="chip">{{ perm }}</span>
            <span v-if="!(role.permissions || []).length" class="empty-text">暂无权限</span>
          </div>
        </div>
        <p v-if="!roles.length" class="empty-text">暂无可展示的角色</p>
      </div>

      <div class="roles-editor">
        <div class="card" v-if="canWriteRoles && canReadPermissions" ref="roleFormCard">
          <div class="card-header">
            <h3 class="card-title">{{ roleForm.id ? '编辑角色' : '新建角色' }}</h3>
            <button class="btn-ghost" type="button" @click="$emit('reset-role-form')">清空</button>
          </div>
          <form class="role-form" @submit.prevent="$emit('submit-role')">
            <div class="form-group">
              <label class="form-label">角色编码</label>
              <input class="form-input" type="text" :value="roleForm.code" @input="onRoleFormChange('code', $event.target.value)" :disabled="Boolean(roleForm.id)" />
            </div>
            <div class="form-group">
              <label class="form-label">角色名称</label>
              <input class="form-input" type="text" :value="roleForm.name" @input="onRoleFormChange('name', $event.target.value)" />
            </div>
            <div class="form-group">
              <label class="form-label">角色描述</label>
              <textarea class="form-textarea" rows="3" :value="roleForm.description" @input="onRoleFormChange('description', $event.target.value)"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">权限筛选</label>
              <input class="form-input" type="text" placeholder="按编码或名称筛选" :value="permissionKeyword" @input="$emit('update:permissionKeyword', $event.target.value)" />
            </div>
            <div class="permission-list">
              <label v-for="perm in filteredPermissions" :key="perm.id" class="perm-check">
                <input type="checkbox" :value="perm.id" :checked="(roleForm.permissionIds || []).includes(perm.id)" @change="onPermissionToggle(perm.id)" />
                <span class="perm-code">{{ perm.code }}</span>
              </label>
            </div>
            <button class="btn-primary" type="submit">{{ roleForm.id ? '保存角色' : '创建角色' }}</button>
          </form>
        </div>
        <div v-else-if="canReadRoles" class="card">
          <h3 class="card-title">角色编辑</h3>
          <p class="empty-text">当前账号没有角色写入权限，暂时只能查看角色信息。</p>
        </div>

        <div class="card perm-catalog" v-if="canReadPermissions" ref="permCatalog">
          <div class="card-header">
            <div>
              <p class="card-eyebrow">权限目录</p>
              <h3 class="card-title">系统权限列表</h3>
            </div>
            <span class="perm-count">{{ permissions.length }} 项</span>
          </div>
          <div class="perm-list">
            <div v-for="perm in filteredPermissions" :key="perm.id" class="perm-item">
              <strong class="perm-item-code">{{ perm.code }}</strong>
              <span class="perm-item-name">{{ perm.name || '未命名权限' }}</span>
              <span class="perm-item-desc">{{ perm.description || '' }}</span>
            </div>
            <p v-if="!filteredPermissions.length" class="empty-text">没有匹配的权限</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="section === 'audits'" class="admin-content" ref="adminContent">
      <div v-if="errors.audits" class="notice error">{{ errors.audits }}</div>

      <div class="card" v-if="latestSuspicious.length" ref="auditAlertCard">
        <div class="card-header">
          <p class="card-eyebrow">告警总览</p>
          <h3 class="card-title">异常登录提醒</h3>
        </div>
        <div class="alert-list">
          <div v-for="p in latestSuspicious" :key="`audit-${p.principal}`" class="alert-item">
            <strong class="alert-name">{{ p.principal || '未知主体' }}</strong>
            <span class="alert-meta">24h 失败 {{ p.failureCount }} 次 · {{ formatDateTime(p.latestFailureAt) }}</span>
          </div>
        </div>
      </div>

      <div class="filter-bar filter-wide">
        <input class="filter-input" type="text" placeholder="登录主体" :value="auditFilters.principal" @input="onAuditFilterChange('principal', $event.target.value)" />
        <select class="filter-select" :value="auditFilters.success" @change="onAuditFilterChange('success', $event.target.value)">
          <option value="">全部结果</option>
          <option value="true">成功</option>
          <option value="false">失败</option>
        </select>
        <input class="filter-input" type="text" placeholder="IP 地址" :value="auditFilters.remoteIp" @input="onAuditFilterChange('remoteIp', $event.target.value)" />
        <select class="filter-select" :value="auditFilters.size" @change="onAuditFilterChange('size', Number($event.target.value))">
          <option :value="10">10 条</option>
          <option :value="20">20 条</option>
          <option :value="50">50 条</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn-ghost" type="button" @click="$emit('load-audits', 0)">应用筛选</button>
        <button class="btn-ghost" type="button" @click="$emit('reset-audit-filters')">重置</button>
      </div>

      <div class="audit-list">
        <div v-for="(audit, i) in auditPage.content" :key="audit.id" class="card audit-card" :ref="el => setAuditCardRef(el, i)">
          <div class="card-header">
            <div>
              <h3 class="card-title">{{ audit.principal || '未知主体' }}</h3>
              <p class="card-subtitle">{{ formatDateTime(audit.loggedInAt) }} · {{ audit.remoteIp || '--' }}</p>
            </div>
            <span class="status-badge" :class="audit.success ? 'active' : 'disabled'">
              {{ audit.success ? '登录成功' : '登录失败' }}
            </span>
          </div>
          <p class="audit-detail">{{ audit.message || '无附加信息' }}</p>
          <p class="audit-detail">角色：{{ (audit.roles || []).join(', ') || '无' }}</p>
          <p class="audit-detail">权限：{{ (audit.permissions || []).join(', ') || '无' }}</p>
        </div>
        <p v-if="!auditPage.content.length" class="empty-text">没有符合筛选条件的登录审计记录</p>
      </div>

      <div class="pagination" v-if="auditPage.totalPages > 1">
        <button class="btn-ghost" type="button" :disabled="auditPage.number <= 0" @click="$emit('load-audits', auditPage.number - 1)">上一页</button>
        <span class="page-info">{{ (auditPage.number || 0) + 1 }} / {{ auditPage.totalPages || 1 }}</span>
        <button class="btn-ghost" type="button" :disabled="(auditPage.number || 0) + 1 >= (auditPage.totalPages || 1)" @click="$emit('load-audits', (auditPage.number || 0) + 1)">下一页</button>
      </div>
    </div>

    <div v-else-if="section === 'operations'" class="admin-content" ref="adminContent">
      <div v-if="errors.operations" class="notice error">{{ errors.operations }}</div>

      <div class="filter-bar filter-wide">
        <input class="filter-input" type="text" placeholder="模块编码" :value="operationFilters.module" @input="onOperationFilterChange('module', $event.target.value)" />
        <input class="filter-input" type="text" placeholder="动作编码" :value="operationFilters.action" @input="onOperationFilterChange('action', $event.target.value)" />
        <input class="filter-input" type="text" placeholder="操作者用户名" :value="operationFilters.operatorUsername" @input="onOperationFilterChange('operatorUsername', $event.target.value)" />
        <select class="filter-select" :value="operationFilters.success" @change="onOperationFilterChange('success', $event.target.value)">
          <option value="">全部结果</option>
          <option value="true">成功</option>
          <option value="false">失败</option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn-ghost" type="button" @click="$emit('load-operations', 0)">应用筛选</button>
        <button class="btn-ghost" type="button" @click="$emit('reset-operation-filters')">重置</button>
      </div>

      <div class="audit-list">
        <div v-for="(log, i) in operationPage.content" :key="log.id" class="card audit-card" :ref="el => setOpsCardRef(el, i)">
          <div class="card-header">
            <div>
              <h3 class="card-title">{{ log.action || 'UNKNOWN_ACTION' }}</h3>
              <p class="card-subtitle">{{ formatDateTime(log.occurredAt) }} · 模块 {{ log.module || '--' }} · 操作者 {{ log.operatorUsername || '--' }}</p>
            </div>
            <span class="status-badge" :class="log.success ? 'active' : 'disabled'">
              {{ log.success ? '执行成功' : '执行失败' }}
            </span>
          </div>
          <p class="audit-detail">目标：{{ log.targetType || '--' }} / {{ log.targetId || '--' }} · IP：{{ log.remoteIp || '--' }}</p>
          <p class="audit-detail">{{ log.message || '无描述' }}</p>
          <p v-if="log.detail" class="audit-detail">详情：{{ log.detail }}</p>
        </div>
        <p v-if="!operationPage.content.length" class="empty-text">没有符合筛选条件的操作日志</p>
      </div>

      <div class="pagination" v-if="operationPage.totalPages > 1">
        <button class="btn-ghost" type="button" :disabled="operationPage.number <= 0" @click="$emit('load-operations', operationPage.number - 1)">上一页</button>
        <span class="page-info">{{ (operationPage.number || 0) + 1 }} / {{ operationPage.totalPages || 1 }}</span>
        <button class="btn-ghost" type="button" :disabled="(operationPage.number || 0) + 1 >= (operationPage.totalPages || 1)" @click="$emit('load-operations', (operationPage.number || 0) + 1)">下一页</button>
      </div>
    </div>

    <teleport to="body">
      <div v-if="showCreateUserDialog" class="dialog-overlay" @click.self="$emit('update:showCreateUserDialog', false)">
        <div class="dialog">
          <div class="dialog-header">
            <h3 class="dialog-title">创建用户</h3>
            <button class="dialog-close" type="button" @click="$emit('update:showCreateUserDialog', false)">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 4L14 14M14 4L4 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
            </button>
          </div>
          <form class="dialog-body" @submit.prevent="$emit('create-user')">
            <div class="form-group">
              <label class="form-label">用户名 <span class="required">*</span></label>
              <input class="form-input" type="text" minlength="6" maxlength="64" placeholder="至少6个字符" required
                :value="createUserForm.username"
                @input="onCreateFormChange('username', $event.target.value)" />
            </div>
            <div class="form-group">
              <label class="form-label">邮箱 <span class="required">*</span></label>
              <input class="form-input" type="email" maxlength="128" placeholder="user@example.com" required
                :value="createUserForm.email"
                @input="onCreateFormChange('email', $event.target.value)" />
            </div>
            <div class="form-group">
              <label class="form-label">密码 <span class="required">*</span></label>
              <input class="form-input" type="password" minlength="8" maxlength="128" placeholder="至少8位，含大小写字母、数字和特殊字符" required
                :value="createUserForm.password"
                @input="onCreateFormChange('password', $event.target.value)" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">显示名</label>
                <input class="form-input" type="text" maxlength="100" placeholder="可选"
                  :value="createUserForm.displayName"
                  @input="onCreateFormChange('displayName', $event.target.value)" />
              </div>
              <div class="form-group">
                <label class="form-label">手机号</label>
                <input class="form-input" type="text" maxlength="32" placeholder="可选"
                  :value="createUserForm.phoneNumber"
                  @input="onCreateFormChange('phoneNumber', $event.target.value)" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">角色分配</label>
              <div class="permission-list">
                <label v-for="role in roles" :key="role.id" class="perm-check">
                  <input type="checkbox" :value="role.id"
                    :checked="(createUserForm.roleIds || []).includes(role.id)"
                    @change="onCreateRoleToggle(role.id)" />
                  <span class="perm-code">{{ role.name }} ({{ role.code }})</span>
                </label>
              </div>
              <p class="form-hint">不选择则默认分配 USER 角色</p>
            </div>
            <div class="dialog-footer">
              <button class="btn-ghost" type="button" @click="$emit('update:showCreateUserDialog', false)">取消</button>
              <button class="btn-primary" type="submit">创建</button>
            </div>
          </form>
        </div>
      </div>
    </teleport>

    <teleport to="body">
      <div v-if="showResetPasswordDialog" class="dialog-overlay" @click.self="$emit('update:showResetPasswordDialog', false)">
        <div class="dialog dialog-sm">
          <div class="dialog-header">
            <h3 class="dialog-title">重置密码</h3>
            <button class="dialog-close" type="button" @click="$emit('update:showResetPasswordDialog', false)">
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 4L14 14M14 4L4 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
            </button>
          </div>
          <form class="dialog-body" @submit.prevent="$emit('submit-reset-password')">
            <p class="dialog-desc">为用户 <strong>{{ resetPasswordTargetName }}</strong> 设置新密码</p>
            <div class="form-group">
              <label class="form-label">新密码 <span class="required">*</span></label>
              <input class="form-input" type="password" minlength="8" maxlength="128" placeholder="至少8位，含大小写字母、数字和特殊字符" required
                :value="resetPasswordForm.newPassword"
                @input="$emit('update:resetPasswordForm', { ...resetPasswordForm, newPassword: $event.target.value })" />
            </div>
            <div class="dialog-footer">
              <button class="btn-ghost" type="button" @click="$emit('update:showResetPasswordDialog', false)">取消</button>
              <button class="btn-primary" type="submit">确认重置</button>
            </div>
          </form>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script>
import { gsap } from "gsap";

export default {
  name: "AdminPage",
  props: {
    section: { type: String, default: "overview" },
    user: { type: Object, default: () => ({}) },
    users: { type: Array, default: () => [] },
    roles: { type: Array, default: () => [] },
    permissions: { type: Array, default: () => [] },
    errors: { type: Object, default: () => ({}) },
    auditAlerts: { type: Object, default: () => ({ failedAttemptsLast24Hours: 0, suspiciousPrincipals: [] }) },
    auditPage: { type: Object, default: () => ({ content: [], number: 0, totalPages: 1 }) },
    operationPage: { type: Object, default: () => ({ content: [], number: 0, totalPages: 1 }) },
    userFilters: { type: Object, default: () => ({ keyword: "", status: "" }) },
    userRoleSelections: { type: Object, default: () => ({}) },
    userStatusSelections: { type: Object, default: () => ({}) },
    roleForm: { type: Object, default: () => ({ id: null, code: "", name: "", description: "", permissionIds: [] }) },
    permissionKeyword: { type: String, default: "" },
    auditFilters: { type: Object, default: () => ({ principal: "", success: "", remoteIp: "", size: 10 }) },
    operationFilters: { type: Object, default: () => ({ module: "", action: "", operatorUsername: "", success: "", size: 10 }) },
    accountStatusOptions: { type: Array, default: () => ["ACTIVE", "LOCKED", "DISABLED"] },
    canReadUsers: { type: Boolean, default: false },
    canWriteUsers: { type: Boolean, default: false },
    canCreateUsers: { type: Boolean, default: false },
    canDeleteUsers: { type: Boolean, default: false },
    canResetUserPasswords: { type: Boolean, default: false },
    canReadRoles: { type: Boolean, default: false },
    canWriteRoles: { type: Boolean, default: false },
    canDeleteRoles: { type: Boolean, default: false },
    canReadPermissions: { type: Boolean, default: false },
    canReadLogins: { type: Boolean, default: false },
    canReadOperationLogs: { type: Boolean, default: false },
    createUserForm: { type: Object, default: () => ({ username: "", email: "", password: "", displayName: "", phoneNumber: "", roleIds: [] }) },
    resetPasswordForm: { type: Object, default: () => ({ userId: null, newPassword: "" }) },
    showCreateUserDialog: { type: Boolean, default: false },
    showResetPasswordDialog: { type: Boolean, default: false }
  },
  emits: [
    "change-section", "refresh",
    "update:userFilters", "save-user-roles", "save-user-status",
    "create-user", "delete-user", "reset-user-password", "submit-reset-password",
    "edit-role", "reset-role-form", "submit-role", "delete-role",
    "update:permissionKeyword", "update:roleForm",
    "update:auditFilters", "load-audits", "reset-audit-filters",
    "update:operationFilters", "load-operations", "reset-operation-filters",
    "update:createUserForm", "update:resetPasswordForm",
    "update:showCreateUserDialog", "update:showResetPasswordDialog",
    "update:userRoleSelections", "update:userStatusSelections"
  ],
  data() {
    return {
      statRefs: [],
      userCardRefs: [],
      roleCardRefs: [],
      auditCardRefs: [],
      opsCardRefs: []
    };
  },
  computed: {
    tabs() {
      const tabs = [];
      const iconOverview = '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 1L1 5V13L9 17L17 13V5L9 1Z" stroke="currentColor" stroke-width="1.3"/></svg>';
      const iconUsers = '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="7" cy="5" r="3" stroke="currentColor" stroke-width="1.3"/><path d="M1 16C1 12.6863 3.68629 10 7 10C10.3137 10 13 12.6863 13 16" stroke="currentColor" stroke-width="1.3"/><path d="M15 7V13M18 10H12" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>';
      const iconRoles = '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 1L2 5V10C2 14.4183 5.03 17.72 9 18.5C12.97 17.72 16 14.4183 16 10V5L9 1Z" stroke="currentColor" stroke-width="1.3"/></svg>';
      const iconAudit = '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M3 3H15V15H3V3Z" stroke="currentColor" stroke-width="1.3"/><path d="M7 7L11 11M7 11L11 7" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>';
      const iconOps = '<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 1V17M1 9H17" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>';

      tabs.push({ key: "overview", label: "安全态势", icon: iconOverview });
      if (this.canReadUsers) tabs.push({ key: "users", label: "用户管理", icon: iconUsers });
      if (this.canReadRoles || this.canReadPermissions) tabs.push({ key: "roles", label: "角色权限", icon: iconRoles });
      if (this.canReadLogins) tabs.push({ key: "audits", label: "登录审计", icon: iconAudit });
      if (this.canReadOperationLogs) tabs.push({ key: "operations", label: "操作日志", icon: iconOps });
      return tabs;
    },
    overviewStats() {
      return [
        { label: "用户数", value: this.canReadUsers ? this.users.length : '--' },
        { label: "角色数", value: this.canReadRoles ? this.roles.length : '--' },
        { label: "权限数", value: this.canReadPermissions ? this.permissions.length : '--' },
        { label: "24h 失败登录", value: this.canReadLogins ? this.auditAlerts.failedAttemptsLast24Hours : '--' }
      ];
    },
    latestSuspicious() {
      return (this.auditAlerts.suspiciousPrincipals || []).slice(0, 5);
    },
    adminPermissions() {
      return (this.user.permissions || []).filter((item) => item.startsWith("admin:"));
    },
    filteredUsers() {
      const keyword = (this.userFilters.keyword || "").toLowerCase();
      const status = this.userFilters.status;
      return this.users.filter((item) => {
        const hit = !keyword || [item.username, item.email, item.displayName, ...(item.roles || [])].some((v) => String(v || "").toLowerCase().includes(keyword));
        return hit && (!status || item.accountStatus === status);
      });
    },
    filteredPermissions() {
      const keyword = (this.permissionKeyword || "").toLowerCase();
      return this.permissions.filter((item) =>
        !keyword || [item.code, item.name, item.description].some((v) => String(v || "").toLowerCase().includes(keyword))
      );
    },
    resetPasswordTargetName() {
      const uid = this.resetPasswordForm.userId;
      if (!uid) return "";
      const u = this.users.find((item) => item.id === uid);
      return u ? (u.displayName || u.username) : "";
    }
  },
  mounted() {
    this.$nextTick(() => this.animateAdmin());
  },
  methods: {
    setStatRef(el, i) { if (el) this.statRefs[i] = el; },
    setUserCardRef(el, i) { if (el) this.userCardRefs[i] = el; },
    setRoleCardRef(el, i) { if (el) this.roleCardRefs[i] = el; },
    setAuditCardRef(el, i) { if (el) this.auditCardRefs[i] = el; },
    setOpsCardRef(el, i) { if (el) this.opsCardRefs[i] = el; },
    animateAdmin() {
      const tl = gsap.timeline({ defaults: { ease: "power2.out" } });

      tl.fromTo(this.$refs.adminTabs, { opacity: 0, y: -10 }, { opacity: 1, y: 0, duration: 0.35, clearProps: "transform" });

      const stats = this.statRefs.filter(Boolean);
      if (stats.length) {
        tl.fromTo(stats, { opacity: 0, y: 20, scale: 0.97 }, { opacity: 1, y: 0, scale: 1, stagger: 0.08, duration: 0.35, clearProps: "transform" }, "-=0.2");
      }

      const overviewCards = [this.$refs.alertCard, this.$refs.permCard, this.$refs.auditAlertCard, this.$refs.roleFormCard, this.$refs.permCatalog].filter(Boolean);
      if (overviewCards.length) {
        tl.fromTo(overviewCards, { opacity: 0, y: 15 }, { opacity: 1, y: 0, stagger: 0.1, duration: 0.3, clearProps: "transform" }, "-=0.2");
      }

      const userCards = this.userCardRefs.filter(Boolean);
      if (userCards.length) {
        tl.fromTo(userCards, { opacity: 0, y: 12 }, { opacity: 1, y: 0, stagger: 0.06, duration: 0.3, clearProps: "transform" }, "-=0.15");
      }

      const roleCards = this.roleCardRefs.filter(Boolean);
      if (roleCards.length) {
        tl.fromTo(roleCards, { opacity: 0, y: 12 }, { opacity: 1, y: 0, stagger: 0.06, duration: 0.3, clearProps: "transform" }, "-=0.15");
      }

      const auditCards = this.auditCardRefs.filter(Boolean);
      if (auditCards.length) {
        tl.fromTo(auditCards, { opacity: 0, y: 12 }, { opacity: 1, y: 0, stagger: 0.05, duration: 0.3, clearProps: "transform" }, "-=0.15");
      }

      const opsCards = this.opsCardRefs.filter(Boolean);
      if (opsCards.length) {
        tl.fromTo(opsCards, { opacity: 0, y: 12 }, { opacity: 1, y: 0, stagger: 0.05, duration: 0.3, clearProps: "transform" }, "-=0.15");
      }
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
    },
    onUserRoleChange(userId, event) {
      const selected = Array.from(event.target.selectedOptions).map((o) => Number(o.value));
      this.$emit("update:userRoleSelections", { ...this.userRoleSelections, [userId]: selected });
    },
    onUserStatusChange(userId, event) {
      this.$emit("update:userStatusSelections", { ...this.userStatusSelections, [userId]: event.target.value });
    },
    onRoleFormChange(field, value) {
      this.$emit("update:roleForm", { ...this.roleForm, [field]: value });
    },
    onPermissionToggle(permId) {
      const ids = [...(this.roleForm.permissionIds || [])];
      const idx = ids.indexOf(permId);
      if (idx >= 0) ids.splice(idx, 1);
      else ids.push(permId);
      this.$emit("update:roleForm", { ...this.roleForm, permissionIds: ids });
    },
    onAuditFilterChange(field, value) {
      this.$emit("update:auditFilters", { ...this.auditFilters, [field]: value });
    },
    onOperationFilterChange(field, value) {
      this.$emit("update:operationFilters", { ...this.operationFilters, [field]: value });
    },
    onCreateFormChange(field, value) {
      this.$emit("update:createUserForm", { ...this.createUserForm, [field]: value });
    },
    onCreateRoleToggle(roleId) {
      const ids = [...(this.createUserForm.roleIds || [])];
      const idx = ids.indexOf(roleId);
      if (idx >= 0) ids.splice(idx, 1);
      else ids.push(roleId);
      this.$emit("update:createUserForm", { ...this.createUserForm, roleIds: ids });
    }
  }
};
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 20px;
}

.admin-tabs {
  display: flex;
  gap: 6px;
  padding: 6px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.ml-auto { margin-left: auto; }

.admin-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.admin-tab:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.admin-tab.active {
  background: var(--accent-bg);
  color: var(--accent);
  position: relative;
}

.admin-tab.active::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  border-radius: 3px;
  background: var(--accent);
  animation: tabIndicator 0.3s ease;
}

@keyframes tabIndicator {
  from { width: 0; opacity: 0; }
  to { width: 20px; opacity: 1; }
}

.tab-icon {
  display: flex;
  align-items: center;
}

.admin-content {
  display: grid;
  gap: 18px;
}

.section-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.section-toolbar .filter-bar {
  flex: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px;
  display: grid;
  gap: 8px;
  position: relative;
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--accent);
  border-radius: 4px 0 0 4px;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card:nth-child(1)::before { background: #3b82f6; }
.stat-card:nth-child(2)::before { background: #f59e0b; }
.stat-card:nth-child(3)::before { background: #10b981; }
.stat-card:nth-child(4)::before { background: #ef4444; }

.stat-card .stat-label {
  font-size: 13px;
  color: var(--text-muted);
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.5px;
  font-variant-numeric: tabular-nums;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 22px;
  display: grid;
  gap: 14px;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.card-subtitle {
  margin: 2px 0 0;
  font-size: 13px;
  color: var(--text-muted);
}

.alert-list {
  display: grid;
  gap: 8px;
}

.alert-item {
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: 12px;
  display: grid;
  gap: 2px;
  transition: border-color 0.2s ease;
}

.alert-item:hover {
  border-color: var(--danger);
  background: var(--danger-bg);
}

.alert-name {
  font-size: 14px;
  color: var(--text);
}

.alert-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.chip-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 5px 12px;
  border-radius: 8px;
  background: var(--accent-bg);
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
  transition: transform 0.15s ease;
}

.chip:hover {
  transform: scale(1.05);
}

.filter-bar {
  display: grid;
  grid-template-columns: 1fr 160px;
  gap: 10px;
  flex: 1;
}

.filter-wide {
  grid-template-columns: repeat(4, 1fr);
}

.filter-input, .filter-select {
  padding: 10px 14px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  color: var(--text);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.filter-input:focus, .filter-select:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.1);
}

.filter-input::placeholder {
  color: var(--text-light);
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.user-list {
  display: grid;
  gap: 14px;
}

.user-card {
  gap: 12px;
}

.user-header-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent), #e85d26);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(194, 65, 12, 0.2);
}

.user-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.user-perms {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
}

.user-controls {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
}

.control-group {
  display: grid;
  gap: 6px;
}

.control-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.control-select {
  width: 100%;
  padding: 8px 10px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  color: var(--text);
  font-size: 13px;
  outline: none;
}

.control-select:focus {
  border-color: var(--accent);
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.status-badge::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-badge.active::before,
.status-badge.success::before {
  background: var(--success);
  box-shadow: 0 0 6px rgba(92, 130, 104, 0.4);
}

.status-badge.disabled::before,
.status-badge.locked::before {
  background: var(--danger);
  box-shadow: 0 0 6px rgba(194, 65, 12, 0.3);
}

.status-badge.active, .status-badge.success {
  background: rgba(92, 130, 104, 0.1);
  color: var(--success);
}

.status-badge.disabled, .status-badge.locked {
  background: var(--danger-bg);
  color: var(--danger);
}

.roles-layout {
  grid-template-columns: 1fr 1fr;
}

.roles-list {
  display: grid;
  gap: 14px;
  align-content: start;
}

.role-card .role-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
}

.role-actions {
  display: flex;
  gap: 6px;
}

.roles-editor {
  display: grid;
  gap: 14px;
  align-content: start;
}

.role-form {
  display: grid;
  gap: 14px;
}

.form-group {
  display: grid;
  gap: 6px;
}

.form-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-input, .form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--bg-secondary);
  color: var(--text);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.form-input:focus, .form-textarea:focus {
  border-color: var(--accent);
  background: var(--surface);
}

.form-textarea {
  resize: vertical;
}

.form-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

.required {
  color: var(--danger);
}

.permission-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
  padding: 2px;
}

.perm-check {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.perm-check:hover {
  background: var(--bg-secondary);
}

.perm-check input {
  accent-color: var(--accent);
}

.perm-code {
  font-size: 12px;
  font-weight: 500;
  color: var(--text);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
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
}

.perm-catalog {
  max-height: 700px;
  overflow-y: auto;
}

.perm-count {
  font-size: 13px;
  color: var(--text-muted);
}

.perm-list {
  display: grid;
  gap: 8px;
}

.perm-item {
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: 12px;
  display: grid;
  gap: 2px;
}

.perm-item-code {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.perm-item-name {
  font-size: 12px;
  color: var(--text-muted);
}

.perm-item-desc {
  font-size: 12px;
  color: var(--text-light);
}

.audit-list {
  display: grid;
  gap: 12px;
}

.audit-card .audit-detail {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 8px 0;
}

.page-info {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  padding: 6px 14px;
  background: var(--bg-secondary);
  border-radius: 8px;
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
  white-space: nowrap;
}

.btn-ghost:hover {
  background: var(--bg-secondary);
  color: var(--text);
  border-color: var(--border-hover);
}

.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-ghost.btn-danger {
  color: var(--danger);
  border-color: rgba(194, 65, 12, 0.25);
}

.btn-ghost.btn-danger:hover {
  background: var(--danger-bg);
  border-color: var(--danger);
}

.notice {
  padding: 14px 18px;
  border-radius: 14px;
  font-size: 14px;
}

.notice.info {
  background: rgba(94, 120, 143, 0.08);
  color: #3d6b8e;
  border: 1px solid rgba(94, 120, 143, 0.12);
}

.notice.error {
  background: var(--danger-bg);
  color: var(--danger);
  border: 1px solid rgba(194, 65, 12, 0.12);
}

.empty-text {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
  text-align: center;
  padding: 24px 16px;
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px dashed var(--border);
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  z-index: 1000;
  animation: dialogFadeIn 0.2s ease;
}

@keyframes dialogFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.dialog {
  background: var(--surface);
  border-radius: 20px;
  width: 560px;
  max-width: 92vw;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: dialogSlideIn 0.25s ease;
}

.dialog-sm {
  width: 420px;
}

@keyframes dialogSlideIn {
  from { opacity: 0; transform: translateY(16px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px 0;
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.dialog-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.15s;
}

.dialog-close:hover {
  background: var(--bg-secondary);
  color: var(--text);
}

.dialog-body {
  padding: 20px 24px 24px;
  display: grid;
  gap: 16px;
}

.dialog-desc {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.dialog-desc strong {
  color: var(--text);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .overview-grid { grid-template-columns: 1fr; }
  .roles-layout { grid-template-columns: 1fr; }
  .filter-wide { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr; }
  .filter-bar { grid-template-columns: 1fr; }
  .filter-wide { grid-template-columns: 1fr; }
  .user-controls { grid-template-columns: 1fr; }
  .permission-list { grid-template-columns: 1fr; }
  .section-toolbar { flex-direction: column; align-items: stretch; }
  .form-row { grid-template-columns: 1fr; }
  .dialog { width: 95vw; }
}
</style>
