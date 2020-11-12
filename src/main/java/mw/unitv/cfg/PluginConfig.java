package mw.unitv.cfg;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "UnitVisualizer", storages = { @Storage("UnitVisualizer.xml") })
public class PluginConfig implements PersistentStateComponent<PluginConfig> {
	private boolean useLayeredIcons = true;
	private boolean useLayeredIconsOnMethods = true;
	private boolean autoMoveTestClasses = true;
	
	@Nullable
	@Override
	public PluginConfig getState() {
		return this;
	}
	
	@Override
	public void loadState(@NotNull PluginConfig singleFileExecutionConfig) {
		XmlSerializerUtil.copyBean(singleFileExecutionConfig, this);
	}
	
	@Nullable
	public static PluginConfig getInstance(Project project) {
		return ServiceManager.getService(project, PluginConfig.class);
	}
	
	public boolean isUseLayeredIcons() {
		return useLayeredIcons;
	}
	
	public void setUseLayeredIcons(boolean useLayeredIcons) {
		this.useLayeredIcons = useLayeredIcons;
	}
	
	public boolean isAutoMoveTestClasses() {
		return autoMoveTestClasses;
	}
	
	public void setAutoMoveTestClasses(boolean autoMoveTestClasses) {
		this.autoMoveTestClasses = autoMoveTestClasses;
	}

	public boolean isUseLayeredIconsOnMethods() {
		return useLayeredIconsOnMethods;
	}

	public void setUseLayeredIconsOnMethods(boolean useLayeredIconsOnMethods) {
		this.useLayeredIconsOnMethods = useLayeredIconsOnMethods;
	}
}
