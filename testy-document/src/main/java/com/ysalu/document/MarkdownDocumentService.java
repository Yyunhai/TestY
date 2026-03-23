package com.ysalu.document;

import com.ysalu.domain.auth.UserAccount;
import com.ysalu.domain.document.DocumentVersionSource;
import com.ysalu.domain.document.MarkdownDocument;
import com.ysalu.domain.document.MarkdownDocumentVersion;
import com.ysalu.repository.auth.UserAccountRepository;
import com.ysalu.repository.document.MarkdownDocumentRepository;
import com.ysalu.repository.document.MarkdownDocumentVersionRepository;
import com.ysalu.service.log.OperationLogService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Markdown 文档服务。
 */
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
    public List<DocumentSummary> listDocuments(Long ownerId) {
        List<MarkdownDocument> documents = markdownDocumentRepository.findAllByOwner_IdOrderByUpdatedAtDesc(ownerId);
        List<DocumentSummary> result = new ArrayList<DocumentSummary>();
        for (MarkdownDocument document : documents) {
            result.add(toSummary(document));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DocumentDetail getDocument(Long ownerId, Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        return toDetail(document);
    }

    @Transactional
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
    public List<DocumentVersionView> listVersions(Long ownerId, Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
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
