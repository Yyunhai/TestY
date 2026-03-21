package com.ysalu.document;

import com.ysalu.domain.MarkdownDocument;
import com.ysalu.domain.UserAccount;
import com.ysalu.repository.MarkdownDocumentRepository;
import com.ysalu.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Markdown 文档服务。
 * 负责用户文档的查询、创建、更新以及摘要组装。
 */
@Service
public class MarkdownDocumentService {

    private final MarkdownDocumentRepository markdownDocumentRepository;
    private final UserAccountRepository userAccountRepository;

    public MarkdownDocumentService(
            MarkdownDocumentRepository markdownDocumentRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.markdownDocumentRepository = markdownDocumentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * 查询某个用户的所有文档列表，按更新时间倒序返回。
     */
    @Transactional(readOnly = true)
    public List<DocumentSummary> listDocuments(Long ownerId) {
        List<MarkdownDocument> documents = markdownDocumentRepository.findAllByOwner_IdOrderByUpdatedAtDesc(ownerId);
        List<DocumentSummary> result = new ArrayList<DocumentSummary>();
        for (MarkdownDocument document : documents) {
            result.add(toSummary(document));
        }
        return result;
    }

    /**
     * 查询某个用户的单篇文档详情。
     */
    @Transactional(readOnly = true)
    public DocumentDetail getDocument(Long ownerId, Long documentId) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        return toDetail(document);
    }

    /**
     * 创建新文档，并校验所属用户和文档内容。
     */
    @Transactional
    public DocumentDetail createDocument(Long ownerId, String title, String content) {
        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new DocumentException("User account does not exist."));

        MarkdownDocument document = new MarkdownDocument();
        document.setOwner(owner);
        document.setTitle(normalizeTitle(title));
        document.setContent(normalizeContent(content));
        return toDetail(markdownDocumentRepository.save(document));
    }

    /**
     * 更新指定文档。
     * 文档查询条件带 ownerId，因此天然限制为“只能修改自己的文档”。
     */
    @Transactional
    public DocumentDetail updateDocument(Long ownerId, Long documentId, String title, String content) {
        MarkdownDocument document = markdownDocumentRepository.findByIdAndOwner_Id(documentId, ownerId)
                .orElseThrow(() -> new DocumentException("Document does not exist."));
        document.setTitle(normalizeTitle(title));
        document.setContent(normalizeContent(content));
        return toDetail(markdownDocumentRepository.save(document));
    }

    /**
     * 规范化标题并校验长度。
     */
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

    /**
     * 校验正文长度。
     */
    private String normalizeContent(String content) {
        String value = content == null ? "" : content;
        if (value.length() > 100000) {
            throw new DocumentException("Document content must be at most 100000 characters.");
        }
        return value;
    }

    /**
     * 将实体对象转换为摘要对象。
     */
    private DocumentSummary toSummary(MarkdownDocument document) {
        return new DocumentSummary(
                document.getId(),
                document.getTitle(),
                buildExcerpt(document.getContent()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    /**
     * 将实体对象转换为详情对象。
     */
    private DocumentDetail toDetail(MarkdownDocument document) {
        return new DocumentDetail(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    /**
     * 为文档列表生成正文摘要。
     */
    private String buildExcerpt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "空文档";
        }
        String normalized = content.replace("\r", " ").replace("\n", " ").trim();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 80) + "...";
    }
}
