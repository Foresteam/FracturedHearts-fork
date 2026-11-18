# TODO
1. Restore permadeath behavior: spectator mode is fine for a completely dead player and eassily reversible, unlike scoreboards and player attributes.
2. Recheck defaults, 3 hears should be the minimum, so the gameplay is not completely miserable.
3. Suggest using it together with a partial inventory/xp saver, for a more reasonable alternative (IMHO) to `keepInventory` and grave mods: to make death extremely undesirable, but not devastating.
4. To me, Fabric is actually preferable in any case, as the worst that could happen is everything returns to default, instead of making the world unloadable or locking health at a non-standard value. So the DP is probably rather for non-fabric clients/servers, or unsupported versions.
5. Install and remove instructions seem rather complex, so should be verified and tested.
6. Make the mod configurable through commands as well (why not)

# 💔 Fractured Hearts

Fractured Hearts changes what death costs in Minecraft. By default, dying reduces your maximum health by one heart until you reach the one-heart minimum. Craft and eat enchanted golden apples to earn those hearts back.

For the intended experience, enable Keep Inventory with `/gamerule keep_inventory true`. You keep your items and experience when you die. Losing maximum health still gives you a reason to stay alive.

Both editions target Minecraft 26.3. See the [README](README.md) for build and technical details.

### ❤️ Heart Recovery

- Craft Enchanted Golden Apples to restore lost hearts

![Recipe for enchanted golden apple](https://cdn.modrinth.com/data/cached_images/dca3530806f8d7fe1e337a19d8d90cc87c9fe4ca.png)

### ⚔️ PvP Heart Stealing (Must be enabled first)

- When enabled, killing a player steals their lost hearts
- Hearts go to killer (up to max ceiling)

### ☠️ Permadeath Mode (Must be enabled first)

When enabled and another death would take a player below the minimum health, the **Fabric mod** disconnects the player and blocks rejoining until the mod is removed. This lockout is stored in the mod's world data, so removing the JAR does not leave the player in spectator mode. The **standalone datapack** uses spectator mode instead.

## Install and Remove

**Standalone datapack.** Add the pack to the vanilla single-player world you want to play. It requires no mod loader or mods. The pack and Keep Inventory setting are per world, so your other worlds can keep vanilla death behavior.

**Fabric mod.** For servers and modpacks, the heart penalty is active by default in every world that runs with the mod. Install the JAR with Fabric Loader and Fabric API for Minecraft 26.3. On a dedicated server, install it server-side. Players can join with vanilla clients. Remove any Fractured Hearts datapack from the world before starting it with the mod.

You can safely remove the **Fabric mod** from a world that has only used the Fabric edition of this fork. Stop the server or close the game, remove the JAR, and reopen the world. The mod's temporary health modifier disappears and normal maximum health returns. Heart totals and permadeath lockouts live in a separate world data file. You can delete that file and the mod's config separately.

For a world migrated from the original datapack, use the cleanup command:

1. Back up the world. Players with saved datapack max-health changes must join once to migrate them.
2. While the mod is still installed, run `/fracturedhearts uninstall` as an admin.
3. If it succeeds, stop the server and remove the mod JAR before starting the server again.

On first start, the mod copies original datapack player scores into `world/data/fractured-hearts.json`, imports old scoreboard settings into its config if needed, then deletes its old scoreboard objectives. Saved datapack health bases are restored when each affected player joins. The original datapack did not record the earlier base, so migration assumes vanilla's 20 health points; customized bases that cannot be safely identified need manual cleanup. The uninstall command removes the world data file, removes active modifiers, and restores online players to full health. It refuses to run while datapack cleanup still needs an offline player. Migration from previous fork builds is not supported.

The standalone datapack cannot restore offline players' health on uninstall. See the [README](README.md#standalone-datapack) for its technical limits.

## Default Settings for This Fork

- Max Hearts: 10 (20 health)
- Min Hearts: 1 (2 health)
- Loss per Death: 1 heart
- Gain per Apple: 1 heart
- Starting Hearts: 10 (20 health)
- Permadeath: Disabled
- Heart Steal: Disabled

The original datapack's maximum was 20 hearts (40 health points). On first start, the Fabric mod imports any existing Fractured Hearts scoreboard settings into a new JSON config. Settings absent from the scoreboard use the fork's defaults. If a JSON config already exists, the mod loads its values instead of replacing it. The mod stores player heart totals by UUID in the world data file, rather than in scoreboards or player data.

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

Fabric mod commands:

| Command | Description |
| --- | --- |
| `/fracturedhearts sethearts <player> <health points>` | Set an online player's maximum health (OP) |
| `/fracturedhearts status` | Show your current hearts |
| `/fracturedhearts toggle sounds` | Toggle sound effects |
| `/fracturedhearts toggle messages` | Toggle heart messages |
| `/fracturedhearts toggle particles` | Toggle particle effects |

The original `/function` and `/trigger` commands remain available in the standalone datapack. The Fabric mod does not install those datapack functions or scoreboard triggers.

## Attribution
Based on the [original Modrinth datapack description](https://modrinth.com/datapack/fractured-hearts)
Foresteam maintains this independent fork of [Renthy's original Fractured Hearts datapack](https://modrinth.com/datapack/fractured-hearts), licensed under GNU Lesser General Public License v3.0 only. See [LICENSE](LICENSE) for the license text.

I liked the idea behind the original datapack and forked it to change its defaults and how it behaves on servers and in modpacks.
