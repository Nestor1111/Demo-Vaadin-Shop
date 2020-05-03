package com.nridev.vaadinshop.form;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

public class MainForm extends VerticalLayout {

	protected final Label pricesLabel;
	protected final Upload pricesUpload;	
	protected final MemoryBuffer pricesBuffer;

	protected final Label purchaseLabel;
	protected final Upload purchaseUpload;	
	protected final MemoryBuffer purchaseBuffer;
	
	protected final Anchor downloadAnchor;
	
	public MainForm() {
		this.pricesLabel = new Label("Prices:");
		this.pricesBuffer = new MemoryBuffer();
		this.pricesUpload = new Upload(this.pricesBuffer);
		
		this.purchaseLabel = new Label("Purchase:");
		this.purchaseBuffer = new MemoryBuffer();
		this.purchaseUpload = new Upload(this.purchaseBuffer);
		
		downloadAnchor = new Anchor("Download Recept");
		downloadAnchor.setTitle("Download Recept");
		downloadAnchor.setText("Download Recept");
		downloadAnchor.getElement().setAttribute("download",true);
		
		// pricesHor layout
		HorizontalLayout pricesHor = new HorizontalLayout();
		pricesHor.add(this.pricesLabel, this.pricesUpload);

		// purchaseHor layout
		HorizontalLayout purchaseHor = new HorizontalLayout();
		purchaseHor.add(this.purchaseLabel, this.purchaseUpload);

		// anchorHor layout
		HorizontalLayout anchorHor = new HorizontalLayout();
		anchorHor.add(this.downloadAnchor);
		
		//general layout
		add(pricesHor, purchaseHor, anchorHor);
	}
	

}
