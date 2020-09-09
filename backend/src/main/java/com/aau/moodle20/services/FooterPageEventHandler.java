package com.aau.moodle20.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;

import java.io.IOException;

public class FooterPageEventHandler implements IEventHandler {

    private final Document document;

    FooterPageEventHandler(Document document)
    {
        this.document = document;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDocument = ((PdfDocumentEvent) event).getDocument();
        PdfPage pdfPage = docEvent.getPage();
        int pageNumber = pdfDocument.getPageNumber(pdfPage);
        PdfCanvas canvas = new PdfCanvas(docEvent.getPage());
        Rectangle pageSize = docEvent.getPage().getPageSize();
        canvas.beginText();
        try {
            canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD), 9);
        } catch (IOException e) {
          //Do Nothing
        }
        canvas.moveText((pageSize.getRight() - document.getRightMargin() - (pageSize.getLeft() + document.getLeftMargin())) / 2 + document.getLeftMargin(), (pageSize.getBottom() + document.getBottomMargin())-20 /*pageSize.getTop() - document.getTopMargin() + 10*/)
                .showText(Integer.toString(pageNumber))
                .endText()
                .release();
    }
}
