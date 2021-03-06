package org.kyffa.models;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.kyffa.general.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;


public class FFAExcelFile {
    private XSSFSheet sheet;
    private XSSFRow row;
    private XSSFCell cell;
    private InputStream file;
    private Iterator rows;
    private Iterator cells;
    private Chapter currentChapter;
    private Student student;

    /*
    Basic Constructor that takes a File and a org.kyffa.models.Chapter object.
    This constructor initializes all necessary variables.
    Currently sheet is declared as in instance variable of this class. Eventually this will
    be used to open different sheets and process data from more than one.
    */
    public FFAExcelFile(File excelFile, Chapter currentChapter) throws IOException {
        this.file = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(this.file);
        this.sheet = workbook.getSheetAt(2);
        this.rows = sheet.rowIterator();
        this.currentChapter = currentChapter;
        this.student = new Student();
    }

    public boolean hasNextRow() {
        return this.rows.hasNext();
    }

    public void setRow() {
        this.row = (XSSFRow)this.rows.next();
    }

    /*
    Check and see if the program is in bounds. This could need expanding
    in the future if the excel form is expanded.
    */
    public boolean isValidRow() {
        if(this.row.getRowNum() == 0 || this.row.getRowNum() == 1) {
            return false;
        } else if(this.row.getRowNum() > 41) {
            return false;
        } else {
            return true;
        }
    }

    public void setCellIterator() {
        this.cells = this.row.cellIterator();
    }

    public boolean hasNextCell() {
        return this.cells.hasNext();
    }

    public void setCell() {
        this.cell = (XSSFCell)cells.next();
    }

    /*
    Splits the name by spaces and stores in an array. If there is a length of 3
    then it is a 3 word name. if a length of two then it is a two word name.

    There is probably a better way to approach this. With the current way the excel
    document is set up, with only one field for first name and last name, this is
    the best option at the time.
    */
    public void setName() {
        String[] splitNames = cell.getStringCellValue().split(" ");
        if(splitNames.length == 3) {
            this.student.setFirstName(splitNames[0] + " " + splitNames[1]);
            this.student.setLastName(splitNames[2]);
        } else if(splitNames.length == 2) {
            this.student.setFirstName(splitNames[0]);
            this.student.setLastName(splitNames[1]);
        }
    }

    public void setGender() {
        this.student.setGender(cell.getStringCellValue());
    }

    /*
    An array is indexed to see if the office being added is a committee.
    The reason this happens is because the committee has a different field
    inside of FileMaker but the field for committee and office are the same
    in the excel document. Therefore some sort of flag needs to be thrown to
    indicate a committee chair is being processed and not a normal officer.
    */
    public void setOffice() {
        for(String comm : Main.committees) {
            if(cell.getStringCellValue().equals(comm)) {
                this.student.setIsCommittee(true);
            }
        }
        this.student.setOffice(cell.getStringCellValue());
    }

    public void setSpecialInterest() {
        if(this.cell.getCellStyle().getFont().getBold()) {
            this.setSpecialInterestAM();
        } else if(cell.getCellStyle().getFont().getItalic()) {
            this.setSpecialInterestPM();
        }
    }

    public void addAdvisors() {
        this.sheet = this.sheet.getWorkbook().getSheetAt(0);
        for(int i = 8; i <= 11; i++) {
            CellReference cellReference = new CellReference("D" + i);
            this.row = sheet.getRow(cellReference.getRow());
            this.cell = row.getCell(cellReference.getCol());
            String[] splitNames = this.cell.getStringCellValue().split(" ");
            if(splitNames.length == 3) {
                this.currentChapter.addStudent(new Student(splitNames[0] + splitNames[1], splitNames[2]));
            } else if(splitNames.length == 2) {
                this.currentChapter.addStudent(new Student(splitNames[0], splitNames[1]));
            }
        }
    }

    public void addMaleChaperones() {
        CellReference cellReference = new CellReference("D15");
        this.row = sheet.getRow(cellReference.getRow());
        this.cell = row.getCell(cellReference.getCol());
        for(int i = 0; i < (int)this.cell.getNumericCellValue(); i++) {
            this.currentChapter.addStudent(new Student("Male", "Chaperone", "Chaperone", "M"));
        }
    }

    public void addFemaleChaperones() {
        CellReference cellReference = new CellReference("E15");
        this.row = sheet.getRow(cellReference.getRow());
        this.cell = row.getCell(cellReference.getCol());
        for(int i = 0; i < (int)this.cell.getNumericCellValue(); i++) {
            this.currentChapter.addStudent(new Student("Female", "Chaperone", "Chaperone", "F"));
        }
    }

    public void addMaleChildren() {
        CellReference cellReference = new CellReference("D17");
        this.row = sheet.getRow(cellReference.getRow());
        this.cell = row.getCell(cellReference.getCol());
        for(int i = 0; i < (int)this.cell.getNumericCellValue(); i++) {
            this.currentChapter.addStudent(new Student("Male", "Child", "Child", "M"));
        }
    }

    public void addFemaleChildren() {
        CellReference cellReference = new CellReference("D16");
        this.row = sheet.getRow(cellReference.getRow());
        this.cell = row.getCell(cellReference.getCol());
        for(int i = 0; i < (int)this.cell.getNumericCellValue(); i++) {
            this.currentChapter.addStudent(new Student("Female", "Child", "Child", "F"));
        }
    }

    /*
    There are two special interest classes, AM and PM.
    The AM is indicated on the excel form as a bold text area.
    There are also two Communication classes. Communications B
    is marked by an underline.

    This helper method to setSpecialInterest checks to see if
    the interest class is Communication skills and if underlined the
    student is put into Communication Skills B, if not then they are
    put into Communication Skills A. If not of these conditions are met,
    then the other classes are added.
    */
    private void setSpecialInterestAM() {
        if(this.cell.getStringCellValue().equals("Communication Skills")
                && this.cell.getCellStyle().getFont().getUnderline() == 1) {
            this.student.setSpecialInterestAM("Communication Skills B");
        } else if(this.cell.getStringCellValue().equals("Communication Skills")) {
            this.student.setSpecialInterestAM("Communication Skills A");
        } else {
            this.student.setSpecialInterestAM(this.cell.getStringCellValue());
        }
    }

    /*
   There are two special interest classes, AM and PM.
   The PM is indicated on the excel form as an italic text area.
   There are also two Communication classes. Communications B
   is marked by an underline.

   This helper method to setSpecialInterest checks to see if
   the interest class is Communication skills and if underlined the
   student is put into Communication Skills B, if not then they are
   put into Communication Skills A. If not of these conditions are met,
   then the other classes are added.
   */
    private void setSpecialInterestPM() {
        if(this.cell.getStringCellValue().equals("Communication Skills")
                && this.cell.getCellStyle().getFont().getUnderline() == 1) {
            this.student.setSpecialInterestPM("Communication Skills B");
        } else if(this.cell.getStringCellValue().equals("Communication Skills")) {
            this.student.setSpecialInterestPM("Communication Skills A");

        } else {
            this.student.setSpecialInterestPM(cell.getStringCellValue());
        }
    }

    public void setGroupNumber() {
        this.student.setGroupNum((int)(Math.random() * 8) + 1);
    }

    public void addStudent() {
        this.currentChapter.addStudent(this.student);
        this.student = new Student();
    }
    public int getCellColumn() {
        return this.cell.getColumnIndex();
    }

    public Chapter getCurrentChapter() {
        return this.currentChapter;
    }

    public void closeInputStream() {
        try {
            this.file.close();
        } catch(IOException e) {
            System.out.println("File could not be closed");
        }
    }
}
