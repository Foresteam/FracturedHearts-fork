#!/usr/bin/env python3
"""Build the Fabric mod and standalone datapack into releases/."""

import argparse
import json
import shutil
import subprocess
import sys
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parent


def version():
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        key, separator, value = line.partition("=")
        if separator and key.strip() == "mod_version":
            return value.strip()
    raise ValueError("mod_version is missing from gradle.properties")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--config", type=Path, help="JSON settings to bake into the datapack")
    parser.add_argument("--offline", action="store_true", help="Use Gradle's cached dependencies")
    args = parser.parse_args()
    mod_version = version()
    releases = ROOT / "releases"
    releases.mkdir(exist_ok=True)

    command = [str(ROOT / "gradlew"), "--no-daemon", "build"]
    if args.offline:
        command.append("--offline")
    subprocess.run(command, cwd=ROOT, check=True)

    built_jar = ROOT / "build/libs" / f"fractured-hearts-fabric-{mod_version}.jar"
    if not built_jar.is_file():
        raise FileNotFoundError(f"Gradle did not produce {built_jar}")
    with ZipFile(built_jar) as archive:
        metadata = json.loads(archive.read("fabric.mod.json"))
        if metadata["version"] != mod_version:
            raise ValueError(f"JAR version {metadata['version']} does not match {mod_version}")
    released_jar = releases / built_jar.name
    shutil.copy2(built_jar, released_jar)

    datapack = releases / f"fractured-hearts-datapack-{mod_version}.zip"
    pack_command = [sys.executable, str(ROOT / "scripts/pack_datapack.py"), "--output", str(datapack)]
    if args.config:
        pack_command.extend(["--config", str(args.config.resolve())])
    subprocess.run(pack_command, cwd=ROOT, check=True)
    print(f"Releases: {released_jar}\n          {datapack}")


if __name__ == "__main__":
    main()
