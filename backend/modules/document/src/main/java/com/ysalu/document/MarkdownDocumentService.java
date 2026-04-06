package com.ysalu.document;

import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.document.DocumentVersionSource;
import com.ysalu.domain.document.MarkdownDocument;
import com.ysalu.domain.document.MarkdownDocumentVersion;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.document.MarkdownDocumentRepository;
import com.ysalu.repository.document.MarkdownDocumentVersionRepository;
import com.ysalu.service.log.OperationLogService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Markdown 文档服务。
@Service
public class MarkdownDocumentService {

    private final MarkdownDocumentRepository markdownDocumentRepository;
    
    private final MarkdownDocumentVersionRepository markdownDocumentVersionRepository;
    
    private final UserAccountRepository userAccountRepository;
    
    private final OperationLogService operationLogService;

    public MarkdownDocumentService(
            MarkdownDocumentRepository markdownDocumentRepository,
            MarkdownDocumentVersionRepository markdownDocumentVersionRepository,
            UserAccountRepository userAccountRepository,
            OperationLogService operationLogService
    ) {
        this.markdownDocumentRepository = markdownDocumentRepository;
        this.markdownDocumentVersionRepository = markdownDocumentVersionRepository;
        this.userAccountRepository = userAccountRepository;
        this.operationLogService = operationLogService;
    }

    @Transactional(readOnly = true)
    // 返回用户自己的文档摘要列表。
    public List<DocumentSummary> listDocuments() {
        List<MarkdownDocument> documents = markdownDocumentRepository.findAllByOrderByUpdatedAtDesc();
        List<DocumentSummary> result = new ArrayList<DocumentSummary>();
        for (MarkdownDocument document : documents) {
            result.add(toSummary(document));
        }
        
        return result;
    }

    @Transactional(readOnly = true)
    // 读取单篇文档的完整内容。
    public DocumentDetail getDocument(Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        return toDetail(document);
    }

