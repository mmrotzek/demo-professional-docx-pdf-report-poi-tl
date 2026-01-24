package rocks.m2x.demo.test;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility to create a simple invoice template DOCX file for testing.
 * Run this once to generate template_invoice.docx in src/test/resources/templates/
 */
public class CreateInvoiceTemplate {
    public static void main(String[] args) throws IOException {
        String outputPath = "src/test/resources/templates/template_invoice.docx";
        
        // Ensure directory exists
        Files.createDirectories(Paths.get(outputPath).getParent());
        
        try (XWPFDocument document = new XWPFDocument()) {
            // Invoice Header
            XWPFParagraph header = document.createParagraph();
            XWPFRun headerRun = header.createRun();
            headerRun.setBold(true);
            headerRun.setFontSize(20);
            headerRun.setText("INVOICE");
            
            // Invoice Number
            XWPFParagraph invoiceNumber = document.createParagraph();
            invoiceNumber.createRun().setText("Invoice Number: {{invoiceNumber}}");
            
            // Invoice Date
            XWPFParagraph invoiceDate = document.createParagraph();
            invoiceDate.createRun().setText("Date: {{invoiceDate}}");
            
            // Customer Name (simple)
            XWPFParagraph customerNameSimple = document.createParagraph();
            customerNameSimple.createRun().setText("Bill To: {{customerName}}");
            
            // Customer (complex - nested)
            XWPFParagraph customer = document.createParagraph();
            customer.createRun().setText("Bill To: {{customer.name}}");
            
            XWPFParagraph customerAddress = document.createParagraph();
            customerAddress.createRun().setText("Address: {{customer.address.street}}, {{customer.address.city}}, {{customer.address.zip}}");
            
            // Empty line
            document.createParagraph();
            
            // Items Header
            XWPFParagraph itemsHeader = document.createParagraph();
            XWPFRun itemsHeaderRun = itemsHeader.createRun();
            itemsHeaderRun.setBold(true);
            itemsHeaderRun.setText("Items:");
            
            // Items placeholder (will be filled with table/list in actual template)
            XWPFParagraph items = document.createParagraph();
            items.createRun().setText("{{items}}");
            
            // In a real template, items would use poi-tl table/list syntax
            // For now, this is a placeholder
            
            // Empty line
            document.createParagraph();
            
            // Totals
            XWPFParagraph subtotal = document.createParagraph();
            subtotal.createRun().setText("Subtotal: {{subtotal}}");
            
            XWPFParagraph tax = document.createParagraph();
            tax.createRun().setText("Tax: {{tax}}");
            
            XWPFParagraph total = document.createParagraph();
            XWPFRun totalRun = total.createRun();
            totalRun.setBold(true);
            totalRun.setText("Total: {{total}}");
            
            // Notes
            XWPFParagraph notesHeader = document.createParagraph();
            document.createParagraph();
            XWPFRun notesHeaderRun = notesHeader.createRun();
            notesHeaderRun.setBold(true);
            notesHeaderRun.setText("Notes:");
            
            XWPFParagraph notes = document.createParagraph();
            notes.createRun().setText("{{notes}}");
            
            // Save document
            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }
        }
        
        System.out.println("Invoice template created at: " + outputPath);
    }
}
