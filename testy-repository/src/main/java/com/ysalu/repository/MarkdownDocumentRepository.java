package com.ysalu.repository;

import com.ysalu.domain.MarkdownDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Markdown 文档仓储接口。
 */
public interface MarkdownDocumentRepository extends JpaRepository<MarkdownDocument, Long> {

    List<MarkdownDocument> findAllByOwner_IdOrderByUpdatedAtDesc(Long ownerId);

    Optional<MarkdownDocument> findByIdAndOwner_Id(Long id, Long ownerId);
}
