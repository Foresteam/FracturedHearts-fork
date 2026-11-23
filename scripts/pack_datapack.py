#!/usr/bin/env python3
"""Build a standalone Minecraft 26.3 datapack from this source tree."""

import argparse
import json
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
OVERRIDES = ROOT / "datapack_overrides"


def project_version():
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        key, separator, value = line.partition("=")
        if separator and key.strip() == "mod_version":
            return value.strip()
    raise ValueError("mod_version is missing from gradle.properties")


DEFAULTS = {
    "maxHealth": 20,
    "minimumHealth": 4,
    "healthPerApple": 2,
    "regularAppleHealthCap": 12,
    "healthLostPerDeath": 2,
    "permadeath": False,
    "pvpHeartSteal": False,
}


def settings(config_path):
    result = DEFAULTS.copy()
    parsed = {}
    if config_path:
        parsed = json.loads(config_path.read_text(encoding="utf-8"))
        if not isinstance(parsed, dict) or set(parsed) - set(result):
            raise ValueError("Config must contain only recognized settings")
        result.update(parsed)
    for key in ("maxHealth", "minimumHealth", "healthPerApple", "regularAppleHealthCap", "healthLostPerDeath"):
        if type(result[key]) is not int:
            raise ValueError(f"{key} must be an integer")
    if "minimumHealth" not in parsed:
        result["minimumHealth"] = min(result["minimumHealth"], result["maxHealth"])
    for key in ("permadeath", "pvpHeartSteal"):
        if type(result[key]) is not bool:
            raise ValueError(f"{key} must be true or false")
    if not 1 <= result["minimumHealth"] <= result["maxHealth"] <= 1024:
        raise ValueError("Require 1 <= minimumHealth <= maxHealth <= 1024")
    for key in ("healthPerApple", "healthLostPerDeath"):
        if not 1 <= result[key] <= result["maxHealth"]:
            raise ValueError(f"{key} must be between 1 and maxHealth")
    if not 0 <= result["regularAppleHealthCap"] <= 1024:
        raise ValueError("regularAppleHealthCap must be between 0 and 1024")
    return result


def default_function(config):
    values = [
        ("ceiling", config["maxHealth"]),
        ("floor", config["minimumHealth"]),
        ("gain_inc", config["healthPerApple"]),
        ("loss_inc", config["healthLostPerDeath"]),
        ("permadeath", int(config["permadeath"])),
        ("heart_steal", int(config["pvpHeartSteal"])),
    ]
    settings = "".join(
        f"scoreboard players set $fractured_hearts.fakeplayer fractured_hearts.{name} {value}\n"
        for name, value in values
    )
    return settings + "function fractured_hearts:options/default_regular_apple_cap\n"


def transformed_function(relative, source, config):
    if relative == "options/default_settings.mcfunction":
        return default_function(config)
    if relative == "options/default_regular_apple_cap.mcfunction":
        return (
            "scoreboard players set #regular_apple_cap fractured_hearts.toggle "
            f"{config['regularAppleHealthCap']}\n"
        )
    if relative == "options/uninstall.mcfunction":
        return ('tellraw @s {"text":"Remove the datapack after resetting affected players. '
                'Offline player health cannot be safely restored by a datapack.","color":"yellow"}\n')
    override = OVERRIDES / relative
    if override.exists():
        return override.read_text(encoding="utf-8")
    if relative == "internals/load.mcfunction":
        return "\n".join(line for line in source.splitlines()
                         if "fractured_hearts.original_" not in line) + "\n"
    if relative in ("internals/initialization.mcfunction", "internals/initialization_set.mcfunction"):
        return "\n".join(line for line in source.splitlines()
                         if "fractured_hearts.original_health" not in line) + "\n"
    if relative == "health_loss/perish.mcfunction":
        return "\n".join(line for line in source.splitlines()
                         if "fracturedhearts markpermadeath" not in line) + "\n"
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", type=Path, help="JSON file whose settings are baked into the datapack")
    parser.add_argument("--output", type=Path,
                        default=ROOT / "releases" / f"fractured-hearts-datapack-{project_version()}.zip")
    args = parser.parse_args()
    config = settings(args.config)
    args.output.parent.mkdir(parents=True, exist_ok=True)

    with ZipFile(args.output, "w", ZIP_DEFLATED) as archive:
        archive.write(RESOURCES / "pack.mcmeta", "pack.mcmeta")
        archive.write(ROOT / "LICENSE", "LICENSE")
        for path in sorted((RESOURCES / "data").rglob("*")):
            if not path.is_file():
                continue
            relative = path.relative_to(RESOURCES).as_posix()
            if relative == "data/fractured_hearts/function/config/sync_health.mcfunction":
                continue
            if path.suffix == ".mcfunction" and relative.startswith("data/fractured_hearts/function/"):
                function = relative.removeprefix("data/fractured_hearts/function/")
                archive.writestr(relative, transformed_function(function, path.read_text(encoding="utf-8"), config))
            else:
                archive.write(path, relative)

    print(f"Packed {args.output}")


if __name__ == "__main__":
    main()
