# KOTH Plugin - Implementation Details

## Overview
This document provides technical details about the KOTH (King of the Hill) plugin implementation for Minecraft Paper 1.21.8.

## Architecture

### Component Structure

#### Main Plugin Class (`KOTH.java`)
- Entry point for the plugin
- Initializes all managers in proper order
- Handles plugin lifecycle (enable/disable)
- Provides access to all manager instances

#### Managers

**KOTHManager** - Core game logic
- Manages KOTH event state (active/inactive)
- Tracks player time in zone using HashMap<UUID, Long>
- Implements capture logic: only the player with the longest time in zone captures
- Runs a BukkitTask every second (20 ticks) to check capture progress
- Handles automatic scheduling of next KOTH event
- Manages announcement system at configured intervals

**RegionManager** - Region handling
- Stores region data (min/max coordinates for x, y, z)
- Provides position selection system for operators
- Handles region persistence to config.yml
- Contains point-in-region collision detection

**BossBarManager** - Visual display
- Creates and manages Bukkit BossBar with WHITE color and SOLID style
- Updates boss bar title and progress every second
- Automatically adds joining players to active boss bar
- Handles boss bar cleanup on KOTH end

**RewardManager** - Reward system
- Executes console commands for rewards
- Supports %player% placeholder in reward commands
- Handles reward configuration persistence

**ConfigManager** - Configuration
- Loads and reloads config.yml
- Provides color code translation (& to §)
- Caches frequently accessed values
- Handles message formatting with prefix

#### Models

**KOTHRegion** - Region data model
- Immutable region boundaries
- Efficient contains() check using min/max bounds
- Serialization to/from ConfigurationSection
- World-aware collision detection

#### Commands

**KOTHCommand** - Command handler
- Implements CommandExecutor and TabCompleter
- All commands require `koth.admin` permission (OP by default)
- Provides comprehensive help system
- Tab completion for subcommands

#### Listeners

**PlayerMoveListener** - Event handling
- Adds joining players to active boss bar
- Lightweight listener for player join events

## Key Algorithms

### Capture Determination Algorithm

```java
1. Every second (20 ticks):
2. For each online player:
   a. If player is in region:
      - Increment their time counter by 1 second
   b. If player is not in region:
      - Remove them from tracking (reset to 0)
3. Find player with highest time counter
4. That player becomes the "capturing player"
5. Update boss bar with their progress
6. If progress >= capture time (60s default):
   - End KOTH with that player as winner
```

### Scheduling Algorithm

```java
1. When KOTH ends:
2. Calculate next KOTH time = current time + interval (60 minutes default)
3. For each announcement time in config:
   a. If announcement time < interval:
      - Schedule task at (interval - announcement) minutes
4. Schedule actual KOTH start at full interval
```

## Configuration System

### Config.yml Structure
- **region**: World name and min/max coordinates (pos1, pos2)
- **capture-time**: Seconds required to win (default: 60)
- **event-interval**: Minutes between KOTH events (default: 60)
- **announcements**: List of minutes before start to announce
- **reward**: Command to execute and enabled flag
- **messages**: All user-facing messages with color code support

### Persistence
- Region is saved immediately when set via /koth setregion
- Reward is saved immediately when set via /koth setreward
- Other config values persist across reloads
- No runtime state persistence (intentional - clean start on server restart)

## Technical Decisions

### Why Track Time Instead of Continuous Presence
The plugin tracks cumulative time in zone rather than continuous presence. This means:
- If Player A is in zone for 30 seconds, leaves, then Player B enters for 31 seconds, Player B wins
- This encourages active defense and creates more dynamic gameplay
- Progress is COMPLETELY reset when leaving (not paused)

### Why One Capturing Player at a Time
- Only the player with the longest cumulative time in zone captures
- Other players in zone accumulate time but don't show on boss bar
- This creates a clear "hill holder" mechanic
- Prevents confusing multiple progress bars

### Boss Bar Color Choice
- WHITE color chosen for maximum visibility
- SOLID style (not segmented) for clear progress indication
- Black text on white background provides good contrast
- Matches Minecraft's standard UI conventions

### Permission System
- Single `koth.admin` permission for all commands
- Defaults to OP (operators only)
- Simple and secure - no granular permissions needed
- Can be overridden by permission plugins if needed

## Event Flow

