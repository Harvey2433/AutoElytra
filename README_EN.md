# Auto Elytra - Automatic Elytra Replacement Mod

[中文](README.md) | [English](README_EN.md)

## Introduction

Auto Elytra is a lightweight Minecraft Fabric mod designed for version 1.20.4. It automatically finds and replaces low-durability elytra from your inventory when the equipped elytra's durability is low, eliminating the need for manual operation.

## Features

- **Automatic Detection**: Real-time monitoring of equipped elytra durability
- **Smart Replacement**: Prioritizes enchanted elytra with relatively higher durability for replacement
- **Client-side Implementation**: All operations are performed client-side only, compatible with both singleplayer and multiplayer servers
- **User-friendly Notifications**: Green chat message on successful replacement, obvious audiovisual alerts on failure
- **Configurable Threshold**: Default trigger at ≤5 durability (adjustable in code)

## Requirements

- Minecraft 1.20.4
- Fabric Loader 0.14.21 or higher
- Fabric API
- Java 17 or higher

## Installation

1. Ensure Fabric Loader is installed
2. Download the latest Auto Elytra mod JAR file
3. Place the JAR file in Minecraft's `mods` folder
4. Launch the game and enjoy automatic elytra replacement

## Usage

### Automatic Replacement Mechanism

When your equipped elytra's durability drops to 5 points or below, the mod will automatically:
1. Search your inventory for available elytra
2. Prioritize enchanted elytra with relatively higher durability
3. If no enchanted elytra are available, select the normal elytra with relatively highest durability
4. Automatically replace your worn/damaged elytra

### Notifications

- **Successful Replacement**: Green message in chat "Elytra durability low, automatically replaced"
- **Failed Replacement**: Red title displayed at the top of the inventory screen "Cannot find replacement elytra, elytra replacement failed!" with rapid experience orb sounds (lasts 5 seconds)

### Alert Mechanism

- When unable to find a replacement elytra and equipped elytra durability is below threshold, triggers audiovisual alert after 1-second delay
- Alert triggers only once, enters silent mode after 5 seconds
- Alert stops immediately when a replacement elytra is found or equipped elytra is no longer below threshold

### Notes

- The mod only replaces elytra with ≤5 durability
- If no elytra are available in inventory, triggers one audiovisual alert then enters silent mode

### Building Instructions

1. Clone the project locally
2. Run the `./gradlew build` command to execute the build
3. After building, the mod file will be located in the `build/libs/` directory

Powered by Maple Bamboo Team