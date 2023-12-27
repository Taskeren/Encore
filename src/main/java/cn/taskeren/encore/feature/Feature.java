package cn.taskeren.encore.feature;

import cn.taskeren.encore.Encore;
import cn.taskeren.encore.util.ToBeOverriden;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Feature implements Listener {

	public final String name;
	public final Encore encore;

	private boolean enabled;

	private boolean needRegisterListeners = false;
	private boolean alwaysSyncEnablingStatus = false;

	public Feature(final String name, final Encore encore) {
		this(name, true, encore);
	}

	public Feature(final String name, final boolean enabled, final Encore encore) {
		this.name = name;
		this.encore = encore;
		this.enabled = enabled;
		init();
	}

	// region Getters and Setters

	@NotNull
	public final Encore getEncore() {
		return encore;
	}

	protected final FileConfiguration getConfig() {
		return getEncore().getConfig();
	}

	protected final Logger getLogger() {
		var encoreLoggerName = getEncore().getSLF4JLogger().getName();
		return LoggerFactory.getLogger(encoreLoggerName + "|" + name);
	}

	// endregion

	// region Initialization Helpers

	protected final void registerAsListener() {
		needRegisterListeners = true;
	}

	protected final void syncEnablingStatus() {
		alwaysSyncEnablingStatus = true;
	}

	protected final void loadIsEnabledFromConfiguration(boolean defaultValue) {
		setEnabled(getIsEnabledFromConfig(defaultValue));
	}

	protected final Boolean getIsEnabledFromConfig(boolean defaultValue) {
		return getConfig().getBoolean("feature." + name, defaultValue);
	}

	protected final void setIsEnabledToConfig(boolean enabled) {
		getConfig().set("feature." + name, enabled);
		getEncore().saveConfig();
	}

	// endregion

	public final void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			this.enabled = enabled;
			onEnableChangedInternal(enabled);
		}
	}

	public final boolean isEnabled() {
		return enabled;
	}

	// region to be called internal

	public final void onEncoreEnabledInternal() {
		onEncoreEnabled();
		if(needRegisterListeners) {
			getEncore().getServer().getPluginManager().registerEvents(this, getEncore());
		}
		if(alwaysSyncEnablingStatus) {
			setEnabled(getIsEnabledFromConfig(enabled));
		}
	}

	public final void onEncoreDisabledInternal() {
		onEncoreDisabled();
	}

	public final void onEncoreReloadInternal() {
		onEncoreReload();
	}

	private void onEnableChangedInternal(boolean newValue) {
		onFeatureEnablingStatusChange(newValue);
		if(alwaysSyncEnablingStatus) {
			setIsEnabledToConfig(newValue);
		}
	}

	// endregion

	@ToBeOverriden
	protected void init() {
	}

	@ToBeOverriden
	protected void onEncoreEnabled() {
	}

	@ToBeOverriden
	protected void onEncoreDisabled() {
	}

	@ToBeOverriden
	protected void onEncoreReload() {
	}

	@ToBeOverriden
	protected void onFeatureEnablingStatusChange(boolean newValue) {
	}

}
