package com.aau.moodle20.services;

import com.aau.moodle20.constants.ECourseRole;
import com.aau.moodle20.constants.EFinishesExampleState;
import com.aau.moodle20.entity.*;
import com.aau.moodle20.exception.ServiceValidationException;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.persistence.UniqueConstraint;
import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfService extends AbstractService {

    private final Integer EXAMPLE_NUMBER_TO_SWITCH_TO_LANDSCAPE = 7;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    public PdfService(ResourceBundleMessageSource resourceBundleMessageSource) {
        this.resourceBundleMessageSource = resourceBundleMessageSource;
    }


    protected byte[] createAttendanceList(Course course) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);
        Paragraph titleParagraph = getTitleParagraph(getLocaleMessage("attendanceList.title"));
        titleParagraph.setFont(font).setFontSize(16);
        titleParagraph.setBold();
        doc.add(titleParagraph);
        doc.add(getAttendanceTable(course));
        doc.close();

        return baos.toByteArray();
    }

    protected byte[] createKreuzelList(ExerciseSheet exerciseSheet) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        long exampleNumber = exerciseSheet.getExamples().stream()
                .filter(example -> example.getSubExamples().isEmpty())
                .count();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);
        if (exampleNumber > EXAMPLE_NUMBER_TO_SWITCH_TO_LANDSCAPE)
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

        Paragraph titleParagraph = getTitleParagraph(getLocaleMessage("kreuzelList.title"));
        titleParagraph.setFont(font).setFontSize(16);
        titleParagraph.setBold();
        doc.add(titleParagraph);
        doc.add(getKreuzelListTable(exerciseSheet));
        doc.close();

        return baos.toByteArray();
    }

    protected byte[] createExerciseSheet(ExerciseSheet exerciseSheet) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdfDoc);
        doc.setLeftMargin(50);
        doc.setRightMargin(50);

        // add title
        Paragraph titleParagraph = getTitleParagraph(exerciseSheet.getName());
        titleParagraph.setFont(font).setFontSize(16);
        titleParagraph.setBold();
        doc.add(titleParagraph);

        // add first line
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Table table = new Table(2);
        table.addCell(getCell(getLocaleMessage("exerciseSheet.issueDate") + ": " + exerciseSheet.getIssueDate().format(dateFormatter), TextAlignment.LEFT));
        table.addCell(getCell(exerciseSheet.getTotalPoints() + " " + getLocaleMessage("exerciseSheet.points"), TextAlignment.RIGHT));
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setWidth(UnitValue.createPercentValue(100));
        doc.add(table);

        addEmptyLinesToDocument(doc, 1);
        addHtmlToPdfDocument(exerciseSheet.getDescription(), doc);
        addEmptyLinesToDocument(doc, 1);
        List<Example> sortedExamples = exerciseSheet.getExamples().stream()
                .filter(example -> example.getParentExample() == null)
                .sorted(Comparator.comparing(Example::getOrder)).collect(Collectors.toList());
        for (Example example : sortedExamples) {
            if (example.getSubExamples().isEmpty()) {
                addExampleHeader(example.getName(), "(" + example.getPoints() + " " + getLocaleMessage("exerciseSheet.points") + ")", doc);
                addHtmlToPdfDocument(example.getDescription(), doc);
                addEmptyLinesToDocument(doc, 1);
            } else {
                addExampleHeader(example.getName(), "(" + example.getPoints() + " " + getLocaleMessage("exerciseSheet.points") + ")", doc);
                addHtmlToPdfDocument(example.getDescription(), doc);
                addEmptyLinesToDocument(doc, 1);
                List<Example> subExamples = example.getSubExamples().stream().sorted(Comparator.comparing(Example::getOrder)).collect(Collectors.toList());
                for (Example subExample : subExamples) {
                    addExampleHeader(subExample.getName(), "(" + subExample.getPoints() + " " + getLocaleMessage("exerciseSheet.points") + ")", doc);
                    addHtmlToPdfDocument(subExample.getDescription(), doc);
                    addEmptyLinesToDocument(doc, 1);
                }
            }
        }


        doc.close();

        return baos.toByteArray();
    }

    protected void addEmptyLinesToDocument(Document document, Integer number) {
        Paragraph emptyLine = new Paragraph();
        emptyLine.setFontSize(12);
        addEmptyLine(emptyLine, number);
        document.add(emptyLine);
    }

    protected void addHtmlToPdfDocument(String html, Document document) {
        List<IElement> elements = HtmlConverter.convertToElements(html);
        for (IElement element : elements) {
            element.setProperty(Property.OVERFLOW_X, OverflowPropertyValue.FIT);
            document.add((IBlockElement) element);
        }
    }

    protected void addExampleHeader(String leftText, String rightText, Document document) {
        Table table = new Table(2);
        table.addCell(getCell(leftText, TextAlignment.LEFT).setFontSize(13).setBold());
        table.addCell(getCell(rightText, TextAlignment.RIGHT));
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);
        document.add(table);
    }

    protected Cell getCell(String text, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(text));
        cell.setPadding(0);
        cell.setTextAlignment(alignment);
        cell.setBorder(Border.NO_BORDER);
        cell.setFontSize(11);
        return cell;
    }

    protected Paragraph getTitleParagraph(String text) throws IOException {

        Paragraph preface = new Paragraph();
        preface.setTextAlignment(TextAlignment.CENTER);
        // Lets write a big header
        Paragraph paragraph = new Paragraph(text);
        paragraph.setHorizontalAlignment(HorizontalAlignment.CENTER);
        preface.add(paragraph);

        addEmptyLine(preface, 3);

        return preface;
    }


    protected Table getAttendanceTable(Course course) {

        String forename = getLocaleMessage("attendanceList.forename");
        String surname = getLocaleMessage("attendanceList.surname");
        String matriculationNumber = getLocaleMessage("attendanceList.matriculationNumber");
        String signature = getLocaleMessage("attendanceList.signature");

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 22, 22, 31}));
        table.setFixedLayout();
        table.setWidth(UnitValue.createPercentValue(95));
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addHeaderCell(createHeaderCell(matriculationNumber));
        table.addHeaderCell(createHeaderCell(surname));
        table.addHeaderCell(createHeaderCell(forename));
        table.addHeaderCell(createHeaderCell(signature));

        Comparator<UserInCourse> comparatorSurname = Comparator.comparing(userInCourse -> userInCourse.getUser().getSurname());
        Comparator<UserInCourse> comparatorForename = Comparator.comparing(userInCourse -> userInCourse.getUser().getForename());
        Comparator<UserInCourse> comparatorMatriculationNumber = Comparator.comparing(userInCourse -> userInCourse.getUser().getMatriculationNumber());


        List<UserInCourse> userInCourses = course.getStudents().stream()
                .sorted(comparatorSurname.thenComparing(comparatorForename).thenComparing(comparatorMatriculationNumber))
                .collect(Collectors.toList());

        for (UserInCourse userInCourse : userInCourses) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            table.addCell(createCell(userInCourse.getUser().getMatriculationNumber(), TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getSurname(), TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getForename(), TextAlignment.LEFT));
            table.addCell(createCell("", TextAlignment.LEFT));
        }

        return table;
    }

    protected Cell createCell(String content, TextAlignment textAlignment) {
        Cell cell = new Cell();
        cell.add(new Paragraph(content));
        cell.setTextAlignment(textAlignment);
        return cell;
    }

    protected Cell createHeaderCell(String content) {
        Cell cell = createCell(content, TextAlignment.LEFT);
        cell.setBold();
        return cell;
    }

    protected Table getKreuzelListTable(ExerciseSheet exerciseSheet) {

        String forename = getLocaleMessage("kreuzelList.forename");
        String surename = getLocaleMessage("kreuzelList.surname");
        String matriculationNumber = getLocaleMessage("kreuzelList.matriculationNumber");

        long exampleNumber = exerciseSheet.getExamples().stream()
                .filter(example -> example.getSubExamples().isEmpty())
                .count();

        List<Example> exampleList = exerciseSheet.getExamples().stream()
                .filter(example -> example.getParentExample() == null)
                .sorted(Comparator.comparing(Example::getOrder))
                .collect(Collectors.toList());

        List<Example> examplesInTable = new ArrayList<>();

        float[] widths = new float[(int) (exampleNumber + 3)];
        Arrays.fill(widths, (float) 1);
        widths[0] = (float) 5;
        widths[1] = (float) 5;
        widths[2] = (float) 5;

        int absoluteTableWidth = 0;
        for (float width : widths) {
            absoluteTableWidth = (int) (absoluteTableWidth + (width * 25));
        }

        Table table = new Table(widths);
        table.setFixedLayout();
        table.setWidth(absoluteTableWidth);
        table.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // add header cells
        table.addHeaderCell(createHeaderCell(matriculationNumber));
        table.addHeaderCell(createHeaderCell(surename));
        table.addHeaderCell(createHeaderCell(forename));


        for (Example example : exampleList) {
            if (example.getSubExamples().isEmpty()) {
                table.addHeaderCell(createHeaderCell(Integer.toString(example.getOrder() + 1)).setTextAlignment(TextAlignment.CENTER));
                examplesInTable.add(example);
            } else {
                List<Example> subExamples = example.getSubExamples().stream().sorted(Comparator.comparing(Example::getOrder))
                        .collect(Collectors.toList());
                for (Example subExample : subExamples) {
                    String headerText = (example.getOrder() + 1) + "." + (subExample.getOrder() + 1);
                    table.addHeaderCell(createHeaderCell(headerText).setTextAlignment(TextAlignment.CENTER));
                    examplesInTable.add(subExample);
                }
            }
        }

        Comparator<UserInCourse> comparatorSureName = Comparator.comparing(userInCourse -> userInCourse.getUser().getSurname());
        Comparator<UserInCourse> comparatorForeName = Comparator.comparing(userInCourse -> userInCourse.getUser().getForename());
        Comparator<UserInCourse> comparatorMatriculationNumber = Comparator.comparing(userInCourse -> userInCourse.getUser().getMatriculationNumber());

        List<UserInCourse> sortedUserInCourse = exerciseSheet.getCourse().getStudents().stream()
                .sorted(comparatorSureName.thenComparing(comparatorForeName).thenComparing(comparatorMatriculationNumber))
                .collect(Collectors.toList());

        for (UserInCourse userInCourse : sortedUserInCourse) {

            if (!ECourseRole.Student.equals(userInCourse.getRole()))
                continue;

            table.addCell(createCell(userInCourse.getUser().getMatriculationNumber(), TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getSurname(), TextAlignment.LEFT));
            table.addCell(createCell(userInCourse.getUser().getForename(), TextAlignment.LEFT));

            for (Example example : examplesInTable) {
                Optional<FinishesExample> finishesExample = example.getExamplesFinishedByUser().stream()
                        .filter(finishesExample1 -> finishesExample1.getUser().getMatriculationNumber().equals(userInCourse.getUser().getMatriculationNumber()))
                        .findFirst();

                if (finishesExample.isPresent() && !(EFinishesExampleState.NO.equals(finishesExample.get().getState())))
                    table.addCell(getKreuzelCell(true));
                else
                    table.addCell(getKreuzelCell(false));
            }
        }

        return table;
    }


    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Text("\n"));
        }
    }

    private Cell getKreuzelCell(boolean checked) {
        String text = checked ? "X" : " ";
        return createCell(text, TextAlignment.CENTER);
    }

    private String getLocaleMessage(String code) {
        return resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

    public ByteArrayInputStream generateKreuzelList(Long exerciseSheetId) throws ServiceValidationException, IOException {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        return new ByteArrayInputStream(createKreuzelList(exerciseSheet));
    }

    public ByteArrayInputStream generateExerciseSheetDocument(Long exerciseSheetId) throws ServiceValidationException, IOException {
        ExerciseSheet exerciseSheet = readExerciseSheet(exerciseSheetId);
        return new ByteArrayInputStream(createExerciseSheet(exerciseSheet));
    }

    public ByteArrayInputStream generateCourseAttendanceList(Long courseId) throws ServiceValidationException, IOException {
        Course course = readCourse(courseId);
        return new ByteArrayInputStream(createAttendanceList(course));
    }
}
