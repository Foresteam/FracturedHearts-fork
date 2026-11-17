# Fractured Hearts — Fabric 26.3 port

Source project for a Minecraft 26.3 Fabric port of Renthy's Fractured Hearts 1.0 datapack JAR. The original file is `FracturedHearts_v1_0.jar`. Its mechanics are implemented in readable datapack functions, advancements, and a recipe; there is no compiled original Java code to decompile. Those data files are included with attribution and the original LGPL-3.0-only license. The Fabric initializer adds file-based server configuration.

## Behavior

- Each death reduces maximum health by the configured amount, down to a minimum.
- Consuming an enchanted golden apple raises maximum health by the configured amount, up to the cap.
- Optional permadeath puts players in spectator mode after the final death.
- Optional PvP heart stealing awards the killer health on a player kill.
- The original admin functions, individual sound/message/particle triggers, status display, and enchanted golden apple recipe are included.

## Configuration

On the first server start, the mod writes `config/fractured-hearts.json`:

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

Health values are **health points**, where 2 points equal 1 heart. Edit the file and run `/fracturedhearts reload` or restart the server. Existing player scores are preserved, except that scores above a newly lowered cap are clamped. Remove the old datapack from the world when installing this mod, since both use the same namespace and scoreboard objectives.

The new default cap is 20 health points (10 hearts). An existing `config/fractured-hearts.json` is not overwritten; change its `maxHealth` yourself if it still says 40.

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

The original `/function fractured_hearts:admin/set_ceiling`, `set_gain_increment`, `set_loss_increment`, `toggle_permadeath`, and `toggle_heart_steal` paths now call these commands, so those changes persist too. The other admin functions still manage players' heart values directly.

## Uninstall

Run `/fracturedhearts uninstall` while the mod is installed, then stop the server and remove its JAR before starting again. The command restores recorded original max health and game modes, clears the mod's scoreboard objectives and player tags, and cancels its scheduled functions. The old `/function fractured_hearts:options/uninstall` path calls this command.

The command **refuses to run** if any tracked player is offline, has no recorded original health, or has a max-health value changed by something other than Fractured Hearts. This protects other mods' changes but means players first initialized by older versions must be handled manually. It makes no changes when a preflight check fails. Back up the world before using the command. The server config file remains outside the world and can be deleted separately.

## Standalone datapack

Run `python3 scripts/pack_datapack.py` to create `releases/fractured-hearts-datapack-1.0.1+26.3.zip`. To bake custom defaults into the pack, use `python3 scripts/pack_datapack.py --config config/fractured-hearts.json`. The ZIP uses vanilla admin functions; a standalone datapack cannot read or write a server JSON config at runtime. On an existing world, its baked defaults apply only if Fractured Hearts settings have not already been initialized in that world's scoreboard. The datapack cannot safely restore offline player health on uninstall.

## Build

Requires JDK 25 or later and Gradle's dependencies from Maven. Run `python3 build-all.py` to build both the Fabric JAR and standalone datapack ZIP into `releases/`. Use `python3 build-all.py --offline` when Gradle dependencies are already cached, or `--config path/to/config.json` to bake custom datapack defaults. Install the JAR with Fabric Loader and Fabric API for Minecraft 26.3. The port targets Loader 0.20.1 and Fabric API 0.166.2+26.3.

The copied datapack remains under `src/main/resources/data/fractured_hearts/`. Pack metadata and advancement predicates were updated for Minecraft 26.3. The original `/function fractured_hearts:options/uninstall` cannot disable a Fabric mod and now explains the removal step.

## License and attribution

LGPL-3.0-only; see [LICENSE](LICENSE). The copied datapack functions are by Renthy, whose attribution comments are retained. This port is an independent update and is not an official release by the original author.
