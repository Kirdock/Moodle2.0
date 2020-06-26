package com.aau.moodle20.component;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PdfHelper {

//    private PdfFont headFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//    private PdfFont exampleFont = FontFactory.get
//    private PdfFont normalFont = FontFactory.getFont(FontFactory.HELVETICA,13);

    private final Integer EXAMPLE_NUMBER_TO_SWITCH_TO_LANDSCAPE = 7;

    private ResourceBundleMessageSource resourceBundleMessageSource;

    public PdfHelper(ResourceBundleMessageSource resourceBundleMessageSource)
    {
        this.resourceBundleMessageSource = resourceBundleMessageSource;
    }


    public byte [] createAttendanceList(Course course) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);
        Paragraph titleParagraph = getTitleParagraph("attendanceList.title");
        titleParagraph.setFont(font).setFontSize(16);
        titleParagraph.setBold();
        doc.add(titleParagraph);
        doc.add(getAttendanceTable(course));
        doc.close();

        return baos.toByteArray();
    }

    public byte [] createKreuzelList(ExerciseSheet exerciseSheet) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        long exampleNumber = exerciseSheet.getExamples().stream()
                .filter(example -> example.getSubExamples().isEmpty())
                .count();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);
        if (exampleNumber > EXAMPLE_NUMBER_TO_SWITCH_TO_LANDSCAPE)
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

        Paragraph titleParagraph = getTitleParagraph("kreuzelList.title");
        titleParagraph.setFont(font).setFontSize(16);
        doc.add(titleParagraph);
        doc.add(getKreuzelListTable(exerciseSheet));
        doc.close();

        return baos.toByteArray();
    }

    public Paragraph getTitleParagraph(String titleCode) throws IOException {
        String headerText = getLocaleMessage(titleCode);

        Paragraph preface = new Paragraph();
        preface.setTextAlignment(TextAlignment.CENTER);
        // Lets write a big header
        Paragraph paragraph = new Paragraph(headerText);
        paragraph.setHorizontalAlignment(HorizontalAlignment.CENTER);
        preface.add(paragraph);

        addEmptyLine(preface, 3);

        return preface;
    }


    public Table  getAttendanceTable( Course course)  {

        String forename = getLocaleMessage("attendanceList.forename");
        String surname = getLocaleMessage("attendanceList.surname");
        String matriculationNumber = getLocaleMessage("attendanceList.matriculationNumber");
        String signature = getLocaleMessage("attendanceList.signature");

        Table table = new Table(UnitValue.createPercentArray(new float[]{22,22,25,31}));
        table.setFixedLayout();
        table.setWidth(UnitValue.createPercentValue(95));
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addHeaderCell(createHeaderCell(forename));
        table.addHeaderCell(createHeaderCell(surname));
        table.addHeaderCell(createHeaderCell(matriculationNumber));
        table.addHeaderCell(createHeaderCell(signature));


        for (UserInCourse userInCourse : course.getStudents()) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            table.addCell(createCell(userInCourse.getUser().getForename(),TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getSurname(),TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getMatriculationNumber(),TextAlignment.LEFT));
            table.addCell(createCell("",TextAlignment.LEFT));
        }

        return table;
    }
    protected Cell createCell(String content, TextAlignment textAlignment)
    {
        Cell cell = new Cell();
        cell.add(new Paragraph(content));
        cell.setTextAlignment(textAlignment);
        return cell;
    }

    protected Cell createHeaderCell(String content)
    {
        Cell cell = createCell(content,TextAlignment.LEFT);
        cell.setBold();
        return cell;
    }

    public Table  getKreuzelListTable(ExerciseSheet exerciseSheet)  {

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

        Table table = new Table(UnitValue.createPercentArray((int) (exampleNumber+2)));
        table.setAutoLayout();
        table.setWidth(UnitValue.createPercentValue(90));

//        Table table = new PdfPTable((int) (exampleNumber + 2));
//        table.setWidthPercentage(95);
//        float [] widths = new float[(int) (exampleNumber+2)];
//        Arrays.fill(widths, (float) 0.2);
//        widths[0] = (float) 1;
//        widths[1] = (float) 1;
//        table.setWidths(widths);
//        table.setHeaderRows(1);

        table.addHeaderCell(createCell(name,TextAlignment.LEFT));
        table.addHeaderCell(createCell(matriculationNumber,TextAlignment.LEFT));

        for (Example example : exampleList) {
            if (example.getSubExamples().isEmpty()) {
                table.addHeaderCell(createCell(Integer.toString(example.getOrder() + 1), TextAlignment.LEFT));
                examplesInTable.add(example);
            } else {
                List<Example> subExamples = example.getSubExamples().stream().sorted(Comparator.comparing(Example::getOrder))
                        .collect(Collectors.toList());
                for (Example subExample : subExamples) {
                    String headerText = (example.getOrder() + 1) + "." + (subExample.getOrder() + 1);
                    table.addHeaderCell(createCell(headerText, TextAlignment.LEFT));
                    examplesInTable.add(subExample);
                }
            }
        }

        for (UserInCourse userInCourse : exerciseSheet.getCourse().getStudents()) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            String studentName = userInCourse.getUser().getForename() +" "+ userInCourse.getUser().getSurname();
            table.addCell(createCell(studentName,TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getMatriculationNumber(),TextAlignment.LEFT));

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

       return table;
    }



//    private void addExampleHeading(Paragraph paragraph, Example example)
//    {
//        addEmptyLine(paragraph,1);
//
//
//        Paragraph heading = new Paragraph((example.getOrder()+1) +" "+example.getName(), exampleFont);
//
//
//        preface.add(paragraph);
//
//    }

//    public PdfPCell getCell(String text, Ele alignment)
//    {
//        PdfPCell cell = new PdfPCell().addElement(new Paragraph(text));
//    }


    private  void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Text("\n"));
        }
    }

    private Cell getKreuzelCell(boolean checked) {
        String text = checked ? "X" : " ";
        return createCell(text, TextAlignment.CENTER);
    }

    private String getLocaleMessage(String code)
    {
        return resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }



    //    private PdfFont headFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//    private PdfFont exampleFont = FontFactory.get
//    private PdfFont normalFont = FontFactory.getFont(FontFactory.HELVETICA,13);
}
