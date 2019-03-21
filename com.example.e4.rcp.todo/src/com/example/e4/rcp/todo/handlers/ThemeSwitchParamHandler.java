package com.example.e4.rcp.todo.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;

public class ThemeSwitchParamHandler {
	@Execute
	public void switchTheme(IThemeEngine engine, @Named("theme_parameter") String themeId) {
		engine.setTheme(themeId, true);
	}
}
