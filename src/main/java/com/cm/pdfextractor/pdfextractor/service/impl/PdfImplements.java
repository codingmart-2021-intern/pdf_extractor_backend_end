package com.cm.pdfextractor.pdfextractor.service.impl;

import com.cm.pdfextractor.pdfextractor.model.Pages;
import com.cm.pdfextractor.pdfextractor.model.Pdf;
import com.cm.pdfextractor.pdfextractor.model.PdfDownloadModel;
import com.cm.pdfextractor.pdfextractor.model.User;
import com.cm.pdfextractor.pdfextractor.repository.PagesRepository;
import com.cm.pdfextractor.pdfextractor.repository.PdfRepository;
import com.cm.pdfextractor.pdfextractor.repository.UserRepository;
import com.cm.pdfextractor.pdfextractor.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Text;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfImplements implements PdfService {

    private final UserRepository userRepository;
    private final PdfRepository pdfRepository;
    private final PagesRepository pagesRepository;

    @Override
    public byte[] downloadPdf(PdfDownloadModel pageNoModel) throws Exception {
        // get the directory path
        String userDirectory = Paths.get("").toAbsolutePath().toString();
        //  set the destination path
        String destinationDir = userDirectory + "/file/pdf/";
        File destinationFile = new File(destinationDir);

        // create folder if not exist
        if (!destinationFile.exists()) {
            destinationFile.mkdir();
            System.out.println("Folder Created ---> " + destinationFile.getAbsolutePath());
        }

        Pdf pdfData = pdfRepository.findById(pageNoModel.getPdfId()).orElseThrow(() -> new Exception("Pdf id not found"));
        // pdf url
        URL url = new URL(pdfData.getUrl());
        PDDocument document = PDDocument.load(url.openStream());
        Splitter splitter = new Splitter();
        List<PDDocument> Pages = splitter.split(document);

        //  merged file destination
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName(destinationDir + "merged.pdf");

        int arr[] = pageNoModel.getPageNos();
        for (int i = 0; i < arr.length; i++) {
            PDDocument pd = Pages.get(arr[i]);
            pd.save(destinationDir + arr[i] + ".pdf");
            File file = new File(destinationDir + arr[i] + ".pdf");
            PDFmerger.addSource(file);
            pd.close();
        }
        PDFmerger.mergeDocuments();

        document.close();

        byte[] byteData = Files.readAllBytes(Paths.get(destinationDir + "merged.pdf"));

        boolean isCleaned = cleanUpFolder(arr, destinationDir);
        if (!isCleaned) throw new Exception("Error in cleaningup folder");
        return byteData;
    }

    //    not using--
    @Override
    public List<String> listAllPagesInPdf() throws IOException {

        String userDirectory = Paths.get("").toAbsolutePath().toString();
        String destinationDir = userDirectory + "/file/pdf/";
        URL url = new URL("https://file-examples-com.github.io/uploads/2017/10/file-example_PDF_1MB.pdf");
        PDDocument document = PDDocument.load(url.openStream());
        Splitter splitter = new Splitter();
        List<PDDocument> Pages = splitter.split(document);
        List<String> imageData = new ArrayList<>();
        Iterator<PDDocument> iterator = Pages.listIterator();
        int i = 1;

        while (iterator.hasNext()) {
            PDDocument pd = iterator.next();
            pd.save(destinationDir + i + ".pdf");
            byte[] byteData = Files.readAllBytes(Paths.get(destinationDir + i + ".pdf"));
            String encodedString = Base64.getEncoder().encodeToString(byteData);
            imageData.add(encodedString);
            Files.deleteIfExists(Paths.get(destinationDir + i++ + ".pdf"));
            pd.close();
        }
        document.close();
        return imageData;
    }

    @Override
    public String savePdfFile(Long id, Pdf pdfData) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("User id not found!!"));
        pdfData.setUser(user);
        Pdf pdfSavedData = pdfRepository.save(pdfData);
        fillPdfPages(pdfSavedData);
        return "Data saved successdully!!";
    }

    @Override
    public String deletePdf(Long id) throws Exception {
        pdfRepository.findById(id).orElseThrow(() -> new Exception("Pdf id not found"));
        pdfRepository.deleteById(id);
        return null;
    }

    @Override
    public Pdf findById(Long id) throws Exception {
        return pdfRepository.findById(id).orElseThrow(() -> new Exception("Pdf id not found"));
    }




    public boolean cleanUpFolder(int arr[], String path) throws IOException {
        try {
            // delete merged file
            Files.deleteIfExists(Paths.get(path + "merged.pdf"));
            //clean up the files..
            for (int i = 0; i < arr.length; i++) {
                Files.deleteIfExists(Paths.get(path + arr[i] + ".pdf"));
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public void fillPdfPages(Pdf pdfData) throws IOException {
        URL url = new URL(pdfData.getUrl());
        PDDocument document = PDDocument.load(url.openStream());
        int totalPage = document.getNumberOfPages();
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        for (int i = 0; i < totalPage; i++) {
            pdfTextStripper.setStartPage(i + 1);
            pdfTextStripper.setEndPage(i + 1);
            String text = pdfTextStripper.getText(document);
//            System.out.println("text = " + text);

            Pages pageData = Pages.builder()
                    .pdf(pdfData)
                    .page_number(Long.parseLong(String.valueOf(i)))
                    .content(text)
                    .build();
            pagesRepository.save(pageData);
        }

    }

}
