# FORGE - Gamified Productivity Application

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=java">
  <img src="https://img.shields.io/badge/JavaFX-26-purple?style=for-the-badge">
  <img src="https://img.shields.io/badge/MySQL-8-orange?style=for-the-badge&logo=mysql">
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache+maven">
</p>

---

## 📱 Overview

**FORGE** is a gamified productivity desktop application built with JavaFX and MySQL. It transforms everyday tasks into exciting quests, allowing users to level up, earn coins, and battle other players while accomplishing real-life goals. Think of it as an RPG meets your to-do list!

### 🎯 Core Concept
- Select productivity **modes** (Study, Body Builder, Social Skills, etc.)
- Complete **quests** to earn XP and coins
- Equip items on your character
- Battle other players in async PvP
- Unlock achievements and climb the leaderboard

---

## ✨ Features

### 🎮 Core Features

| Feature | Description |
|---------|-------------|
| **User System** | Secure registration/login with SHA-256 password hashing |
| **Mode Selection** | 5 built-in modes + custom mode creation |
| **Quest System** | START, COMPLETE, ABANDON with time limits and rewards |
| **XP & Leveling** | Earn XP to level up (50 + level×10 XP per level) |
| **Coin Economy** | Earn and spend coins in the shop |
| **Streak Tracking** | Daily login streaks with bonus rewards |
| **Inventory** | 7 equipment slots + shop system |
| **Achievements** | Auto-unlocking badges based on progress |
| **Battle System** | Async PvP with ranked matches |
| **AI Quests** | AI-generated custom quests using Ollama |

### ⚔️ Battle System

| Aspect | Details |
|--------|---------|
| **Attack Types** | Sword (constant damage), Wand (variable damage) |
| **Defense Types** | Shield (fixed reduction), Wand (varies with attack) |
| **Matchmaking** | Auto-match with similar rank players |
| **Healing** | Potions (buy from shop) + Free daily heal |
| **Points** | Win: +20 Attack / +25 Defense, Lose: -15 / -10 |

### 🏆 Achievements System
- First Quest - Complete your first quest
- Level 5 - Reach level 5
- Level 10 - Reach level 10
- Coin Collector - Earn 100 coins total
- Quest Master - Complete 50 quests
- Wealthy - Have 200 coins at once

### 📋 Available Modes

| Mode | Description | Unlock Level |
|------|-------------|--------------|
| **Study** | Focus and learn with productive tasks | Level 1 |
| **Body Builder** | Fitness and workout challenges | Level 1 |
| **Introvertness Killer** | Push your social boundaries | Level 1 |
| **Creative** | Unleash your artistic side | Level 3 |
| **Mindful** | Meditation and mental wellness | Level 5 |

### 📊 Quest Difficulty Tiers

| Difficulty | Time Limit | XP Reward | Coin Reward |
|------------|------------|-----------|-------------|
| EASY | 12 hours | 30-40 | 15-20 |
| MEDIUM | 24 hours | 40-60 | 20-30 |
| HARD | 72 hours | 60-80 | 30-50 |
| BOSS | 72 hours | 100+ | 50+ |

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 17 |
| **GUI Framework** | JavaFX | 26 |
| **Database** | MySQL | 8.x |
| **AI (Quest Gen)** | Ollama + Llama3 | Local |
| **Build Tool** | Maven | 3.x |
| **Logging** | SLF4J | 2.0.9 |
| **JSON** | Jackson | 2.16.1 |

---

## 🗄️ Database Schema

### Core Tables
- **users** - User accounts, stats, battle info
- **modes** - Productivity categories
- **quests** - Available quests per mode
- **user_progress** - Active/completed quests
- **items** - Shop inventory items
- **user_inventory** - User owned items
- **achievements** - Achievement definitions

### Battle Tables
- **battles** - Battle records
- **battle_actions** - Turn-by-turn actions
- **healing_items** - Potion catalog
- **user_healing_items** - User owned potions

---

## 🚀 Setup Instructions

### Prerequisites

1. **Java JDK 17+**
   - Download from: https://adoptium.net/

2. **JavaFX SDK 26**
   - Download from: https://openjfx.io/
   - Extract to: `C:\Users\YourName\Downloads\javafx-sdk-26\lib`

