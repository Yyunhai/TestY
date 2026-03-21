package com.ysalu.domain.document;

/**
 * 文档版本来源。
 * 用于区分手动保存、自动保存和从历史版本恢复。
 */
public enum DocumentVersionSource {
    MANUAL,
    AUTOSAVE,
    RESTORE
}
