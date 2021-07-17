package com.cm.pdfextractor.pdfextractor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PdfDownloadModel {
    private Long pdfId;
    private int[] pageNos;
}
