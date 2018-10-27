package mw.unitv.cfg;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PluginConfigPanel implements Configurable {
	public static final String DISPLAY_NAME = "UnitVisualizer";
	
	private final PluginConfig pluginConfig;
	
	private JCheckBox layeredIconCheckbox;
	private JCheckBox layeredIconOnMethodsCheckbox;
	private JCheckBox moveTestClassCheckbox;
	
	public PluginConfigPanel(@NotNull Project project) {
		pluginConfig = PluginConfig.getInstance(project);
	}
	
	@Nls
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}
	
	@Nullable
	@Override
	public JComponent createComponent() {
		JPanel panel = new JPanel(new VerticalLayout(10));
		
		layeredIconCheckbox = new JCheckBox("Use layered icons for tested classes", pluginConfig.isUseLayeredIcons());
		panel.add(layeredIconCheckbox);

		layeredIconOnMethodsCheckbox = new JCheckBox("Use layered icons for tested methods", pluginConfig.isUseLayeredIconsOnMethods());
		panel.add(layeredIconOnMethodsCheckbox);
		
		moveTestClassCheckbox = new JCheckBox("Automatically move test classes", pluginConfig.isAutoMoveTestClasses());
		panel.add(moveTestClassCheckbox);
		
		return panel;
	}
	
	@Override
	public boolean isModified() {
		boolean modified = false;
		
		modified |= layeredIconCheckbox.isSelected() != pluginConfig.isUseLayeredIcons();
		modified |= layeredIconOnMethodsCheckbox.isSelected() != pluginConfig.isUseLayeredIconsOnMethods();
		modified |= moveTestClassCheckbox.isSelected() != pluginConfig.isAutoMoveTestClasses();
		
		return modified;
	}
	
	@Override
	public void apply() {
		pluginConfig.setAutoMoveTestClasses(moveTestClassCheckbox.isSelected());
		pluginConfig.setUseLayeredIcons(layeredIconCheckbox.isSelected());
		pluginConfig.setUseLayeredIconsOnMethods(layeredIconOnMethodsCheckbox.isSelected());
	}
}
