package in.gore.ui.model;

import in.gore.Record;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class RecordProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object inputElement) {
		if (inputElement instanceof Record) {
		Record rec = (Record)inputElement;
		if (rec.getChildren().size() > 0)
			return rec.getChildren().toArray();
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
		if (inputElement instanceof Record) {
			Record rec = (Record)inputElement;
			return (rec.getChildren().size() > 0);
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TreeModel) {
			TreeModel model = (TreeModel)inputElement;
			return model.getRecordList().toArray();
		} else if (inputElement instanceof Record) {
			Record rec = (Record)inputElement;
			if (rec.getChildren().size() > 0)
				return rec.getChildren().toArray();
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
