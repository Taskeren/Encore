# Encore Plugin

This plugin is developed for my friend's MMORPG server, with some customizable features.

## Installation

**Notification:** this plugin is only allowed to be used on non-commercial servers with credits to the authors, without
my permissions.

**Notification:** this plugin is only tested on Paper 1.19.2, and it perhaps only works on it because of NMSs.
**Please don't report any bugs on other platforms and versions.**

1. Download the latest version of the plugin from [here](https://github.com/Taskeren/Encore/releases/latest).
2. Download ProtocolLib from [here](https://hangar.papermc.io/dmulloy2/ProtocolLib).
3. Drag them into plugins.

## Features

### Ignore Anvil Repair Cost Limit

Normally, the vanilla anvil will limit the repair cost to 39 levels, and give "Too Expensive" text when it is over the
limit.

If this feature is enabled, the anvil repair cost limit will be removed (technically set to Int.MAX_VALUE), and to fix
the "Too Expensive" text (even though the limit is removed, the text will still be shown), players will be set to
creative mode "virtually" by sending fake gamemode change packet, and their gamemode will be fixed (resync-ed) after
they close the menu.

### Blocking Mending Enchantment

Blocking the Mending enchantment to be applied on items by anvil.

The blocked items are configurable in `config.yml` at path `feature.block-mending-materials` as a Material list.
Default it is an empty list, which blocks nothing.

### *To Be Added*

<!-- TODO -->

## Commands

### `/encore reload-config`

Reload the configurations for features. It will only reload the extra configurations, not include the enabling status.

### `/encore feature <feature-id> [true/false]`

To read or set the enabling status of a feature.
