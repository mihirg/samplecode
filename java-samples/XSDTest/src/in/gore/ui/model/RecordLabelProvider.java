package in.gore.ui.model;



import java.net.MalformedURLException;
import java.net.URL;

import in.gore.ElementType;
import in.gore.Record;
import in.gore.SWTResourceManager;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class RecordLabelProvider extends LabelProvider {
	
	private Image folder = getImage();
	
	public String getText(Object element) {
		if (element instanceof Record) {
			Record rec = (Record)element;
			if (rec.isRootElement() && rec.getElementType() == ElementType.Simple) {
				return rec.getName() + " type: " + rec.getType();
			} else
				return rec.getName();
		}
		return null;
	}
	
	public Image getImage(Object element) {
		return folder;
	}
	
	 // Helper Method to load the images
	  private static Image getImage() {
		  URL url;
		try {
			//return SWTResourceManager.getImage(RecordLabelProvider.class, "/sun/print/resources/oneside.png");
			return SWTResourceManager.getImage(RecordLabelProvider.class, "/download.png");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  } 

}
