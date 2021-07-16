package com.cm.pdfextractor.pdfextractor.repository;

import com.cm.pdfextractor.pdfextractor.model.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PdfRepository extends JpaRepository<Pdf, Long> {
}
