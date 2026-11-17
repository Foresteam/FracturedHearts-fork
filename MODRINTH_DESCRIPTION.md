# 💔 Fractured Hearts

Every death shatters your hearts. Recover them with rare apples, or face oblivion.

Fractured Hearts adds a permanent death penalty system to Minecraft — each time you die, you lose hearts. Recover them by crafting expensive enchanted golden apples, or enable permadeath for the ultimate hardcore challenge. Steal hearts from your enemies in PvP, customize everything, and make every decision matter.

### ❤️ Heart Recovery

- Craft Enchanted Golden Apples to restore lost hearts

![Recipe for enchanted golden apple](https://cdn.modrinth.com/data/cached_images/dca3530806f8d7fe1e337a19d8d90cc87c9fe4ca.png)

### ⚔️ PvP Heart Stealing (Must be enabled first)

- When enabled, killing a player steals their lost hearts
- Hearts go to killer (up to max ceiling)

### ☠️ Permadeath Mode (Must be enabled first)

When enabled and a player reaches 0 hearts:

- Player enters spectator mode
- Cannot respawn

## Default Settings

- Max Hearts: 20 (40 health)
- Min Hearts: 1 (2 health)
- Loss per Death: 1 heart
- Gain per Apple: 1 heart
- Starting Hearts: 10 (20 health)
- Permadeath: Disabled
- Heart Steal: Disabled

## Customization & Settings

Admin Commands (OP Required)

| Command | Description |
| --- | --- |
| `/function fractured_hearts:admin/toggle_permadeath` | Enable/disable permadeath server-wide |
| `/function fractured_hearts:admin/toggle_heart_steal` | Enable/disable PvP heart stealing server-wide |
| `/function fractured_hearts:admin/help` | View all admin commands |
| `/function fractured_hearts:admin/set_ceiling {value:40}` | Set max hearts |
| `/function fractured_hearts:admin/set_gain_increment {value:2}` | Hearts per apple |
| `/function fractured_hearts:admin/set_loss_increment {value:2}` | Hearts per death |

Player Commands (No OP Required)

| Command | Description |
| --- | --- |
| `/trigger fractured_hearts_toggle_sounds` | Toggle sound effects on/off |
| `/trigger fractured_hearts_toggle_messages` | Toggle chat broadcasts on/off |
| `/trigger fractured_hearts_toggle_particles` | Toggle particle effects |
| `/trigger fractured_hearts_status` | View current hearts & all settings |

## Other

Can I use this in my modpack?

- Of course!

Isnt this like the other datapacks/mods Life Steal, Limited Lives, etc etc..

- Yes and no. The concept is similar but I plan to expand on the feature set.

Source: [Modrinth description](https://modrinth.com/datapack/fractured-hearts)
