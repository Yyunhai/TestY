package com.ysalu.web.document;

import com.ysalu.document.DocumentDetail;
import com.ysalu.document.DocumentSummary;
import com.ysalu.document.DocumentVersionView;
import com.ysalu.document.MarkdownDocumentService;
import com.ysalu.domain.document.DocumentVersionSource;
import com.ysalu.service.common.AuthException;
import com.ysalu.service.security.PermissionCodes;
import com.ysalu.web.auth.SessionUser;
import com.ysalu.web.common.SessionAuthorization;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Markdown 文档控制器。
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

    @GetMapping
    public List<DocumentSummary> listDocuments(HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_READ);
        return markdownDocumentService.listDocuments(sessionUser.getId());
    }

    @GetMapping("/{documentId}")
    public DocumentDetail getDocument(@PathVariable Long documentId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_READ);
        return markdownDocumentService.getDocument(sessionUser.getId(), documentId);
    }

    @PostMapping
    public DocumentDetail createDocument(
            @Valid @RequestBody SaveDocumentRequest request,
            @RequestParam(name = "mode", defaultValue = "MANUAL") String mode,
            HttpSession session
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        return markdownDocumentService.createDocument(
                sessionUser.getId(),
                request.getTitle(),
                request.getContent(),
                parseSaveMode(mode)
        );
    }

    @PutMapping("/{documentId}")
    public DocumentDetail updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody SaveDocumentRequest request,
            @RequestParam(name = "mode", defaultValue = "MANUAL") String mode,
            HttpSession session
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        return markdownDocumentService.updateDocument(
                sessionUser.getId(),
                documentId,
                request.getTitle(),
                request.getContent(),
                parseSaveMode(mode)
        );
    }

    @DeleteMapping("/{documentId}")
    public void deleteDocument(@PathVariable Long documentId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        markdownDocumentService.deleteDocument(sessionUser.getId(), documentId);
    }

    @GetMapping("/{documentId}/versions")
    public List<DocumentVersionView> listVersions(@PathVariable Long documentId, HttpSession session) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_READ);
        return markdownDocumentService.listVersions(sessionUser.getId(), documentId);
    }

    @PostMapping("/{documentId}/versions/{versionId}/restore")
    public DocumentDetail restoreVersion(
            @PathVariable Long documentId,
            @PathVariable Long versionId,
            HttpSession session
    ) {
        SessionUser sessionUser = sessionAuthorization.requirePermission(session, PermissionCodes.DOCS_WRITE);
        return markdownDocumentService.restoreVersion(sessionUser.getId(), documentId, versionId);
    }

    private DocumentVersionSource parseSaveMode(String mode) {
        try {
            return DocumentVersionSource.valueOf((mode == null ? "MANUAL" : mode.trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new AuthException("Invalid document save mode.");
        }
    }
}
