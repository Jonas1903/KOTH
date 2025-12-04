# KOTH - King of the Hill Plugin

A complete King of the Hill Minecraft plugin for Paper 1.21.8 using Java 22 and Maven.

## Features

- **Automatic KOTH Events**: Starts automatically every hour after the last KOTH ended
- **Region System**: Define a custom KOTH area with position-based selection
- **Capture Mechanics**: 
  - 60-second capture timer (configurable)
  - Only one player can capture at a time (whoever has been in the area longest)
  - Timer resets completely if player leaves the area
- **Boss Bar Display**: Global white boss bar showing capture progress
- **Rewards**: Configurable rewards via console commands
- **Chat Announcements**: Automatic announcements at 45, 30, 20, 10, 5, and 1 minute before KOTH starts
- **Persistent Configuration**: All settings survive server restarts

## Requirements

- Paper Minecraft 1.21.8 (or compatible version)
- Java 22
- Maven (for building)

## Installation

1. Download or build the plugin JAR file
2. Place the JAR in your server's `plugins` folder
3. Restart the server
4. Configure the plugin using the commands below

## Commands

All commands require OP permission (`koth.admin`).

- `/koth start` - Manually start a KOTH event
- `/koth stop` - Stop the current KOTH event
- `/koth setpos1` - Set the first corner of the KOTH region
- `/koth setpos2` - Set the second corner of the KOTH region
- `/koth setreward` - Set the reward to the item currently held in your hand
- `/koth reload` - Reload the configuration
- `/koth status` - Show current KOTH status
- `/koth help` - Show all available commands

## Configuration

The `config.yml` file is generated on first run and contains:

```yaml
# Region settings
region:
  world: "world"
  pos1: {x: 0, y: 0, z: 0}
  pos2: {x: 0, y: 0, z: 0}

# Capture time in seconds
capture-time: 60

# Interval between KOTH events (in minutes)
event-interval: 60

# Announcement times before KOTH starts (in minutes)
announcements:
  - 45
  - 30
  - 20
  - 10
  - 5
  - 1

# Reward configuration (item will be serialized here when set)
reward:
  enabled: true
  item: {}
```

## Building from Source

```bash
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Project Structure

```
KOTH/
├── pom.xml                          # Maven configuration
├── src/
│   └── main/
│       ├── java/
│       │   └── com/koth/plugin/
│       │       ├── KOTH.java                    # Main plugin class
│       │       ├── commands/
│       │       │   └── KOTHCommand.java         # Command handler
│       │       ├── listeners/
│       │       │   └── PlayerMoveListener.java  # Event listener
│       │       ├── managers/
│       │       │   ├── KOTHManager.java         # Game logic
│       │       │   ├── RegionManager.java       # Region handling
│       │       │   ├── RewardManager.java       # Reward system
│       │       │   └── BossBarManager.java      # Boss bar display
│       │       ├── models/
│       │       │   └── KOTHRegion.java          # Region data model
│       │       └── utils/
│       │           └── ConfigManager.java       # Configuration handling
│       └── resources/
│           ├── plugin.yml           # Plugin metadata
│           └── config.yml           # Default configuration
└── README.md
```

## Usage Example

1. Set up the KOTH region:
   ```
   /koth setpos1
   (move to opposite corner)
   /koth setpos2
   ```

2. Set a reward (hold the item you want to give as a reward):
   ```
   (hold item in hand)
   /koth setreward
   ```

3. Start KOTH manually or wait for automatic start:
   ```
   /koth start
   ```

4. Players fight in the region to capture for 60 seconds
5. Winner receives the configured reward
6. Next KOTH automatically scheduled for 1 hour later

## How It Works

### Capture Mechanics
- Players in the KOTH region accumulate time
- The player who has been in the region the longest becomes the "capturing" player
- Only one player can capture at a time
- If a player leaves the region, their progress is completely reset
- If a player is hit by another player while capturing, their progress is completely reset
- First player to reach 60 seconds (configurable) wins

### Reward System
- Rewards are item-based (not command-based)
- Hold the item you want to give as a reward in your hand
- Use `/koth setreward` to set it as the KOTH reward
- The item (including enchantments, name, lore, etc.) is saved in the config
- Winners receive an exact copy of the reward item in their inventory

### Boss Bar
- Displays globally to all online players
- Shows the current capturing player and their progress
- White bar with black text for optimal visibility
- Updates every second during active KOTH

### Scheduling
- After a KOTH ends, the next one is automatically scheduled
- Announcements are sent at configurable intervals
- Manual start with `/koth start` cancels the schedule

## Support

For issues, questions, or contributions, please visit the GitHub repository.

## License

This plugin is provided as-is for use on Minecraft servers.