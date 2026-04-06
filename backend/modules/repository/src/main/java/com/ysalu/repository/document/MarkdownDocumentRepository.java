package com.ysalu.repository.document;

import com.ysalu.domain.document.MarkdownDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Markdown文档仓储接口。

public interface MarkdownDocumentRepository extends JpaRepository<MarkdownDocument, Long> {

    List<MarkdownDocument> findAllByOrderByUpdatedAtDesc();

    List<MarkdownDocument> findAllByOwner_IdOrderByUpdatedAtDesc(Long ownerId);

    Optional<MarkdownDocument> findByIdAndOwner_Id(Long id, Long ownerId);

    void deleteByIdAndOwner_Id(Long id, Long ownerId);
}
