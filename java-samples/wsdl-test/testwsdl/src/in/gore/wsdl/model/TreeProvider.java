package in.gore.wsdl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.WSDLElement;
import javax.xml.namespace.QName;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeProvider implements ITreeContentProvider {
	
	private IModel model;
	
	public TreeProvider(IModel model) {
		this.model = model;
	}

	@Override
	public Object[] getChildren(Object inputElement) {
		if (inputElement instanceof Binding) {
			Binding bind = (Binding)inputElement;
			return bind.getBindingOperations().toArray();
		} else if (inputElement instanceof BindingOperation) {
			BindingOperation bindOp = (BindingOperation)inputElement;
			Operation op = bindOp.getOperation();
			Input ip = op.getInput();
			Output output = op.getOutput();
			Map faults = op.getFaults();
		
			List<WSDLElement> ret = new ArrayList<WSDLElement>();
			ret.add(ip);
			ret.add(output);
			ret.addAll(faults.values());
			return ret.toArray();
		} else if (inputElement instanceof Input) {
			Message msg = ((Input)inputElement).getMessage();
			Object[] ret = new Object[1];
    		ret[0] = msg;
    		return ret;
		} else if (inputElement instanceof Output) {
			Message msg = ((Output)inputElement).getMessage();
			Object[] ret = new Object[1];
    		ret[0] = msg;
    		return ret;
		} else if (inputElement instanceof Fault) {
			Message msg = ((Fault)inputElement).getMessage();
			Object[] ret = new Object[1];
    		ret[0] = msg;
    		return ret;
		} else if (inputElement instanceof Message) {
			Map outParts = ((Message)inputElement).getParts();
			List<Part> outList = new ArrayList<Part>(outParts.values());
			return outList.toArray();			
		}else if (inputElement instanceof Part) {
			Part part = (Part)inputElement;
			QName tname = part.getElementName();
			Record ipRecord = null;
			if (model instanceof TreeModel) {				
	    		ipRecord = TreeModel.recordMap.get(tname);
			} else {
				NewTreeModel ntm = (NewTreeModel)model;
				ipRecord=  ntm.getRecordForElement(tname);
			}
    		Object[] ret = new Object[1];
    		ret[0] = ipRecord;
    		return ret;					

		} else if (inputElement instanceof Record) {
			return ((Record)inputElement).getChildren().toArray();
		}
	
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object inputElement) {
		if (inputElement instanceof Binding) {
			Binding bind = (Binding)inputElement;
			List bindOps = bind.getBindingOperations();
			return bindOps.size() > 0;
		} else if (inputElement instanceof BindingOperation) {
			return true;
		}else if (inputElement instanceof Input) {
			return true;
		}else if (inputElement instanceof Output) {
			return true;
		}else if (inputElement instanceof Fault) {
			return true;
		} else if (inputElement instanceof Part) {
			return true;
		} else if (inputElement instanceof Record) {
			return ((Record)inputElement).getChildren().size() > 0;
		} else if (inputElement instanceof Message) {
			return true;
		}
			
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TreeModel) {
			TreeModel model = (TreeModel)inputElement;
			return model.getAllBinding().toArray();
		} else if (inputElement instanceof NewTreeModel) {
			NewTreeModel model = (NewTreeModel)inputElement;
			return model.getAllBinding().toArray();
		}

		
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

}
