package com.nridev.vaadinshop.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

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
            AtomicInteger rowNum = new AtomicInteger(0);
            StreamSupport.stream(sheet.spliterator(), false)
            	.forEach(row -> {
                if (rowNum.getAndIncrement() != 0) {
                    String name = dataFormatter.formatCellValue(row.getCell(0));
                    BigDecimal price = new BigDecimal(dataFormatter.formatCellValue(row.getCell(1)).replace(",", "."));
                    repo.save(new Product(name, price));
                }
            });
            
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
            AtomicInteger rowNum = new AtomicInteger(0);
            List<ProductPurchase> productPurchases = new ArrayList<>();
            StreamSupport.stream(sheet.spliterator(), false)
        	.forEach(row -> {
	            if (rowNum.getAndIncrement() != 0) {
	                String name = dataFormatter.formatCellValue(row.getCell(0));
	                Integer quantity = new Integer(dataFormatter.formatCellValue(row.getCell(1)));
	                Product product = repo.findByName(name);
	                productPurchases.add(new ProductPurchase(product, quantity));
                }
            });
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

            AtomicInteger r = new AtomicInteger(1);
            AtomicReference<BigDecimal> total = new AtomicReference<BigDecimal>(BigDecimal.ZERO);
            productPurchases.forEach(p -> {
                Row row2 = sheet.createRow(r.getAndAdd(1));

                Cell cell2 = row2.createCell(0);
                cell2.setCellValue(p.getProduct().getName());
                cell2 = row2.createCell(1);
                cell2.setCellValue(p.getQuantity());
                cell2 = row2.createCell(2);
                BigDecimal subtotal = p.getProduct().getPrice().multiply(new BigDecimal(p.getQuantity()));
                cell2.setCellValue(subtotal.doubleValue());
                total.compareAndSet(total.get(), total.get().add(subtotal));
            });
            
            //Total row
            row = sheet.createRow(r.addAndGet(2));
            cell = row.createCell(0);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue("TOTAL");
            cell.setCellStyle(headerCellStyle);
            cell = row.createCell(2);
            cell.setCellValue(total.get().doubleValue());

            // Resize all columns to fit the content size
            IntStream.rangeClosed(0, 7)
            	.forEach(sheet::autoSizeColumn);

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
