package com.cm.pdfextractor.pdfextractor.repository;

import com.cm.pdfextractor.pdfextractor.model.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PdfRepository extends JpaRepository<Pdf, Long> {
}
