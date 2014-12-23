package in.gore.wsdl.model;

import in.gore.SWTResourceManager;

import java.net.URL;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider extends LabelProvider {
	
	private Image operationImage; 
	private Image bindingImage;
	private Image faultImage;
	private Image inputImage;
	private Image outputImage;
	private Image messageImage;
	private Image partImage;
	private Image xmlGif;
	
	public String getText(Object element) {
		if (element instanceof Binding) {
			Binding bind = (Binding)element;
			return bind.getQName().getLocalPart();
		} else if (element instanceof BindingOperation) {
			BindingOperation bindOp = (BindingOperation)element;
			return bindOp.getName();
		} else if (element instanceof Part) {
			Part part = (Part)element;
			return part.getElementName().getLocalPart();		
		} else if (element instanceof Input) {
			return ((Input)element).getMessage().getQName().getLocalPart();
		} else if (element instanceof Output) {
			return ((Output)element).getMessage().getQName().getLocalPart();
		} else if (element instanceof Fault) {
			return ((Fault)element).getName();
		} else if (element instanceof Record) {
			return ((Record)element).getName();
		} else if (element instanceof Message) {
			return ((Message)element).getQName().getLocalPart();
		}
		
		return null;
	}

	
	public Image getImage(Object element) {
		if (element instanceof BindingOperation) {
			if (operationImage == null)
				operationImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_binding_operation_16x16.gif");
			return operationImage;
		} else if (element instanceof Binding) {
			if (bindingImage == null)
				bindingImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_binding_16x16.gif");
			return bindingImage;
			
		} else if (element instanceof Fault) {
			if (faultImage == null)
				faultImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_fault_16x16.gif");
			return faultImage;			
		} else if (element instanceof Input) {
			if (inputImage == null)
				inputImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_input_16x16.gif");
			return inputImage;
		} else if (element instanceof Output) {
			if (outputImage == null)
				outputImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_output_16x16.gif");
			return outputImage;
		} else if (element instanceof Message) {
			if (messageImage == null)
				messageImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_Message_16x16.gif");
			return messageImage;			
		} else if (element instanceof Part) {
			if (partImage == null)
				partImage = getImage("/in/gore/wsdl/model/Thick_DST_9_WSDL_part_16x16.gif");
			return partImage;			
		} else if (element instanceof Record) {
			if (xmlGif == null)
				xmlGif = getImage("/in/gore/wsdl/model/xml.gif");
			return xmlGif;			
		}


		return null;
	}
	
	 // Helper Method to load the images
	  private Image getImage(String imagePath) {
		  URL url;
		try {
			//return SWTResourceManager.getImage(RecordLabelProvider.class, "/sun/print/resources/oneside.png");
			return SWTResourceManager.getImage(TreeLabelProvider.class, imagePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  } 

}
