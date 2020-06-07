package com.aau.moodle20.component;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.entity.Course;
import com.aau.moodle20.entity.UserInCourse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class PdfHelper {

    private Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,Font.BOLD);

    private Font normalFont = FontFactory.getFont(FontFactory.HELVETICA,13);

    @Autowired
    private ResourceBundleMessageSource messageSource;

    public void addAttendanceTitle(Document document) throws DocumentException
    {
        String headerText = messageSource.getMessage("attendanceList.title", null, LocaleContextHolder.getLocale());

        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        Paragraph paragraph = new Paragraph(headerText, headFont);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        preface.add(paragraph);

        addEmptyLine(preface, 3);
        document.add(preface);
    }

    public void  addAttendanceTable(Document document, Course course) throws DocumentException {

        String forename = messageSource.getMessage("attendanceList.forename", null, LocaleContextHolder.getLocale());
        String surname = messageSource.getMessage("attendanceList.surname", null, LocaleContextHolder.getLocale());
        String matriculationNumber = messageSource.getMessage("attendanceList.matriculationNumber", null, LocaleContextHolder.getLocale());
        String signature = messageSource.getMessage("attendanceList.signature", null, LocaleContextHolder.getLocale());

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(95);
        table.setWidths(new int[]{2, 2, 3,3});


        table.setHeaderRows(1);

        PdfPCell hcell;
        hcell = new PdfPCell(new Phrase(forename, normalFont));
        hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(hcell);

        hcell = new PdfPCell(new Phrase(surname, normalFont));
        hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(hcell);

        hcell = new PdfPCell(new Phrase(matriculationNumber, normalFont));
        hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(hcell);

        hcell = new PdfPCell(new Phrase(signature, normalFont));
        hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(hcell);

        for (UserInCourse userInCourse : course.getStudents()) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            PdfPCell cell;

            cell = new PdfPCell(new Phrase(userInCourse.getUser().getForename()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(userInCourse.getUser().getSurname()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(userInCourse.getUser().getMatriculationNumber()));
            cell.setPaddingLeft(5);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(""));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPaddingRight(5);
            table.addCell(cell);
        }

        document.add(table);
    }

    private  void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
