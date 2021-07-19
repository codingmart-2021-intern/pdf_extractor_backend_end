package com.cm.pdfextractor.pdfextractor.service.impl;

import com.cm.pdfextractor.pdfextractor.model.*;
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
import java.util.*;

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
        Set<Integer> set = new LinkedHashSet<>();

        for (int i = 0; i < arr.length; i++) {
            set.add(arr[i]);
        }
        int a[] = new int[set.size()];
        int temp = 0;
        for (Integer i : set) {
            a[temp++] = i;
        }
        for (int i = 0; i < a.length; i++) {
            PDDocument pd = Pages.get(a[i]);
            pd.save(destinationDir + a[i] + ".pdf");
            File file = new File(destinationDir + a[i] + ".pdf");
            PDFmerger.addSource(file);
            pd.close();
        }
        PDFmerger.mergeDocuments();

        document.close();

        byte[] byteData = Files.readAllBytes(Paths.get(destinationDir + "merged.pdf"));

        boolean isCleaned = cleanUpFolder(a, destinationDir);
        if (!isCleaned) throw new Exception("Error in cleaningup folder");
        return byteData;
    }

    @Override
    public PdfDownloadModel categoryPdf(PdfCategoryModel categories) throws Exception {

        Optional<Pdf> fetchedPdf = pdfRepository.findById(categories.getPdfId());

        Pdf pdfDetails = fetchedPdf.get();
        List<Pages> pagesData = pdfDetails.getPages();
        List<Long> categoryPages = new ArrayList<Long>();

        for (int i = 0; i < pagesData.size(); i++) {

            String content = pagesData.get(i).getContent();
            String[] categoryList = categories.getCategories();
            for (int j = 0; j < categoryList.length; j++) {
                if (content
                        .replaceAll("\n", "")
                        .toLowerCase()
                        .contains(
                                categoryList[j].toLowerCase()
                        )
                ) {
                    categoryPages.add(pagesData.get(i).getPage_number());
                    System.out.println("pagesData.get(i).getPage_number() = " + pagesData.get(i).getPage_number());
                }
            }
        }

        Collections.sort(categoryPages);
        PdfDownloadModel pages = new PdfDownloadModel();
        int[] arr = new int[categoryPages.size()];

        for (int i = 0; i < categoryPages.size(); i++) {
            arr[i] = Integer.parseInt(categoryPages.get(i) + "");
        }

        pages.setPdfId(categories.getPdfId());
        pages.setPageNos(arr);

        return pages;
    }

    @Override
    public List<String> getCategoriesList(Long pdfId) throws Exception {

        List<String> categories = new ArrayList<String>();

        Optional<Pdf> fetchPdf = pdfRepository.findById(pdfId);

        List<Pages> pages = fetchPdf.get().getPages();

        for (int i = 0; i < pages.size(); i++) {
            String content = pages.get(i).getContent();

            if (content.toLowerCase().contains(new String("Clients By Industry").toLowerCase())) {
                System.out.println(content);
                String[] spl = content.split("\n");
                for (int j = 0; j < spl.length; j++) {

                    if (!spl[j].toLowerCase().contains(new String("Clients By Industry").toLowerCase()))
                        categories.add(spl[j]);
                }
            }
        }

        return categories;
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
