package com.example.e4.rcp.todo.toolcontrols;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.ILocaleChangeService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.example.e4.rcp.todo.i18n.MessagesRegistry;

public class SwitchLanguageToolControl {

	@Inject
	ILocaleChangeService lcs;

	@Inject
	public SwitchLanguageToolControl(Composite parent, MessagesRegistry messagesRegistry) {

		parent.setLayout(new GridLayout(10, false));
		final Text input = new Text(parent, SWT.BORDER);
		input.setLayoutData(new GridData(200, -1));

		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
					lcs.changeApplicationLocale(input.getText());
				}
			}
		});

		Button button = new Button(parent, SWT.PUSH);
		messagesRegistry.register(button::setText, m -> m.toolbar_main_changelocale);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				lcs.changeApplicationLocale(input.getText());
			};
		});
	}
}
