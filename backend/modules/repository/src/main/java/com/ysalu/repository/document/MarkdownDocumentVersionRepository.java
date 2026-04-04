package com.ysalu.repository.document;

import com.ysalu.domain.document.MarkdownDocumentVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Markdown文档版本仓储接口。

public interface MarkdownDocumentVersionRepository extends JpaRepository<MarkdownDocumentVersion, Long> {

    List<MarkdownDocumentVersion> findAllByDocument_IdOrderByVersionNumberDesc(Long documentId);

    Optional<MarkdownDocumentVersion> findByIdAndDocument_Id(Long id, Long documentId);

    Optional<MarkdownDocumentVersion> findTopByDocument_IdOrderByVersionNumberDesc(Long documentId);

    void deleteAllByDocument_Id(Long documentId);
}
