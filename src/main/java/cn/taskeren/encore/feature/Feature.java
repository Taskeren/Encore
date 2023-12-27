package cn.taskeren.encore.feature;

import cn.taskeren.encore.Encore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public abstract class Feature implements Listener {

	public final String name;
	public final Encore encore;

	private boolean enabled = false;

	public Feature(final String name, final Encore encore) {
		this.name = name;
		this.encore = encore;
		init();
	}

	// region Getters and Setters

	protected final Encore getEncore() {
		return encore;
	}

	protected final FileConfiguration getConfig() {
		return getEncore().getConfig();
	}

	protected final Boolean isEnabledInConfig(boolean defaultValue) {
		return getConfig().getBoolean("feature." + name, defaultValue);
	}

	protected final void setEnabledInConfig(boolean enabled) {
		getConfig().set("feature." + name, enabled);
		getEncore().saveConfig();
	}

	protected final Logger getLogger() {
		var encoreLoggerName = getEncore().getLogger().getName();
		return LogManager.getLogManager().getLogger(encoreLoggerName + "|" + name);
	}

	protected final org.slf4j.Logger getSLF4JLogger() {
		var encoreLoggerName = getEncore().getSLF4JLogger().getName();
		return LoggerFactory.getLogger(encoreLoggerName + "|" + name);
	}

	// endregion

	// region Initialization Helpers

	protected final void registerListener() {
		getEncore().getServer().getPluginManager().registerEvents(this, getEncore());
	}

	protected final void loadIsEnabledFromConfiguration(boolean defaultValue) {
		setEnabled(isEnabledInConfig(defaultValue));
	}

	// endregion

	protected void init() {
		// no-op
	}

	public final void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			this.enabled = enabled;
			onEnableChanged(enabled);
		}
	}

	public final boolean isEnabled() {
		return enabled;
	}

	protected void onEnableChanged(boolean newValue) {
		// no-op
	}

	public void onEncoreEnabled() {
		// no-op
	}

	public void onEncoreDisabled() {
		// no-op
	}

	public void onEncoreReload() {
		// no-op
	}

}
