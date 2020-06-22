package com.aau.moodle20.component;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PdfHelper {

    private Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,16,Font.BOLD);

    private Font normalFont = FontFactory.getFont(FontFactory.HELVETICA,13);


    private ResourceBundleMessageSource resourceBundleMessageSource;

    public PdfHelper(ResourceBundleMessageSource resourceBundleMessageSource)
    {
        this.resourceBundleMessageSource = resourceBundleMessageSource;
    }

    public void addTitle(Document document, String titleCode) throws DocumentException
    {
        String headerText = getLocaleMessage(titleCode);

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

        String forename = getLocaleMessage("attendanceList.forename");
        String surname = getLocaleMessage("attendanceList.surname");
        String matriculationNumber = getLocaleMessage("attendanceList.matriculationNumber");
        String signature = getLocaleMessage("attendanceList.signature");

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


    public void  addKreuzelTable(Document document, ExerciseSheet exerciseSheet) throws DocumentException {

        String name = getLocaleMessage("kreuzelList.name");
        String matriculationNumber = getLocaleMessage("kreuzelList.matriculationNumber");

        long exampleNumber = exerciseSheet.getExamples().stream()
                .filter(example -> example.getSubExamples().isEmpty())
                .count();

        List<Example> exampleList = exerciseSheet.getExamples().stream()
                .filter(example -> example.getParentExample()==null)
                .sorted(Comparator.comparing(Example::getOrder))
                .collect(Collectors.toList());

        List<Example> examplesInTable = new ArrayList<>();

        PdfPTable table = new PdfPTable((int) (exampleNumber + 2));
        table.setWidthPercentage(95);
        float [] widths = new float[(int) (exampleNumber+2)];
        Arrays.fill(widths, (float) 0.2);
        widths[0] = (float) 1;
        widths[1] = (float) 1;
        table.setWidths(widths);
        table.setHeaderRows(1);

        table.addCell(getTableHeaderCell(name));
        table.addCell(getTableHeaderCell(matriculationNumber));

        for(Example example: exampleList)
        {
            if(example.getSubExamples().isEmpty())
            {
                table.addCell(getTableHeaderCell(Integer.toString(example.getOrder()+1)));
                examplesInTable.add(example);
            }
            else
            {
                List<Example> subExamples = example.getSubExamples().stream().sorted(Comparator.comparing(Example::getOrder))
                        .collect(Collectors.toList());
                for(Example subExample:subExamples)
                {
                    String headerText = (example.getOrder() + 1) + "." + (subExample.getOrder() + 1);
                    table.addCell(getTableHeaderCell(headerText));
                    examplesInTable.add(subExample);
                }
            }
        }

        for (UserInCourse userInCourse : exerciseSheet.getCourse().getStudents()) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            PdfPCell cell;

            cell = new PdfPCell(new Phrase(userInCourse.getUser().getForename() +" "+ userInCourse.getUser().getSurname()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);

            table.addCell(cell);

            cell = new PdfPCell(new Phrase(userInCourse.getUser().getMatriculationNumber()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);

            for(Example example : examplesInTable)
            {
                Optional<FinishesExample> finishesExample = example.getExamplesFinishedByUser().stream()
                        .filter(finishesExample1 -> finishesExample1.getUser().getMatriculationNumber().equals(userInCourse.getUser().getMatriculationNumber()))
                        .findFirst();

                if(finishesExample.isPresent() && !(EFinishesExampleState.NO.equals(finishesExample.get().getState())) )
                    table.addCell(getKreuzelCell(true));
                else
                    table.addCell(getKreuzelCell(false));
            }
        }

        document.add(table);
    }

    private  void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private PdfPCell getKreuzelCell(boolean checked)
    {
        PdfPCell cell = new PdfPCell(new Phrase(checked?"X":" "));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return cell;
    }

    private PdfPCell getTableHeaderCell(String headerText) {

        PdfPCell cell;
        cell = new PdfPCell(new Phrase(headerText, normalFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private String getLocaleMessage(String code)
    {
        return resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
