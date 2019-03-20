package com.example.e4.rcp.todo.parts;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.example.e4.rcp.todo.events.MyEventConstants;
import com.example.e4.rcp.todo.i18n.MessagesRegistry;
import com.example.e4.rcp.todo.model.ITodoService;
import com.example.e4.rcp.todo.model.Todo;

public class TodoOverviewPart {

	private Button loadTableDataButton;
	private TableViewer viewer;

	@Inject
	private ESelectionService selectionService;

	@Inject
	private IEventBroker broker;

	@Inject
	private ITodoService todoService;
	
	private WritableList<Todo> writableList;
	protected String searchString = ""; //$NON-NLS-1$

	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService, MessagesRegistry messageRegistry) {
		parent.setLayout(new GridLayout(2, false));

		loadTableDataButton = new Button(parent, SWT.PUSH);
		// set column text and register column text locale changes
		messageRegistry.register(loadTableDataButton::setText, m -> m.buttonLoadData);
		loadTableDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				broker.post(MyEventConstants.TOPIC_TODOS_CHANGED, new HashMap<String, String>());
			}
		});
		
		Button selectAllButton = new Button(parent, SWT.PUSH);
		selectAllButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		messageRegistry.register(selectAllButton::setText, m -> m.buttonSelectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setSelection(new StructuredSelection(writableList));
			}
		});
		

		Text search = new Text(parent, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);

		// Assuming that GridLayout is used
		search.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		messageRegistry.register(search::setMessage, m -> m.txtSearchMessage);

		// Filter at every keystroke
		search.addModifyListener(e -> {
			Text source = (Text) e.getSource();
			searchString = source.getText();
			// Trigger update in the viewer
			viewer.refresh();
		});

		// SWT.SEARCH | SWT.CANCEL are not supported under Windows 7
		// This does not work under Windows 7
		search.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.detail == SWT.CANCEL) {
					Text text = (Text) e.getSource();
					text.setText(""); //$NON-NLS-1$
					//
				}
			}
		});

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn colSummary = new TableViewerColumn(viewer, SWT.NONE);
		// set column text and register column text locale changes
		messageRegistry.register(colSummary.getColumn()::setText, m -> m.txtSummary);
		colSummary.getColumn().setWidth(100);

		colSummary.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				Todo todo = (Todo) element;
				todo.setSummary(String.valueOf(value));
				viewer.refresh();
			}

			@Override
			protected Object getValue(Object element) {
				Todo todo = (Todo) element;
				return todo.getSummary();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable(), SWT.NONE);
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		TableViewerColumn colDescription = new TableViewerColumn(viewer, SWT.NONE);
		// set column text and register column text locale changes
		messageRegistry.register(colDescription.getColumn()::setText, (m) -> m.txtDescription);
		colDescription.getColumn().setWidth(100);

		// We search in the summary and description field
		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				Todo todo = (Todo) element;
				return todo.getSummary().contains(searchString) || todo.getDescription().contains(searchString);
			}
		});

		viewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = viewer.getStructuredSelection();
			selectionService.setSelection(selection.toList());
		});
		menuService.registerContextMenu(viewer.getControl(), "com.example.e4.rcp.todo.popupmenu.table"); //$NON-NLS-1$
		writableList = new WritableList<>();
		todoService.getTodos(writableList::addAll);
		
		ViewerSupport.bind(viewer, writableList,
				BeanProperties.values(new String[] { Todo.FIELD_SUMMARY, Todo.FIELD_DESCRIPTION }));
	}

	@Inject
	@Optional
	private void subscribeTopicTodoAllTopics(
			@UIEventTopic(MyEventConstants.TOPIC_TODO_ALLTOPICS) Map<String, String> event) {
		todoService.getTodos(todos -> {
			writableList.clear();
			writableList.addAll(todos);
		});
	}

	@Focus
	public void setFocus() {
		loadTableDataButton.setFocus();
	}
}
