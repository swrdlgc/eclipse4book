package com.example.e4.rcp.todo.parts;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.example.e4.rcp.todo.events.MyEventConstants;
import com.example.e4.rcp.todo.i18n.MessagesRegistry;
import com.example.e4.rcp.todo.model.ITodoService;
import com.example.e4.rcp.todo.model.Todo;

public class TodoDeletionPart {
	@Inject
	private ITodoService model;

	@Inject
	private ESelectionService selectionService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	@Inject
	private IEclipseContext ctx;

	private ComboViewer viewer;

	@PostConstruct
	public void createControls(Composite parent, MessagesRegistry messagesRegistry) {
		parent.setLayout(new GridLayout(2, false));
		viewer = new ComboViewer(parent, SWT.READ_ONLY);
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Todo todo = (Todo) element;
				return todo.getSummary();
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		model.getTodos(this::updateViewer);

		Button button = new Button(parent, SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = viewer.getSelection();
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (sel.size() > 0) {
					selectionService.setSelection(sel.toList());
					// assure that "com.example.e4.rcp.todo.remove" is the ID
					// of the command which deletes todos in your application
					// model
					ParameterizedCommand cmd = commandService.createCommand("com.example.e4.rcp.todo.remove", null);
					handlerService.executeHandler(cmd, ctx);
					model.getTodos(TodoDeletionPart.this::updateViewer);
				}
			}
		});
		
		// set button text and register button text locale changes
		messagesRegistry.register(button::setText, m -> m.part_deletion_button_deletetodo);
	}

	private void updateViewer(List<Todo> todos) {
		viewer.setInput(todos);
		if (todos.size() > 0) {
			viewer.setSelection(new StructuredSelection(todos.get(0)));
		}
	}

	@Inject
	@Optional
	private void getNotified(@UIEventTopic(MyEventConstants.TOPIC_TODO_ALLTOPICS) Todo todo) {
		model.getTodos(this::updateViewer);
	}

	@Focus
	public void focus() {
		viewer.getControl().setFocus();
	}
}
