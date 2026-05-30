<template>
  <div class="docs-page">
    <!-- Document Sidebar -->
    <aside class="docs-sidebar">
      <div class="sidebar-top">
        <button class="btn-primary btn-full" type="button" @click="$emit('new-document')">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3V13M3 8H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          新建文档
        </button>
        <div class="search-box">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="6.5" cy="6.5" r="5" stroke="currentColor" stroke-width="1.3"/>
            <path d="M10.5 10.5L14.5 14.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
          </svg>
          <input
            class="search-input"
            type="text"
            placeholder="搜索文档..."
            :value="searchKeyword"
            @input="$emit('update:searchKeyword', $event.target.value)"
          />
          <button v-if="searchKeyword" class="search-clear" type="button" @click="$emit('update:searchKeyword', '')">&times;</button>
        </div>
        <div class="doc-count">
          <span>{{ filteredDocuments.length }} / {{ documents.length }} 篇文档</span>
          <button class="btn-text" type="button" @click="$emit('refresh')">刷新</button>
        </div>
      </div>
      <div class="doc-list">
        <button
          v-for="doc in filteredDocuments"
          :key="doc.id"
          type="button"
          class="doc-item"
          :class="{ active: currentDocumentId === doc.id }"
          @click="$emit('open-document', doc.id)"
        >
          <span class="doc-item-title">{{ doc.title || '未命名文档' }}</span>
        </button>
        <div v-if="documents.length && !filteredDocuments.length" class="doc-empty">没有匹配的文档</div>
        <div v-else-if="!documents.length" class="doc-empty">暂无文档，点击上方按钮创建</div>
      </div>
    </aside>

    <!-- Editor Main -->
    <div class="docs-main">
      <!-- Editor Area -->
      <div class="editor-section">
        <div class="editor-header">
          <div class="editor-header-left">
            <h2 class="editor-title">{{ currentDocumentId ? '编辑文档' : '新建草稿' }}</h2>
            <span class="save-hint">{{ saveHint }}</span>
          </div>
          <div class="editor-actions">
            <button class="btn-ghost" type="button" @click="$emit('save', 'MANUAL')">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M13 14H3C2.44772 14 2 13.5523 2 13V3C2 2.44772 2.44772 2 3 2H10L14 6V13C14 13.5523 13.5523 14 13 14Z" stroke="currentColor" stroke-width="1.3"/>
                <path d="M10 2V6H14" stroke="currentColor" stroke-width="1.3"/>
                <path d="M5 9H11M5 12H8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
              </svg>
              保存
            </button>
            <button class="btn-ghost btn-danger" type="button" @click="$emit('delete-document')">删除</button>
          </div>
        </div>

        <div class="title-field">
          <input
            ref="titleInput"
            class="title-input"
            type="text"
            placeholder="文档标题"
            :value="documentForm.title"
            @input="$emit('update:documentForm', { ...documentForm, title: $event.target.value })"
          />
        </div>

        <div class="editor-workbench">
          <div class="editor-pane">
            <div class="pane-header">
              <span class="pane-label">Markdown 编辑</span>
              <span class="word-count">{{ documentWordCount }} 字</span>
            </div>
            <textarea
              class="editor-textarea"
              placeholder="在这里输入 Markdown 内容..."
              :value="documentForm.content"
              @input="$emit('update:documentForm', { ...documentForm, content: $event.target.value })"
            ></textarea>
          </div>
          <div class="preview-pane">
            <div class="pane-header">
              <span class="pane-label">实时预览</span>
            </div>
            <div class="preview-content markdown-body" v-html="renderedMarkdown"></div>
          </div>
        </div>
      </div>

      <!-- Version History -->
      <div class="versions-section">
        <div class="versions-header">
          <div>
            <p class="section-eyebrow">版本历史</p>
            <h3 class="section-title">历史版本</h3>
          </div>
          <button class="btn-ghost" type="button" :disabled="!currentDocumentId" @click="$emit('refresh-versions')">刷新</button>
        </div>
        <div class="versions-list" v-if="documentVersions.length">
          <div v-for="version in documentVersions" :key="version.id" class="version-item">
            <div class="version-info">
              <span class="version-number">#{{ version.versionNumber }}</span>
              <div class="version-meta">
                <span class="version-title">{{ version.title || '未命名文档' }}</span>
                <span class="version-detail">{{ version.sourceType }} · {{ formatDateTime(version.createdAt) }}</span>
              </div>
            </div>
            <button class="btn-ghost btn-sm" type="button" @click="$emit('restore-version', version.id)">恢复</button>
          </div>
        </div>
        <div v-else class="versions-empty">暂无历史版本</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "DocsPage",
  props: {
    documents: { type: Array, default: () => [] },
    searchKeyword: { type: String, default: "" },
    currentDocumentId: { type: [String, Number, Object], default: null },
    documentForm: { type: Object, default: () => ({ title: "", content: "" }) },
    documentVersions: { type: Array, default: () => [] },
    saveHint: { type: String, default: "" },
    renderedMarkdown: { type: String, default: "" },
    documentWordCount: { type: Number, default: 0 }
  },
  emits: [
    "update:searchKeyword", "update:documentForm",
    "new-document", "open-document", "save", "delete-document",
    "refresh", "refresh-versions", "restore-version"
  ],
  computed: {
    filteredDocuments() {
      const keyword = (this.searchKeyword || "").toLowerCase();
      return this.documents.filter((item) =>
        !keyword || [item.title, item.excerpt].some((v) => String(v || "").toLowerCase().includes(keyword))
      );
    }
  },
  methods: {
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
.docs-page {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  min-height: calc(100vh - 140px);
}

/* Sidebar */
.docs-sidebar {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-top {
  padding: 18px;
  display: grid;
  gap: 12px;
  border-bottom: 1px solid var(--border);
}

.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  border-radius: 12px;
  background: var(--accent);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-full { width: 100%; }

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 10px 32px 10px 36px;
  border: 1.5px solid var(--border);
  border-radius: 10px;
  background: var(--bg-secondary);
  color: var(--text);
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.search-input:focus {
  border-color: var(--accent);
  background: var(--surface);
}

.search-input::placeholder {
  color: var(--text-light);
}

.search-clear {
  position: absolute;
  right: 8px;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 6px;
  background: var(--bg-secondary);
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.doc-count {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-muted);
}

.btn-text {
  border: none;
  background: none;
  color: var(--accent);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
}

.btn-text:hover {
  text-decoration: underline;
}

.doc-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.doc-item {
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  transition: all 0.15s;
}

.doc-item:hover {
  background: var(--bg-secondary);
}

.doc-item.active {
  background: var(--accent-bg);
}

.doc-item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-item.active .doc-item-title {
  color: var(--accent);
}

.doc-empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}

/* Main Editor */
.docs-main {
  display: grid;
  gap: 20px;
  min-width: 0;
}

.editor-section {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
  display: grid;
  gap: 18px;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.editor-header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.editor-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
}

.save-hint {
  font-size: 13px;
  color: var(--text-muted);
}

.editor-actions {
  display: flex;
  gap: 8px;
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

.btn-danger {
  color: var(--danger);
  border-color: rgba(194, 65, 12, 0.2);
}

.btn-danger:hover {
  background: var(--danger-bg);
  color: var(--danger);
  border-color: rgba(194, 65, 12, 0.3);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.title-field {
  position: relative;
}

.title-input {
  width: 100%;
  padding: 14px 16px;
  border: 1.5px solid var(--border);
  border-radius: 12px;
  background: var(--bg-secondary);
  color: var(--text);
  font-size: 18px;
  font-weight: 600;
  outline: none;
  transition: all 0.2s;
}

.title-input:focus {
  border-color: var(--accent);
  background: var(--surface);
  box-shadow: 0 0 0 3px rgba(194, 65, 12, 0.06);
}

.title-input::placeholder {
  color: var(--text-light);
  font-weight: 400;
}

/* Editor Workbench */
.editor-workbench {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  min-height: 500px;
}

.editor-pane, .preview-pane {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
}

.pane-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.word-count {
  font-size: 12px;
  color: var(--text-light);
}

.editor-textarea {
  flex: 1;
  width: 100%;
  padding: 16px;
  border: 1.5px solid var(--border);
  border-radius: 14px;
  background: var(--bg-secondary);
  color: var(--text);
  font-family: "JetBrains Mono", "Fira Code", "Cascadia Code", monospace;
  font-size: 14px;
  line-height: 1.7;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
}

.editor-textarea:focus {
  border-color: var(--accent);
}

.editor-textarea::placeholder {
  color: var(--text-light);
}

.preview-content {
  flex: 1;
  padding: 16px;
  border: 1.5px solid var(--border);
  border-radius: 14px;
  background: var(--bg-secondary);
  overflow-y: auto;
  font-size: 15px;
  line-height: 1.75;
}

/* Version History */
.versions-section {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 24px;
}

.versions-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.section-eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.versions-list {
  display: grid;
  gap: 8px;
}

.version-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  transition: all 0.15s;
}

.version-item:hover {
  background: var(--bg-secondary);
}

.version-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.version-number {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
  min-width: 32px;
}

.version-meta {
  display: grid;
  gap: 2px;
}

.version-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.version-detail {
  font-size: 12px;
  color: var(--text-muted);
}

.versions-empty {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--text-muted);
}

@media (max-width: 1200px) {
  .docs-page { grid-template-columns: 1fr; }
  .docs-sidebar { max-height: 300px; }
  .editor-workbench { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .editor-section { padding: 16px; }
  .editor-header { flex-direction: column; align-items: stretch; }
}
</style>