### Starting a KOTH Event
1. Check if region is set
2. Set active = true
3. Clear all player time trackers
4. Broadcast start message
5. Create white boss bar
6. Start 20-tick repeating task for capture checks

### During Active KOTH
1. Every second, check all online players
2. Increment time for players in region
3. Reset time for players not in region
4. Determine player with most time
5. Update boss bar with their progress
6. Check if anyone reached capture time

### Ending a KOTH Event
1. Cancel capture check task
2. Identify winner (if any)
3. Broadcast end message
4. Execute reward command for winner
5. Remove boss bar
6. Clear all trackers
7. Schedule next KOTH event

### Automatic Scheduling
1. Calculate next start time (current + interval)
2. Schedule announcement tasks at appropriate times
3. Schedule actual start task at full interval
4. Manual start cancels scheduled start

## Code Quality Features

### Error Handling
- Null checks for player lookups
- World validation in region creation
- Region existence checks before KOTH start
- Graceful handling of missing configuration values

### Resource Management
- BukkitTasks properly canceled in shutdown()
- Boss bars removed on disable
- No memory leaks from event listeners
- Efficient map cleanup with removeIf()

### Extensibility
- Manager pattern allows easy feature additions
- Configuration-driven behavior
- Clean separation of concerns
- Each component has single responsibility

## Performance Considerations

### Optimization Strategies
- Region check uses simple boundary comparison (O(1))
- Player iteration only over online players
- Boss bar updates only when values change
- Configuration values cached in ConfigManager
- No database queries or file I/O during gameplay

### Resource Usage
- One repeating task during active KOTH (20 tick interval)
- One scheduled task for next KOTH
- Multiple scheduled tasks for announcements (short-lived)
- One boss bar instance maximum
- HashMap for player tracking (O(1) lookups)

## Testing Recommendations

### Manual Testing Checklist
1. Set region with /koth setregion
2. Configure reward with /koth setreward
3. Start KOTH with /koth start
4. Enter region and verify boss bar appears
5. Stay for 60 seconds and verify win
6. Leave region mid-capture and verify reset
7. Have multiple players enter and verify longest time wins
8. Wait for automatic KOTH and verify scheduling
9. Check announcements appear at correct times
10. Verify reward is given to winner
11. Test reload command preserves settings
12. Test status command shows correct information

### Edge Cases to Test
- Player leaves server while capturing
- Server restart during active KOTH
- No players in region for entire event
- Multiple players with equal time
- Region spanning world border
- Invalid reward commands
- Missing configuration values

## Future Enhancement Ideas

### Potential Features
- Multiple concurrent KOTH zones
- Team-based KOTH
- Custom boss bar colors per KOTH
- Leaderboard system
- Integration with economy plugins
- Particle effects in region
- Sound effects for capture milestones
- Holographic displays
- API for other plugins

### API Design
If exposing an API:
```java
public interface KOTHApi {
    boolean isKothActive();
    UUID getCapturingPlayer();
    int getCaptureProgress();
    void startKoth();
    void stopKoth();
    void registerKothStartListener(Consumer<KothStartEvent> listener);
    void registerKothEndListener(Consumer<KothEndEvent> listener);
}
```

## Security Considerations

### Permission Checks
- All commands verify `koth.admin` permission
- Default permission level is OP only
- Console can execute all commands without checks

### Input Validation
- Region positions validated for same world
- Configuration values have sensible defaults
- Player existence checked before operations

### Command Execution
- Reward commands executed as console (safe)
- No player input directly executed
- %player% placeholder properly sanitized

## Maintenance Notes

### When Updating Minecraft Version
1. Update pom.xml dependency version
2. Check BossBar API compatibility
3. Verify BukkitScheduler behavior
4. Test region collision detection
5. Rebuild and test thoroughly

### When Adding Features
1. Consider impact on capture mechanics
2. Update configuration system if needed
3. Document new messages in config.yml
4. Update README.md with new commands
5. Test interaction with existing features

### Common Issues
- **Boss bar not showing**: Check if players joined after KOTH started
- **Capture not progressing**: Verify player is in configured region
- **Region not saving**: Check config.yml file permissions
- **Commands not working**: Verify OP status or permission plugin config

## Credits

This plugin was designed and implemented according to specifications for Paper 1.21.8 using Java 22 and Maven.
