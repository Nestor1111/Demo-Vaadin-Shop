package com.nridev.vaadinshop.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nridev.vaadinshop.entities.Product;
import com.nridev.vaadinshop.entities.ProductPurchase;
import com.nridev.vaadinshop.form.MainForm;
import com.nridev.vaadinshop.repositories.ProductRepository;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route
public class MainView extends MainForm {

	private final ProductRepository repo;
	
	public MainView(ProductRepository repo) {
		this.repo = repo;		

		// Upload prices file
		this.pricesUpload.addSucceededListener(event -> {
		    //event.getMIMEType(),
            //event.getFileName(), 
		    try {
				loadPrices(this.pricesBuffer.getInputStream());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});	

		// Upload purchase file
		this.purchaseUpload.addSucceededListener(event -> {
		    try {
		    	byte[] bFile = this.loadPurchase(this.purchaseBuffer.getInputStream());
				this.downloadAnchor.setHref(getStreamResource("Receipt.xlsx", bFile));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		
	}

	//
	private void loadPrices(InputStream inputStream) throws IOException {
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            int rowNum = 0;
            for (Row row : sheet) {
                if (rowNum++ == 0) {
                    continue;
                }
                String name = dataFormatter.formatCellValue(row.getCell(0));
                BigDecimal price = new BigDecimal(dataFormatter.formatCellValue(row.getCell(1)).replace(",", "."));
                repo.save(new Product(name, price));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
        	inputStream.close();
        }
	}
	
	private byte[] loadPurchase(InputStream inputStream) throws EncryptedDocumentException, IOException {
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            int rowNum = 0;
            List<ProductPurchase> productPurchases = new ArrayList<>();
            for (Row row : sheet) {
                if (rowNum++ == 0) {
                    continue;
                }
                String name = dataFormatter.formatCellValue(row.getCell(0));
                Integer quantity = new Integer(dataFormatter.formatCellValue(row.getCell(1)));
                Product product = repo.findByName(name);
                productPurchases.add(new ProductPurchase(product, quantity));
            }
            return this.generateReceipt(productPurchases);            
        } finally {
        	inputStream.close();
        }
	}
	
	private byte[] generateReceipt(List<ProductPurchase> productPurchases) throws IOException {
		try (Workbook workbook = new XSSFWorkbook()) {
            //CreationHelper createHelper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("Purchase Receipt");

            // Create a Font for styling header cells
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);

            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create a Row
            Row row = sheet.createRow(0);

            // Create cells
            Cell cell = row.createCell(0);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue("Product");
            cell = row.createCell(1);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue("Quantity");
            cell = row.createCell(2);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue("Price");
            cell = row.createCell(3);
            cell.setCellStyle(headerCellStyle);

            int r = 1;
            BigDecimal total = BigDecimal.ZERO;
            for (ProductPurchase p : productPurchases) {
                row = sheet.createRow(r++);

                cell = row.createCell(0);
                cell.setCellValue(p.getProduct().getName());
                cell = row.createCell(1);
                cell.setCellValue(p.getQuantity());
                cell = row.createCell(2);
                BigDecimal subtotal = p.getProduct().getPrice().multiply(new BigDecimal(p.getQuantity()));
                cell.setCellValue(subtotal.doubleValue());
                total = total.add(subtotal);
            }
            
            //Total row
            row = sheet.createRow(r+2);
            cell = row.createCell(0);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue("TOTAL");
            cell.setCellStyle(headerCellStyle);
            cell = row.createCell(2);
            cell.setCellValue(total.doubleValue());

            // Resize all columns to fit the content size
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.close();
            byte[] bFile = bos.toByteArray();
            return bFile;
        }
	}

    public StreamResource getStreamResource(String filename, byte[] bFile) {
        return new StreamResource(filename,
                () -> new ByteArrayInputStream(bFile));
    }

}