3. **MySQL 8.x**
   - Install and start MySQL service
   - Default credentials: `root` / `Seecs@123`

4. **Ollama (Optional - for AI Quests)**
   - Download from: https://ollama.com/
   - Run: `ollama pull llama3`

### Database Setup

```sql
-- MySQL will auto-create the database on first run
-- Database: forge (auto-created)
-- Host: localhost:3306
-- Username: root
-- Password: Seecs@123
```

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/kahmedbscs25seecs-bot/ProjectForge.git
   cd ProjectForge
   ```

2. **Open in IntelliJ IDEA**
   - File → Open → Select `D:\AntiGravity\my-new-project`

3. **Create Run Configuration**
   - Main class: `com.forge.Main`
   - VM options: 
     ```
     --module-path "C:\Users\khawa\Downloads\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml
     ```

4. **Run the application**
   - The database tables are auto-created on first run
   - Default data is seeded automatically

---

## 📁 Project Structure

```
src/main/java/com/forge/
├── Main.java                    # Application entry point
├── model/                     # Data models
│   ├── User.java              # User with XP/level/stats/battle
│   ├── Mode.java             # Game modes
│   ├── Quest.java            # Quests with rewards
│   ├── Item.java             # Inventory items
│   ├── Achievement.java     # Achievements
│   ├── Battle.java          # Battle records
│   ├── BattleAction.java    # Turn actions
│   └── HealingItem.java      # Potions
├── repository/               # Database access layer
│   ├── UserRepository.java
│   ├── QuestRepository.java
│   ├── BattleRepository.java
│   └── ... (12 more)
├── service/                 # Business logic
│   ├── UserService.java
│   ├── QuestService.java
│   ├── BattleService.java
│   ├── AIQuestService.java
│   └── ... (6 more)
├── controller/             # JavaFX UI controllers
│   ├── LoginController.java
│   ├── MainController.java
│   ├── BattleController.java
│   └── ... (14 more)
└── util/                   # Utilities
    ├── DatabaseUtil.java
    └── DragUtil.java

src/main/resources/
├── fxml/                   # JavaFX layouts
│   ├── login.fxml
│   ├── main.fxml
│   ├── battle.fxml
│   └── ... (12 more)
└── css/
    └── styles.css          # Cyberpunk theme
```

---

## 🎮 How to Use

### 1. Registration/Login
- Enter username and password
- Click "CREATE ACCOUNT" to register
- Click "ENTER" to login

### 2. Select a Mode
- Click on any mode card (some unlock at higher levels)
- Active modes appear in the top toggle bar

### 3. Complete Quests
- Click "START QUEST" to begin a quest
- Do the task in real life
- Click "COMPLETE" to earn rewards
- Or click "ABANDON" (with coin penalty)

### 4. Progress & Level Up
- Earn XP to level up
- Collect coins from quests
- Buy items in Inventory shop
- Unlock achievements

### 5. Battle System
- Click "Battle" in the header
- Click "FIND OPPONENT" to find a similar-ranked player
- Choose SWORD (constant) or WAND (variable) attack
- Win to gain rank points!

### 6. Heal Up
- Buy potions in the Heal Shop
- Use free daily heal (resets at midnight)

---

## 🎨 UI Features

- **Cyberpunk Theme** - Neon colors (cyan, magenta, gold)
- **Pill-Shaped Buttons** - Modern rounded navigation
- **Drag-to-Move Window** - Custom title bar
- **Animated Splash Screen** - Loading animation
- **Character Display** - Colored equipment visualization
- **Health Bars** - Visual HP and XP tracking

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📝 Version History

| Version | Features |
|---------|----------|
| **v1.0.0** | Initial release with core features |
| **v1.1.0** | Added Battle System, Healing Shop, Leaderboard |
| **v1.2.0** | Added AI Quest Generation (Ollama), Pill Button UI |

---

## ⚠️ Troubleshooting

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

## 📧 Contact

- **GitHub**: https://github.com/kahmedbscs25seecs-bot/ProjectForge
- **Email**: Contact through GitHub

---

<p align="center">
  <strong>Level up your life with FORGE! 🎮</strong>
</p>