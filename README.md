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
| **Inventory** | 7 equipment slots + shop system with stat bonuses |
| **Achievements** | Auto-unlocking badges based on progress |
| **Battle System** | Prepare → Opponent Preview → Battle with loadout |
| **AI Quests** | AI-generated custom quests using Ollama |

### 🛡️ Battle System (Enhanced)

| Aspect | Details |
|--------|---------|
| **PrepareForBattle** | Select wand, equipment, 5 attack + 3 defense spells |
| **Opponent Preview** | View opponent stats before battle |
| **Stat Scaling** | +2 ATK/DEF per player level |
| **Equipment Bonuses** | Helmet, Armor, Gloves, Boots, Accessory with stats |
| **Wand Bonuses** | Different wands have different ATK/DEF bonuses |
| **Default Spells** | Auto-selected if user doesn't choose |
| **Bot Opponent** | AI opponent when no real players available |
| **Animated Battle Log** | Smooth fade in/out messages |

---

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
| **Language** | Java | 17+ |
| **GUI Framework** | JavaFX | 26 |
| **Database** | MySQL | 8.x |
| **AI (Quest Gen)** | Ollama + Llama3 | Local |
| **Build Tool** | Maven | 3.x |

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

2. **Build with Maven**
   ```bash
   mvn clean compile
   ```

3. **Run with Maven**
   ```bash
   mvn javafx:run
   ```
   
   Or open in IntelliJ IDEA:
   - File → Open → Select project folder
   - Main class: `com.forge.Main`
   - VM options: `--module-path "path/to/javafx-sdk-26/lib" --add-modules javafx.controls,javafx.fxml`

4. **First Run**
   - Database tables auto-create on first run
   - Default data seeded automatically

---

## 📁 Project Structure

```
src/main/java/com/forge/
├── Main.java                    # Application entry point
├── model/                     # Data models
│   ├── User.java              # User with XP/level/stats/rank
│   ├── Mode.java             # Game modes
│   ├── Quest.java            # Quests with rewards
│   ├── Item.java             # Inventory items with stat bonuses
│   ├── Spell.java           # Battle spells
│   ├── Wand.java            # Wands with stat bonuses
│   ├── Achievement.java     # Achievements
│   ├── Battle.java          # Battle records
│   ├── BattleAction.java    # Turn actions
│   └── HealingItem.java      # Potions
├── repository/               # Database access layer
├── service/                 # Business logic
│   ├── UserService.java
│   ├── QuestService.java
│   ├── BattleService.java
│   ├── HarryPotterBattleService.java
│   ├── StatCalculationService.java
│   └── AIQuestService.java
├── controller/             # JavaFX UI controllers
│   ├── LoginController.java
│   ├── MainController.java
│   ├── BattleController.java
│   ├── PrepareForBattleController.java
│   ├── OpponentPreviewController.java
│   └── ...
└── util/                   # Utilities
    ├── DatabaseUtil.java
    ├── SceneHelper.java
    └── DragUtil.java

src/main/resources/
├── fxml/                   # JavaFX layouts
│   ├── login.fxml
│   ├── main.fxml
│   ├── battle.fxml
│   ├── prepareForBattle.fxml
│   ├── opponentPreview.fxml
│   └── ...
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

### 4. Battle System (New!)
- Click **Battle** in the header
- **PrepareForBattle**: Select wand + 5 attack + 3 defense spells (or use defaults)
- **OpponentPreview**: Review opponent stats
- **Battle**: Fight and win!

### 5. Progress & Level Up
- Earn XP to level up (+2 ATK/DEF per level)
- Collect coins from quests
- Buy equipment with stat bonuses
- Unlock achievements

### 6. Heal Up
- Buy potions in the Heal Shop
- Use free daily heal (resets at midnight)

---

## 🎨 UI Features

- **Cyberpunk Theme** - Neon colors (cyan, magenta, gold)
- **Pill-Shaped Buttons** - Modern rounded navigation
- **Drag-to-Move Window** - Custom title bar
- **Animated Splash Screen** - Loading animation
- **Equipment Visualization** - Visual character display
- **Health Bars** - Visual HP and XP tracking
- **Animated Battle Log** - Smooth fade messages

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
| **v1.3.0** | Enhanced Battle: PrepareForBattle, OpponentPreview, Stat Scaling, Bot Opponents |

---

## ⚠️ Troubleshooting

### "Module javafx.controls not found"
- Update JavaFX SDK path in VM options
- Ensure path points to the `lib` folder inside the SDK

### "Access denied for user 'root'"
- Check MySQL password in `DatabaseUtil.java`
- Default: Seecs@123

### Compilation errors
- Run: `mvn clean compile`
- In IntelliJ: File → Invalidate Caches → Restart

### Database schema issues
- The app auto-creates tables on first run
- Restart the app to recreate if needed

---

## 📧 Contact

- **GitHub**: https://github.com/kahmedbscs25seecs-bot/ProjectForge

---

<p align="center">
  <strong>Level up your life with FORGE! 🎮</strong>
</p>