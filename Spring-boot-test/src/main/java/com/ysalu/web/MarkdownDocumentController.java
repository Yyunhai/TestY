package com.ysalu.web;

import com.ysalu.document.DocumentDetail;
import com.ysalu.document.DocumentSummary;
import com.ysalu.document.MarkdownDocumentService;
import com.ysalu.service.PermissionCodes;
import com.ysalu.web.auth.SessionUser;
import com.ysalu.web.document.SaveDocumentRequest;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Markdown 文档控制器。
 * 所有文档接口都以“当前登录用户”为作用域，避免跨用户访问文档。
 */
@RestController
@RequestMapping("/api/docs")
public class MarkdownDocumentController {

    private final MarkdownDocumentService markdownDocumentService;
    private final SessionAuthorization sessionAuthorization;

    public MarkdownDocumentController(
            MarkdownDocumentService markdownDocumentService,
            SessionAuthorization sessionAuthorization
    ) {
        this.markdownDocumentService = markdownDocumentService;
        this.sessionAuthorization = sessionAuthorization;
    }

    /**
     * 查询当前用户的文档列表。
     */
    @GetMapping
    public List<DocumentSummary> listDocuments(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_READ);
        return markdownDocumentService.listDocuments(sessionUser.getId());
    }

    /**
     * 查询当前用户的某一篇文档详情。
     */
    @GetMapping("/{documentId}")
    public DocumentDetail getDocument(@PathVariable Long documentId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_READ);
        return markdownDocumentService.getDocument(sessionUser.getId(), documentId);
    }

    /**
     * 创建属于当前用户的新文档。
     */
    @PostMapping
    public DocumentDetail createDocument(@Valid @RequestBody SaveDocumentRequest request, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        return markdownDocumentService.createDocument(sessionUser.getId(), request.getTitle(), request.getContent());
    }

    /**
     * 更新当前用户已有文档的标题和内容。
     */
    @PutMapping("/{documentId}")
    public DocumentDetail updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody SaveDocumentRequest request,
            HttpSession session
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        return markdownDocumentService.updateDocument(sessionUser.getId(), documentId, request.getTitle(), request.getContent());
    }
}
