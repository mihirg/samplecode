package in.gore.ui;

import java.util.List;

import in.gore.Field;
import in.gore.Record;
import in.gore.XSDTest;
import in.gore.ui.model.RecordLabelProvider;
import in.gore.ui.model.RecordProvider;
import in.gore.ui.model.TreeModel;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.wb.swt.SWTResourceManager;

public class MainApplication extends ApplicationWindow {
	private TableViewer tableViewer;
	private TreeViewer treeViewer;

	/**
	 * Create the application window.
	 */
	public MainApplication() {
		super(null);
		setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}
	
	public void parseFile(String file) {
		List<Record> recordList = XSDTest.getRecordList(file);
		TreeModel model = new TreeModel(recordList);
		treeViewer.setInput(model);
		
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		{
			treeViewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
			treeViewer.setContentProvider(new RecordProvider());
			treeViewer.setLabelProvider(new RecordLabelProvider());
			Tree elementTree = treeViewer.getTree();
			elementTree.setLinesVisible(true);
			GridData gd_elementTree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_elementTree.heightHint = 165;
			elementTree.setLayoutData(gd_elementTree);
			treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					MainApplication.this.selectionChange(event.getSelection());
				}
			});

		}
		{
			tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
			Table table = tableViewer.getTable();
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			addColumns();
		}
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("");
		menuManager.setVisible(true);
		MenuManager fileMenu = new MenuManager("&File");
		fileMenu.setVisible(true);
		menuManager.add(fileMenu);
		fileMenu.add(new Action("&Open File") {
			public void run() {
				FileDialog fileDialog = new FileDialog(MainApplication.this.getShell());
			    fileDialog.setText("Select File");
			    fileDialog.setFilterExtensions(new String[] { "*.xsd" });
			    String selected = fileDialog.open();
			    MainApplication.this.parseFile(selected);
			}			
		});
		
		fileMenu.add(new Action("Open &Url") {
			public void run() {
				
			}
		});
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			MainApplication window = new MainApplication();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setImage(SWTResourceManager.getImage(MainApplication.class, "/sun/print/resources/duplex.png"));
		super.configureShell(newShell);
		newShell.setText("XSD TreeViewer");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	public void selectionChange(Object srcObject) {
		if (srcObject instanceof TreeSelection) {
			TreeSelection s = (TreeSelection)srcObject;
			Object obj = s.getFirstElement();
			if (obj instanceof Record) {
				Record record = (Record)obj;
				try {
					List<Field> fldList = record.getFields();
					tableViewer.setContentProvider(new ArrayContentProvider());
					tableViewer.setInput(fldList.toArray());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	private void addColumns() {
		TableViewerColumn name = new TableViewerColumn(tableViewer, SWT.NONE);		
		name.getColumn().setMoveable(true);
		name.getColumn().setText("Field Name");
		name.getColumn().setResizable(true);
		name.getColumn().setWidth(100);
		name.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Field) {
					Field f = (Field)element;
					return f.getName();
				}
				return null;
			}
			
		});


		TableViewerColumn dataType = new TableViewerColumn(tableViewer, SWT.NONE);		
		dataType.getColumn().setMoveable(true);
		dataType.getColumn().setText("Data Type");
		dataType.getColumn().setResizable(true);
		dataType.getColumn().setWidth(100);
		dataType.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Field) {
					Field f = (Field)element;
					return f.getType();
				}
				return null;
			}
			
		});

	}



}
