# Forge - Gamified Productivity Application

## Overview

Forge is a JavaFX desktop application that gamifies productivity by letting users select "modes" (Study, Body Builder, Introvertness Killer, etc.) and complete associated quests to earn XP, coins, and achievements.

---

## Features

### Core Features
- **User System**: Registration and login with secure password hashing
- **Mode Selection**: Choose from different productivity modes
- **Quest System**: Start, complete, or abandon quests
- **XP & Leveling**: Earn XP by completing quests, level up
- **Inventory**: Buy and equip items (cosmetic + functional)
- **Achievements**: Unlock achievements based on progress
- **Custom Modes**: Create your own modes with custom quests

### Modes Available
1. **Study** - Focus and learn with productive tasks
2. **Body Builder** - Fitness and workout challenges
3. **Introvertness Killer** - Push your social boundaries
4. **Creative** - Unleash your artistic side (unlocks at Level 3)
5. **Mindful** - Meditation and mental wellness (unlocks at Level 5)

### Quest System
- Each mode has multiple quests
- Quests have XP and coin rewards
- Penalties for abandoning quests (lose coins)
- Track active, available, and completed quests

### Achievements
- First Quest - Complete your first quest
- Level 5 - Reach level 5
- Level 10 - Reach level 10
- Coin Collector - Earn 100 coins total
- Quest Master - Complete 50 quests
- Wealthy - Have 200 coins at once

---

## Technology Stack

- **Language**: Java 17+
- **UI**: JavaFX 21
- **Database**: MySQL
- **Build Tool**: Maven
- **IDE**: IntelliJ IDEA

---

## Project Structure

```
src/
├── main/
│   ├── java/com/forge/
│   │   ├── Main.java              # Application entry point
│   │   ├── model/                 # Data models
│   │   │   ├── User.java           # User with XP/level
│   │   │   ├── Mode.java           # Game modes
│   │   │   ├── Quest.java          # Quests with rewards
│   │   │   ├── Item.java           # Inventory items
│   │   │   ├── Achievement.java    # Achievements
│   │   │   ├── QuestProgress.java  # Quest progress tracking
│   │   │   └── UserInventory.java # User's owned items
│   │   ├── repository/             # Database access
│   │   ├── service/                # Business logic
│   │   ├── controller/             # JavaFX UI controllers
│   │   └── util/                   # Database utilities
│   └── resources/
│       ├── fxml/                   # UI layouts
│       └── css/                    # Cyberpunk styling
```

---

## Setup Instructions

### Prerequisites
1. **Java JDK 17+** installed
2. **JavaFX SDK 21** downloaded from https://openjfx.io
3. **MySQL** installed and running
4. **IntelliJ IDEA** (recommended)

### Database Setup
- **Host**: localhost:3306
- **Database**: forge (auto-created)
- **Username**: root
- **Password**: Seecs@123

### Running the App

1. Open project in IntelliJ IDEA: `D:\AntiGravity\my-new-project`

2. Create Run Configuration:
   - Main class: `com.forge.Main`
   - VM options: `--module-path "C:\Users\khawa\Downloads\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml`

3. Run the application

The database tables are auto-created on first run.

---

## How to Use

### 1. Registration/Login
- Enter username and password
- Click "REGISTER" to create new account
- Click "LOGIN" to sign in

### 2. Select a Mode
- Click on any available mode card
- Click "View Quests" to see available quests

### 3. Complete Quests
- Click "START QUEST" to begin a quest
- Do the task in real life
- Click "MARK COMPLETE" to earn rewards
- Or click "ABANDON" (with penalty)

### 4. Progress
- Earn XP to level up
- Collect coins from quests
- Buy items in Inventory
- Unlock achievements
- View your achievements in the Achievements tab

---

## Default Data

The app seeds default data on first run:
- 5 modes with quests (Study, Body Builder, Introvertness Killer, Creative, Mindful)
- 6 inventory items (Golden Avatar, Dark Knight Title, Legendary Badge, Coin Boost, Streak Shield, Quest Refresh)
- 6 achievements (First Quest, Level 5, Level 10, Coin Collector, Quest Master, Wealthy)

---

## Troubleshooting

### "Module javafx.controls not found"
- Update JavaFX SDK path in Run Configuration
- Ensure path points to the `lib` folder inside the SDK

### "Access denied for user 'root'"
- Check MySQL password in `DatabaseUtil.java`
- Default: Seecs@123

### Compilation errors
- In IntelliJ: File → Invalidate Caches → Invalidate and Restart
- Then: Build → Rebuild Project

### Database schema issues
- Run: `DROP DATABASE forge;` in MySQL
- Restart the app to recreate tables

---

## Version History

- **v1.0.0** - Initial release with core features:
  - User authentication
  - Mode selection
  - Quest system (start, complete, abandon)
  - XP/Leveling
  - Inventory
  - Achievements
  - Custom mode creation

---

## Project Location

`D:\AntiGravity\my-new-project`