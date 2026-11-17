# 💔 Fractured Hearts

Based on the [original Modrinth datapack description](https://modrinth.com/datapack/fractured-hearts). This fork targets Minecraft 26.3. The standalone datapack is intended for existing vanilla worlds; the Fabric mod is intended for servers and modpacks and adds file-based configuration. See the [README](README.md) for build and technical details.

Every death shatters your hearts. Recover them with rare apples, or face oblivion.

Fractured Hearts adds a permanent death penalty system to Minecraft — each time you die, you lose hearts. Recover them by crafting expensive enchanted golden apples, or enable permadeath for the ultimate hardcore challenge. Steal hearts from your enemies in PvP, customize everything, and make every decision matter.

### ❤️ Heart Recovery

- Craft Enchanted Golden Apples to restore lost hearts

![Recipe for enchanted golden apple](https://cdn.modrinth.com/data/cached_images/dca3530806f8d7fe1e337a19d8d90cc87c9fe4ca.png)

### ⚔️ PvP Heart Stealing (Must be enabled first)

- When enabled, killing a player steals their lost hearts
- Hearts go to killer (up to max ceiling)

### ☠️ Permadeath Mode (Must be enabled first)

When enabled and another death would take a player below the minimum health:

- Player enters spectator mode
- Cannot respawn

## Install and Remove

For an existing vanilla world, use the standalone datapack. For a server or modpack, install the Fabric mod JAR with Fabric Loader and Fabric API for Minecraft 26.3. If this world already has the original Fractured Hearts datapack, remove that datapack before starting with the mod; both use the same namespace and scoreboard objectives. Do not install the standalone datapack and Fabric mod in the same world.

To remove the **Fabric mod** safely:

1. Back up the world and have every player tracked by Fractured Hearts join the server.
2. While the mod is still installed, run `/fracturedhearts uninstall` as an admin.
3. If it succeeds, stop the server and remove the mod JAR before starting the server again. The server config file remains and can be deleted separately.

The uninstall command restores recorded original maximum health and game modes and clears Fractured Hearts data. It stops without changing anything if a tracked player is offline, their original health was never recorded, or another mod has changed their maximum health. Players first initialized by the original datapack may need manual cleanup. The old `/function fractured_hearts:options/uninstall` path calls this command in the Fabric mod.

The standalone datapack cannot safely restore offline players' health on uninstall. See the [README](README.md#standalone-datapack) for its technical limits.

## Default Settings for This Fork

- Max Hearts: 10 (20 health)
- Min Hearts: 1 (2 health)
- Loss per Death: 1 heart
- Gain per Apple: 1 heart
- Starting Hearts: 10 (20 health)
- Permadeath: Disabled
- Heart Steal: Disabled

The original datapack's maximum was 20 hearts (40 health points). On first start, the Fabric mod imports any existing Fractured Hearts scoreboard settings into a new JSON config. Settings absent from the scoreboard use the fork's defaults. If a JSON config already exists, the mod loads its values instead of replacing it.

### Fabric Mod Configuration

On first server start, the Fabric mod creates `config/fractured-hearts.json`. On a new world, it contains these defaults:

```json
{
  "maxHealth": 20,
  "minimumHealth": 2,
  "healthPerApple": 2,
  "healthLostPerDeath": 2,
  "permadeath": false,
  "pvpHeartSteal": false
}
```

These numbers are **health points**: 2 health points equal 1 heart. Edit the file, then run `/fracturedhearts reload` or restart the server. Admin commands also change the settings and save them to the file immediately:

| Command | Description |
| --- | --- |
| `/fracturedhearts ceiling <health points>` | Set the maximum health |
| `/fracturedhearts floor <health points>` | Set the minimum health |
| `/fracturedhearts gain <health points>` | Set health gained per apple |
| `/fracturedhearts loss <health points>` | Set health lost per death |
| `/fracturedhearts permadeath <true\|false\|toggle>` | Set permadeath |
| `/fracturedhearts heartsteal <true\|false\|toggle>` | Set PvP heart stealing |
| `/fracturedhearts reload` | Reload the config file |

The standalone datapack cannot read a server JSON file. Its defaults can be set when the pack is built; see the [README](README.md#standalone-datapack).

## In-Game Commands

The original admin function commands are also available (OP required). In the Fabric mod, the setting commands update the JSON config:

| Command | Description |
| --- | --- |
| `/function fractured_hearts:admin/toggle_permadeath` | Enable/disable permadeath server-wide |
| `/function fractured_hearts:admin/toggle_heart_steal` | Enable/disable PvP heart stealing server-wide |
| `/function fractured_hearts:admin/help` | View all admin commands |
| `/function fractured_hearts:admin/set_ceiling {value:40}` | Set maximum health points |
| `/function fractured_hearts:admin/set_gain_increment {value:2}` | Set health points gained per apple |
| `/function fractured_hearts:admin/set_loss_increment {value:2}` | Set health points lost per death |

Player Commands (No OP Required)

| Command | Description |
| --- | --- |
| `/trigger fractured_hearts_toggle_sounds` | Toggle sound effects on/off |
| `/trigger fractured_hearts_toggle_messages` | Toggle chat broadcasts on/off |
| `/trigger fractured_hearts_toggle_particles` | Toggle particle effects |
| `/trigger fractured_hearts_status` | View current hearts & all settings |

## Attribution

This is an independent fork of [Renthy's original Fractured Hearts datapack](https://modrinth.com/datapack/fractured-hearts), licensed under GNU Lesser General Public License v3.0 only. See [LICENSE](LICENSE) for the license text.
