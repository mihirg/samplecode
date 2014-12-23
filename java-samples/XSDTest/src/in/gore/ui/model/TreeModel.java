package in.gore.ui.model;

import in.gore.Record;

import java.util.List;

public class TreeModel {
	private List<Record> recordList;
	
	public TreeModel(List<Record> recordList) {
		this.recordList = recordList;
	}
	
	public List<Record> getRecordList() {
		return recordList;
	}

}
