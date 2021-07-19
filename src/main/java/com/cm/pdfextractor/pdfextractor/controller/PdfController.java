package com.cm.pdfextractor.pdfextractor.controller;

import com.cm.pdfextractor.pdfextractor.model.ErrorResponse;
import com.cm.pdfextractor.pdfextractor.model.Pdf;
import com.cm.pdfextractor.pdfextractor.model.PdfCategoryModel;
import com.cm.pdfextractor.pdfextractor.model.PdfDownloadModel;
import com.cm.pdfextractor.pdfextractor.service.PdfService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/v1/pdf")
public class PdfController {

    private final PdfService pdfService;

    @PostMapping("/download")
    private ResponseEntity<?> downloadPdf(@RequestBody PdfDownloadModel pages) throws Exception {
        byte[] content = pdfService.downloadPdf(pages);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Here you have to set the actual filename of your pdf
        String filename = "output.pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @PostMapping("/category")
    private ResponseEntity<?> categoryPdf(@RequestBody PdfCategoryModel categories) throws Exception {

        Pdf fetchedPdf = pdfService.findById(categories.getPdfId());

        if( fetchedPdf.getPdf_id() >= 0 ){
            PdfDownloadModel pages = pdfService.categoryPdf(categories);

            byte[] content = pdfService.downloadPdf(pages);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Here you have to set the actual filename of your pdf
            String filename = "output.pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        }
        else {

            ErrorResponse errorResponse = new ErrorResponse(false,"Pdf not found");
            return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/categorieslist/{pdfid}")
    private ResponseEntity<?> getCategoriesList(@PathVariable Long pdfid) throws Exception {

        return new ResponseEntity<>(pdfService.getCategoriesList(pdfid),HttpStatus.OK);
    }

    @GetMapping("/getAllPages")
    private ResponseEntity<?> getAllPagesInPdf() throws Exception {
        return new ResponseEntity<>(pdfService.listAllPagesInPdf(), HttpStatus.OK);
    }

    @PostMapping("/save/pdfFile/{userId}")
    private ResponseEntity<?> savePdfFile(@PathVariable Long userId, @RequestBody Pdf pdfData) throws Exception {
        return new ResponseEntity<>(pdfService.savePdfFile(userId, pdfData), HttpStatus.CREATED);
    }

    @DeleteMapping("/deletePdf/{id}")
    private ResponseEntity<?> deletePdf(@PathVariable Long id) throws Exception {
        return new ResponseEntity<>(pdfService.deletePdf(id), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    private ResponseEntity<?> findById(@PathVariable Long id) throws Exception {
        return new ResponseEntity<>(pdfService.findById(id), HttpStatus.CREATED);
    }




}