    @Transactional
    // 创建新文档，并生成第一个版本快照。
    public DocumentDetail createDocument(Long ownerId, String title, String content, DocumentVersionSource sourceType) {
        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new DocumentException("User account does not exist."));
        MarkdownDocument document = new MarkdownDocument();
        document.setOwner(owner);
        document.setTitle(normalizeTitle(title));
        document.setContent(normalizeContent(content));
        MarkdownDocument savedDocument = markdownDocumentRepository.save(document);
        createVersion(savedDocument, sourceType);
        recordOperationLog(
                owner,
                "DOCUMENT_CREATED",
                savedDocument.getId(),
                "Created markdown document.",
                "title=" + savedDocument.getTitle() + ", mode=" + sourceType.name()
        );
        return toDetail(savedDocument);
    }

    @Transactional
    public DocumentDetail importDocument(Long ownerId, String originalFilename, byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new DocumentException("Markdown file is empty.");
        }
        String resolvedFilename = normalizeImportFilename(originalFilename);
        String content = stripUtf8Bom(new String(fileBytes, StandardCharsets.UTF_8));
        DocumentDetail detail = createDocument(ownerId, extractTitleFromFilename(resolvedFilename), content, DocumentVersionSource.IMPORT);
        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new DocumentException("User account does not exist."));
        recordOperationLog(
                owner,
                "DOCUMENT_IMPORTED",
                detail.getId(),
                "Imported markdown document.",
                "filename=" + resolvedFilename
        );
        return detail;
    }

    @Transactional
    // 更新文档内容；如果内容没变，直接返回当前结果。
    public DocumentDetail updateDocument(Long ownerId, Long documentId, String title, String content, DocumentVersionSource sourceType) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        String normalizedTitle = normalizeTitle(title);
        String normalizedContent = normalizeContent(content);
        if (normalizedTitle.equals(document.getTitle()) && normalizedContent.equals(document.getContent())) {
            return toDetail(document);
        }
        document.setTitle(normalizedTitle);
        document.setContent(normalizedContent);
        MarkdownDocument savedDocument = markdownDocumentRepository.save(document);
        createVersion(savedDocument, sourceType);
        recordOperationLog(
                savedDocument.getOwner(),
                "DOCUMENT_UPDATED",
                savedDocument.getId(),
                "Updated markdown document.",
                "title=" + savedDocument.getTitle() + ", mode=" + sourceType.name()
        );
        return toDetail(savedDocument);
    }

    @Transactional
    // 删除文档前先清理全部版本记录。
    public void deleteDocument(Long ownerId, Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        String title = document.getTitle();
        Long targetId = document.getId();
        UserAccount owner = document.getOwner();
        markdownDocumentVersionRepository.deleteAllByDocument_Id(document.getId());
        markdownDocumentRepository.delete(document);
        recordOperationLog(owner, "DOCUMENT_DELETED", targetId, "Deleted markdown document.", "title=" + title);
    }

    @Transactional(readOnly = true)
    // 返回文档的全部历史版本。
    public List<DocumentVersionView> listVersions(Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        List<MarkdownDocumentVersion> versions =
                markdownDocumentVersionRepository.findAllByDocument_IdOrderByVersionNumberDesc(document.getId());
        List<DocumentVersionView> result = new ArrayList<DocumentVersionView>();
        for (MarkdownDocumentVersion version : versions) {
            result.add(new DocumentVersionView(
                    version.getId(),
                    version.getVersionNumber().intValue(),
                    version.getTitle(),
                    version.getSourceType().name(),
                    version.getCreatedAt()
            ));
        }
        return result;
    }

    @Transactional
    // 把文档回滚到指定版本，并新增一条恢复版本。
    public DocumentDetail restoreVersion(Long ownerId, Long documentId, Long versionId) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        MarkdownDocumentVersion version = markdownDocumentVersionRepository.findByIdAndDocument_Id(versionId, document.getId())
                .orElseThrow(() -> new DocumentException("Version does not exist."));
        document.setTitle(version.getTitle());
        document.setContent(version.getContent());
        MarkdownDocument savedDocument = markdownDocumentRepository.save(document);
        createVersion(savedDocument, DocumentVersionSource.RESTORE);
        recordOperationLog(
                savedDocument.getOwner(),
                "DOCUMENT_RESTORED",
                savedDocument.getId(),
                "Restored markdown document version.",
                "versionId=" + version.getId() + ", versionNumber=" + version.getVersionNumber()
        );
        return toDetail(savedDocument);
    }

    // 每次保存正文后，都把当时内容写入版本表。
    private void createVersion(MarkdownDocument document, DocumentVersionSource sourceType) {
        int nextVersion = markdownDocumentVersionRepository.findTopByDocument_IdOrderByVersionNumberDesc(document.getId())
                .map(existing -> existing.getVersionNumber().intValue() + 1)
                .orElse(1);
        MarkdownDocumentVersion version = new MarkdownDocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(Integer.valueOf(nextVersion));
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setSourceType(sourceType);
        markdownDocumentVersionRepository.save(version);
    }

    private String normalizeTitle(String title) {
        String value = title == null ? "" : title.trim();
        if (value.isEmpty()) {
            throw new DocumentException("Document title is required.");
        }
        if (value.length() > 120) {
            throw new DocumentException("Document title must be at most 120 characters.");
        }
        return value;
    }

    private String normalizeContent(String content) {
        String value = content == null ? "" : content;
        if (value.length() > 100000) {
            throw new DocumentException("Document content must be at most 100000 characters.");
        }
        return value;
    }

    private String normalizeImportFilename(String originalFilename) {
        String value = originalFilename == null ? "" : originalFilename.trim();
        if (value.isEmpty()) {
            throw new DocumentException("Markdown file name is missing.");
        }
        String normalized = value.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (!lowerCase.endsWith(".md") && !lowerCase.endsWith(".markdown")) {
            throw new DocumentException("Only .md or .markdown files are supported.");
        }
        return normalized;
    }

    private String extractTitleFromFilename(String filename) {
        int extensionIndex = filename.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? filename.substring(0, extensionIndex) : filename;
        String normalized = baseName.replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            throw new DocumentException("Document title is required.");
        }
        return normalizeTitle(normalized);
    }

    private String stripUtf8Bom(String content) {
        if (content != null && !content.isEmpty() && content.charAt(0) == '\uFEFF') {
            return content.substring(1);
        }
        return content;
    }

    private DocumentSummary toSummary(MarkdownDocument document) {
        return new DocumentSummary(
                document.getId(),
                document.getTitle(),
                buildExcerpt(document.getContent()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private DocumentDetail toDetail(MarkdownDocument document) {
        return new DocumentDetail(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private String buildExcerpt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "暂无内容";
        }
        String normalized = content.replace("\r", " ").replace("\n", " ").trim();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 80) + "...";
    }

    private void recordOperationLog(UserAccount owner, String action, Long targetId, String message, String detail) {
        operationLogService.record(
                owner == null ? null : owner.getId(),
                owner == null ? null : owner.getUsername(),
                "DOCS",
                action,
                "DOCUMENT",
                targetId,
                true,
                message,
                detail,
                null
        );
    }
}
