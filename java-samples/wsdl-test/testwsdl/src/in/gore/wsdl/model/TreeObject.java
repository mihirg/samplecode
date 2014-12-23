package in.gore.wsdl.model;

import javax.wsdl.BindingOperation;

public class TreeObject {
	
	private BindingOperation bindingOp;
	private Object object;
	
	public BindingOperation getBindingOperation() {
		return bindingOp;
	}
	
	public void setBindingOp(BindingOperation bindingOp) {
		this.bindingOp = bindingOp;
	}
	
	public void setObject(Object obj) {
		this.object = obj;
	}
	
	public Object getObject() {
		return object;
	}

}
