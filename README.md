# Fractured Hearts (fork) — 26.3

This repository is a fork of [Renthy's original Fractured Hearts datapack](https://modrinth.com/datapack/fractured-hearts) for Minecraft 26.3. The standalone datapack is intended for existing vanilla worlds; the Fabric mod is intended for servers and modpacks. For features, player commands, and settings, see [MODRINTH_DESCRIPTION.md](MODRINTH_DESCRIPTION.md).

This fork is based on Renthy's Fractured Hearts 1.0 datapack JAR. The original file is `FracturedHearts_v1_0.jar`. Its mechanics are implemented in readable datapack functions, advancements, and a recipe; there is no compiled original Java code to decompile. Those data files are included with attribution and the original LGPL-3.0-only license. The Fabric mod runs on the server only; clients do not need Fabric.

## Configuration

On the first server start, the mod writes `config/fractured-hearts.json`. On a new world, it contains these defaults:

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

Health values are **health points**, where 2 points equal 1 heart. Edit the file and run `/fracturedhearts reload` or restart the server. Existing player totals are preserved, except that totals above a newly lowered cap are clamped. Remove the old datapack from the world when installing this mod so its functions do not keep running.

The default cap is 20 health points (10 hearts). If the JSON config does not exist, the mod imports any existing Fractured Hearts scoreboard settings into the new file, using fork defaults for missing settings. On later starts, it loads the JSON config without replacing it. If you have a custom config with `maxHealth` set to 40, change it manually to use the fork's 20-point default.

Player totals and preferences are stored by UUID in the world's `data/fractured-hearts.json`, not in player data. At first start, legacy scores are copied into that file before the old Fractured Hearts objectives are removed. The mod uses a transient max-health modifier, which Minecraft does not save to player data. It restores legacy max-health bases and spectator game modes in offline player files when the server's cached name resolves to a UUID and the saved values match the imported scores; unresolved players are migrated when they next join. The original datapack did not save an earlier baseline, so this migration assumes the vanilla 20-point base; unexpected bases are left alone and logged.

The original datapack uses a 40 health point cap; this fork uses 20 by default. Health values in the JSON file and admin commands are health points, with 2 points per heart.

Admin setting commands save to the JSON file immediately:

```text
/fracturedhearts ceiling <health points>
/fracturedhearts floor <health points>
/fracturedhearts gain <health points>
/fracturedhearts loss <health points>
/fracturedhearts permadeath <true|false|toggle>
/fracturedhearts heartsteal <true|false|toggle>
/fracturedhearts reload
/fracturedhearts uninstall
```

The Fabric mod has `/fracturedhearts sethearts <player> <health points>`, `/fracturedhearts status`, and `/fracturedhearts toggle <sounds|messages|particles>`. The copied datapack functions and scoreboard triggers are packaged only in the standalone datapack.

## Uninstall

For worlds that have only run this Fabric build, stop the server and remove the JAR. On restart, its transient modifier is absent and normal max health returns. Existing current health may remain below maximum until the player heals or dies; the next death and respawn restores normal full health. Permadeath is a mod-owned lockout, not a persistent spectator game mode. The world data file and server config remain inert and can be deleted separately. `/fracturedhearts uninstall` removes the world heart data file, removes active modifiers, and immediately restores online players to full health.

Legacy player files are migrated offline when the server can resolve their cached names to UUIDs and verify the saved values. If that fails, those players must join once for migration before removing the mod. These are persistent vanilla player-data changes made by earlier datapack or fork builds; simply deleting the JAR cannot undo an unresolved saved base, even after a later death. `/fracturedhearts uninstall` retries offline migration and refuses to run while any legacy cleanup remains unresolved. Back up the world before migration or uninstall.

## Standalone datapack

Run `python3 scripts/pack_datapack.py` to create `releases/fractured-hearts-datapack-1.0.1+26.3.zip`. To bake custom defaults into the pack, use `python3 scripts/pack_datapack.py --config config/fractured-hearts.json`. The ZIP uses vanilla admin functions; a standalone datapack cannot read or write a server JSON config at runtime. On an existing world, its baked defaults apply only if Fractured Hearts settings have not already been initialized in that world's scoreboard. The datapack cannot safely restore offline player health on uninstall.

## License and attribution

LGPL-3.0-only; see [LICENSE](LICENSE). The copied datapack functions are by [Renthy](https://modrinth.com/datapack/fractured-hearts), whose attribution comments are retained. Foresteam maintains this independent fork; it is not an official release by the original author.

## Build

Requires JDK 25 or later and Gradle's dependencies from Maven. Run `python3 build-all.py` to build both the Fabric JAR and standalone datapack ZIP into `releases/`. Use `python3 build-all.py --offline` when Gradle dependencies are already cached, or `--config path/to/config.json` to bake custom datapack defaults. Install the JAR with Fabric Loader and Fabric API for Minecraft 26.3. The fork targets Loader 0.20.1 and Fabric API 0.166.2+26.3.

The copied datapack remains under `src/main/resources/data/fractured_hearts/`; Gradle excludes its functions and advancements from the Fabric JAR and adds the mod's apple advancement from `fabric_resources/`. The standalone packer uses the copied datapack and vanilla function overrides. Pack metadata and advancement predicates were updated for Minecraft 26.3.
