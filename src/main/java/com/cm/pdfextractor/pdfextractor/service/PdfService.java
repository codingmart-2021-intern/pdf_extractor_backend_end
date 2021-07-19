package com.cm.pdfextractor.pdfextractor.service;

import com.cm.pdfextractor.pdfextractor.model.Pdf;
import com.cm.pdfextractor.pdfextractor.model.PdfCategoryModel;
import com.cm.pdfextractor.pdfextractor.model.PdfDownloadModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface PdfService {

    byte[] downloadPdf(PdfDownloadModel pages) throws Exception;

    PdfDownloadModel categoryPdf(PdfCategoryModel categories) throws Exception;

    List<String> listAllPagesInPdf() throws IOException;

    String savePdfFile(Long id, Pdf pdfData) throws Exception;

    String deletePdf(Long id) throws Exception;

    Pdf findById(Long id) throws Exception;

    List<String> getCategoriesList (Long pdfId) throws Exception;
}
