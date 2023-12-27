package cn.taskeren.encore;

import cn.taskeren.encore.feature.BlockMending;
import cn.taskeren.encore.feature.Feature;
import cn.taskeren.encore.feature.IgnoreAnvilRepairCap;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class Encore extends JavaPlugin implements CommandExecutor, TabCompleter {

	public final IgnoreAnvilRepairCap ignoreAnvilRepairCap = new IgnoreAnvilRepairCap(this);
	public final BlockMending blockMending = new BlockMending(this);
	private final Feature[] features = new Feature[]{
			ignoreAnvilRepairCap,
			blockMending,
	};
	private ProtocolManager protocolManager;

	private final String[] featureNames = Arrays.stream(features).map(x -> x.name).toArray(String[]::new);

	public Encore() {
	}

	@Nullable
	public Feature getFeature(String name) {
		for(Feature feature : features) {
			if(feature.name.equalsIgnoreCase(name)) {
				return feature;
			}
		}
		return null;
	}

	public Stream<Feature> getFeatures() {
		return Arrays.stream(features);
	}

	@NotNull
	public ProtocolManager getProtocolManager() {
		return Objects.requireNonNull(protocolManager, "Too early to access the protocol manager!");
	}

	@Override
	public void onEnable() {
		var thisCommand = getServer().getPluginCommand("encore");
		if(thisCommand != null) {
			thisCommand.setExecutor(this);
			thisCommand.setTabCompleter(this);
		} else {
			getSLF4JLogger().warn("Command encore for this plugin is not registered.");
		}

		protocolManager = ProtocolLibrary.getProtocolManager();

		getFeatures().forEach(Feature::onEncoreEnabled);
	}

	@Override
	public void onDisable() {
		getFeatures().forEach(Feature::onEncoreDisabled);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(args.length > 0) {
			var firstArg = args[0];
			if(firstArg.equalsIgnoreCase("feature")) {
				if(args.length > 1) {
					var secondArg = args[1];
					var feature = getFeature(secondArg);
					if(feature != null) {
						if(args.length > 2) {
							var thirdArg = args[2];
							var thirdArgBoolean = Boolean.parseBoolean(thirdArg);
							feature.setEnabled(thirdArgBoolean);
							sender.sendMessage(Component.text("Updated " + feature.name + " = " + thirdArgBoolean));
						} else {
							sender.sendMessage(Component.text(feature.name + " = " + feature.isEnabled()));
						}
					} else {
						sender.sendMessage(Component.text("Unknown feature: " + secondArg, NamedTextColor.RED));
					}
				} else {
					sender.sendMessage(Component.text("Features: " + String.join(", ", featureNames)));
				}
				return true;
			}
			if(firstArg.equalsIgnoreCase("reload-config")) {
				// showing starting
				sender.sendMessage(Component.text("Reloading configurations", NamedTextColor.GRAY));
				// doing the actual logics
				AtomicInteger exceptionCounterReference = new AtomicInteger(0);
				getFeatures().forEach(feature -> {
					try {
						feature.onEncoreReload();
					} catch(Exception ex) {
						var message = Component.text()
								.append(Component.text("Error reloading configuration for "))
								.append(Component.text(feature.name, NamedTextColor.YELLOW))
								.append(Component.text(": "))
								.append(Component.text(ex.getMessage(), NamedTextColor.RED));
						sender.sendMessage(message);
						exceptionCounterReference.getAndIncrement();
					}
				});
				// showing result
				var exceptionCounter = exceptionCounterReference.get();
				if(exceptionCounter > 0) {
					sender.sendMessage(Component.text("Something went wrong while reloading configurations! See console for details.", NamedTextColor.GOLD));
				} else {
					sender.sendMessage(Component.text("Finished reloading configurations!", NamedTextColor.GREEN));
				}
				return true;
			}
		}
		return super.onCommand(sender, command, label, args);
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if(args.length == 1) {
			return List.of("feature", "reload-config");
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("feature")) {
				return List.of(featureNames);
			}
		} else if(args.length == 3) {
			if(args[0].equalsIgnoreCase("feature")) {
				if(Arrays.stream(featureNames).anyMatch(args[1]::equalsIgnoreCase)) {
					return List.of("true", "false");
				}
			}
		}
		return super.onTabComplete(sender, command, alias, args);
	}

}
