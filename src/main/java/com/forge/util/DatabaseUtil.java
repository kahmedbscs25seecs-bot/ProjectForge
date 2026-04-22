package com.forge.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/forge?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Seecs@123";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public static void initializeDatabase() {
        try {
            try (Connection conn = getConnection()) {
                var stmt = conn.createStatement();
                ResultSet rs;

                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "email VARCHAR(100) UNIQUE NOT NULL," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "coins INT DEFAULT 0," +
                    "xp INT DEFAULT 0," +
                    "level INT DEFAULT 1," +
                    "total_quests_completed INT DEFAULT 0," +
                    "total_coins_earned INT DEFAULT 0," +
                    "current_streak INT DEFAULT 0," +
                    "longest_streak INT DEFAULT 0," +
                    "last_active_date DATE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                try { stmt.execute("ALTER TABLE users ADD COLUMN current_streak INT DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN longest_streak INT DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN last_active_date DATE"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN attack INT DEFAULT 10"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN defense INT DEFAULT 10"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN luck INT DEFAULT 5"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN rank_points INT DEFAULT 100"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN max_hp INT DEFAULT 60"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN current_hp INT DEFAULT 60"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN last_heal_time TIMESTAMP NULL"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE users ADD COLUMN defense_type VARCHAR(20) DEFAULT 'SHIELD'"); } catch (Exception e) {}

                stmt.execute("CREATE TABLE IF NOT EXISTS defense_settings (" +
                    "user_id INT PRIMARY KEY," +
                    "defense_type VARCHAR(20) NOT NULL DEFAULT 'SHIELD'," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS battles (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "attacker_id INT NOT NULL," +
                    "defender_id INT NOT NULL," +
                    "attacker_hp_before INT," +
                    "defender_hp_before INT," +
                    "winner_id INT," +
                    "attacker_points_change INT DEFAULT 0," +
                    "defender_points_change INT DEFAULT 0," +
                    "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "completed_at TIMESTAMP NULL," +
                    "FOREIGN KEY (attacker_id) REFERENCES users(id)," +
                    "FOREIGN KEY (defender_id) REFERENCES users(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS battle_actions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "battle_id INT NOT NULL," +
                    "attacker_id INT NOT NULL," +
                    "defender_id INT NOT NULL," +
                    "round_number INT NOT NULL," +
                    "spell_id INT," +
                    "spell_name VARCHAR(50)," +
                    "spell_type VARCHAR(20)," +
                    "spell_effect VARCHAR(50)," +
                    "damage_dealt INT DEFAULT 0," +
                    "damage_reduced INT DEFAULT 0," +
                    "turn_order INT NOT NULL," +
                    "hit BOOLEAN DEFAULT TRUE," +
                    "status_effect VARCHAR(50)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (battle_id) REFERENCES battles(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS battle_queue (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'WAITING'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "description TEXT," +
                    "type VARCHAR(30) DEFAULT 'COSMETIC'," +
                    "slot VARCHAR(20)," +
                    "sprite_color VARCHAR(20)," +
                    "unlock_level INT DEFAULT 1," +
                    "cost INT DEFAULT 0," +
                    "rarity VARCHAR(20) DEFAULT 'COMMON'," +
                    "attack_bonus INT DEFAULT 0," +
                    "defense_bonus INT DEFAULT 0," +
                    "is_consumable BOOLEAN DEFAULT FALSE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS user_inventory (" +
                    "user_id INT NOT NULL," +
                    "item_id INT NOT NULL," +
                    "quantity INT DEFAULT 1," +
                    "acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "is_equipped BOOLEAN DEFAULT FALSE," +
                    "PRIMARY KEY (user_id, item_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE)");

                try { stmt.execute("ALTER TABLE items ADD COLUMN attack_bonus INT DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE items ADD COLUMN defense_bonus INT DEFAULT 0"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE items ADD COLUMN is_consumable BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE user_inventory ADD COLUMN is_equipped BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}

                stmt.execute("CREATE TABLE IF NOT EXISTS achievements (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "description TEXT," +
                    "icon VARCHAR(50)," +
                    "xp_reward INT DEFAULT 10," +
                    "coin_reward INT DEFAULT 5," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS user_achievements (" +
                    "user_id INT NOT NULL," +
                    "achievement_id INT NOT NULL," +
                    "unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (user_id, achievement_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS user_progress (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "quest_id INT NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'PENDING'," +
                    "progress_data TEXT," +
                    "started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "expires_at TIMESTAMP NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS user_active_modes (" +
                    "user_id INT NOT NULL," +
                    "mode_id INT NOT NULL," +
                    "activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (user_id, mode_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (mode_id) REFERENCES modes(id) ON DELETE CASCADE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS healing_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "description TEXT," +
                    "heal_amount INT NOT NULL," +
                    "cost INT DEFAULT 0," +
                    "rarity VARCHAR(20) DEFAULT 'COMMON')");

                stmt.execute("CREATE TABLE IF NOT EXISTS user_healing_items (" +
                    "user_id INT NOT NULL," +
                    "item_id INT NOT NULL," +
                    "quantity INT DEFAULT 1," +
                    "PRIMARY KEY (user_id, item_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (item_id) REFERENCES healing_items(id))");

                rs = stmt.executeQuery("SELECT COUNT(*) FROM healing_items");
                rs.next();
                if (rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO healing_items (name, description, heal_amount, cost, rarity) VALUES " +
                        "('Small Potion', 'Restores 20 HP', 20, 10, 'COMMON'), " +
                        "('Medium Potion', 'Restores 50 HP', 50, 25, 'UNCOMMON'), " +
                        "('Large Potion', 'Restores 100 HP', 100, 50, 'RARE'), " +
                        "('Full Restore', 'Fully restores HP', 999, 100, 'EPIC')");
                }

                stmt.execute("CREATE TABLE IF NOT EXISTS spells (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "description TEXT," +
                    "type VARCHAR(20) NOT NULL," +
                    "base_damage INT DEFAULT 0," +
                    "mana_cost INT DEFAULT 0," +
                    "accuracy INT DEFAULT 100," +
                    "effect VARCHAR(50)," +
                    "rarity VARCHAR(20) DEFAULT 'COMMON')");

                rs = stmt.executeQuery("SELECT COUNT(*) FROM spells");
                rs.next();
                if (rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO spells (name, description, type, base_damage, mana_cost, accuracy, effect, rarity) VALUES " +
                        "('Incendio', 'Fire spell - burns for extra damage', 'ATTACK', 35, 15, 90, 'BURN', 'COMMON'), " +
                        "('Aguamenti', 'Water spell - soaking hit', 'ATTACK', 30, 12, 95, 'WET', 'COMMON'), " +
                        "('Stupefy', 'Stunning blow - may stun enemy', 'ATTACK', 40, 18, 85, 'STUN', 'UNCOMMON'), " +
                        "('Expelliarmus', 'Disarms opponent - drops their wand', 'ATTACK', 25, 10, 100, 'DISARM', 'UNCOMMON'), " +
                        "('Avada Kedavra', 'Unforgivable curse - massive damage', 'ATTACK', 80, 35, 60, 'DEATH', 'LEGENDARY'), " +
                        "('Protego', 'Shield charm - blocks attacks', 'DEFENSE', 20, 10, 100, 'SHIELD', 'COMMON'), " +
                        "('Reparo', 'Mending charm - repairs your defense', 'DEFENSE', 15, 8, 100, 'HEAL', 'COMMON'), " +
                        "('Fiendfyre', 'Converts enemy attack to fire', 'DEFENSE', 30, 20, 75, 'REFLECT', 'EPIC'), " +
                        "('Lumos', 'Reveals hidden information', 'UTILITY', 0, 5, 100, 'REVEAL', 'COMMON'), " +
                        "('Nox', 'Helps see enemy weaknesses', 'UTILITY', 0, 8, 100, 'SCAN', 'UNCOMMON'), " +
                        "('Obliviate', 'Confuses enemy - reduces accuracy', 'CURSED', 20, 25, 70, 'CONFUSE', 'RARE')");
                }

                stmt.execute("CREATE TABLE IF NOT EXISTS wands (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "wood VARCHAR(30)," +
                    "core VARCHAR(30)," +
                    "attack_bonus INT DEFAULT 0," +
                    "defense_bonus INT DEFAULT 0," +
                    "luck_bonus INT DEFAULT 0," +
                    "cost INT DEFAULT 0," +
                    "unlock_level INT DEFAULT 1)");

                rs = stmt.executeQuery("SELECT COUNT(*) FROM wands");
                rs.next();
                if (rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO wands (name, wood, core, attack_bonus, defense_bonus, luck_bonus, cost, unlock_level) VALUES " +
                        "('Core Wand', 'Oak', 'Phoenix Feather', 5, 5, 5, 0, 1), " +
                        "('Willow Wand', 'Willow', 'Dragon Heartstring', 10, 8, 3, 100, 3), " +
                        "('Hazel Wand', 'Hazel', 'Unicorn Hair', 8, 10, 8, 100, 5), " +
                        "('Ebony Wand', 'Ebony', 'Thestral Tail', 15, 5, 10, 200, 7), " +
                        "('Elder Wand', 'Elder', 'Deathly Hallow', 25, 15, 15, 500, 15)");
                }

                stmt.execute("CREATE TABLE IF NOT EXISTS user_wand (" +
                    "user_id INT PRIMARY KEY," +
                    "wand_id INT DEFAULT 1," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (wand_id) REFERENCES wands(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS modes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "description TEXT," +
                    "icon_path VARCHAR(255)," +
                    "is_custom BOOLEAN DEFAULT FALSE," +
                    "unlock_level INT DEFAULT 1)");

                stmt.execute("CREATE TABLE IF NOT EXISTS quests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "mode_id INT," +
                    "title VARCHAR(100) NOT NULL," +
                    "description TEXT," +
                    "xp_reward INT DEFAULT 10," +
                    "coin_reward INT DEFAULT 5," +
                    "penalty_type VARCHAR(20)," +
                    "penalty_value INT DEFAULT 0," +
                    "difficulty VARCHAR(20) DEFAULT 'MEDIUM'," +
                    "time_limit_hours INT DEFAULT 24," +
                    "required_slot VARCHAR(20)," +
                    "required_item_id INT," +
                    "is_custom BOOLEAN DEFAULT FALSE," +
                    "is_daily BOOLEAN DEFAULT FALSE," +
                    "FOREIGN KEY (mode_id) REFERENCES modes(id))");

                try { stmt.execute("ALTER TABLE quests ADD COLUMN difficulty VARCHAR(20) DEFAULT 'MEDIUM'"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE quests ADD COLUMN time_limit_hours INT DEFAULT 24"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE quests ADD COLUMN required_slot VARCHAR(20)"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE quests ADD COLUMN required_item_id INT"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE quests ADD COLUMN is_custom BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE quests ADD COLUMN is_daily BOOLEAN DEFAULT FALSE"); } catch (Exception e) {}

                try { stmt.execute("ALTER TABLE user_progress ADD COLUMN expires_at TIMESTAMP NULL"); } catch (Exception e) {}

                try { stmt.execute("ALTER TABLE items ADD COLUMN slot VARCHAR(20)"); } catch (Exception e) {}
                try { stmt.execute("ALTER TABLE items ADD COLUMN sprite_color VARCHAR(20)"); } catch (Exception e) {}
                try {
                    seedDefaultData(stmt);
                    System.out.println("Database initialized successfully");
                } catch (Exception e) {
                    System.out.println("Seed data may already exist: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void seedDefaultData(java.sql.Statement stmt) {
        try {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM modes");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO modes (name, description, icon_path, unlock_level) VALUES " +
                    "('Study', 'Focus and learn with productive tasks', '/images/modes/study.png', 1), " +
                    "('Body Builder', 'Fitness and workout challenges', '/images/modes/gym.png', 1), " +
                    "('Introvertness Killer', 'Push your social boundaries', '/images/modes/social.png', 1), " +
                    "('Creative', 'Unleash your artistic side', '/images/modes/creative.png', 3), " +
                    "('Mindful', 'Meditation and mental wellness', '/images/modes/mindful.png', 5)");

                rs = stmt.executeQuery("SELECT id FROM modes WHERE name = 'Study'");
                rs.next();
                int studyId = rs.getInt(1);
                stmt.execute("INSERT INTO quests (mode_id, title, description, xp_reward, coin_reward, penalty_type, penalty_value, difficulty, time_limit_hours) VALUES " +
                    "(" + studyId + ", 'Deep Focus Session', 'Study for 2 hours without interruption', 50, 25, 'lose_coins', 10, 'HARD', 72), " +
                    "(" + studyId + ", 'Complete Chapter', 'Read one complete chapter of your book', 30, 15, 'extra_task', 1, 'MEDIUM', 24), " +
                    "(" + studyId + ", 'Practice Problems', 'Solve 10 practice problems', 40, 20, 'lose_coins', 5, 'EASY', 12)");

                rs = stmt.executeQuery("SELECT id FROM modes WHERE name = 'Body Builder'");
                rs.next();
                int gymId = rs.getInt(1);
                stmt.execute("INSERT INTO quests (mode_id, title, description, xp_reward, coin_reward, penalty_type, penalty_value, difficulty, time_limit_hours) VALUES " +
                    "(" + gymId + ", 'Morning Workout', 'Complete a 45-minute workout', 50, 25, 'lose_coins', 10, 'MEDIUM', 24), " +
                    "(" + gymId + ", '100 Pushups', 'Do 100 pushups throughout the day', 40, 20, 'extra_task', 1, 'EASY', 12), " +
                    "(" + gymId + ", 'No Junk Food', 'Avoid junk food for one day', 30, 15, 'lose_coins', 5, 'EASY', 12)");

                rs = stmt.executeQuery("SELECT id FROM modes WHERE name = 'Introvertness Killer'");
                rs.next();
                int socialId = rs.getInt(1);
                stmt.execute("INSERT INTO quests (mode_id, title, description, xp_reward, coin_reward, penalty_type, penalty_value, difficulty, time_limit_hours) VALUES " +
                    "(" + socialId + ", 'Talk to Stranger', 'Have a 5-minute conversation with someone new', 50, 25, 'extra_task', 2, 'EASY', 12), " +
                    "(" + socialId + ", 'Social Event', 'Attend a social gathering', 60, 30, 'lose_coins', 15, 'HARD', 72), " +
                    "(" + socialId + ", 'Share Something', 'Share something personal with a friend', 40, 20, 'extra_task', 1, 'MEDIUM', 24)");
            }

            rs = stmt.executeQuery("SELECT COUNT(*) FROM items");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO items (name, description, type, slot, sprite_color, unlock_level, cost, attack_bonus, defense_bonus) VALUES " +
                    "('Iron Helmet', 'Basic head protection +2 DEF', 'COSMETIC', 'HELMET', '#C0C0C0', 1, 0, 0, 2), " +
                    "('Steel Helmet', 'Strong head protection +3 DEF', 'COSMETIC', 'HELMET', '#708090', 3, 100, 0, 3), " +
                    "('Golden Crown', 'Royal headgear +4 DEF', 'COSMETIC', 'HELMET', '#FFD700', 5, 150, 0, 4), " +
                    "('Diamond Band', 'Epic headgear +6 DEF', 'COSMETIC', 'HELMET', '#B9F2FF', 8, 300, 0, 6), " +
                    "('Leather Armor', 'Basic body armor +2 DEF', 'COSMETIC', 'ARMOR', '#8B4513', 1, 0, 0, 2), " +
                    "('Chainmail', 'Strong body armor +3 DEF', 'COSMETIC', 'ARMOR', '#C0C0C0', 3, 100, 0, 3), " +
                    "('Plate Armor', 'Heavy armor +5 DEF', 'COSMETIC', 'ARMOR', '#708090', 5, 200, 0, 5), " +
                    "('Dragon Scale', 'Epic armor +7 DEF', 'COSMETIC', 'ARMOR', '#228B22', 10, 400, 0, 7), " +
                    "('Leather Gloves', 'Basic hand protection +1 ATK', 'COSMETIC', 'GLOVES', '#8B4513', 1, 0, 1, 0), " +
                    "('Combat Gloves', 'Combat gloves +2 ATK', 'COSMETIC', 'GLOVES', '#2F4F4F', 3, 75, 2, 0), " +
                    "('Gauntlets', 'Heavy gauntlets +3 ATK', 'COSMETIC', 'GLOVES', '#708090', 6, 150, 3, 0), " +
                    "('War Gauntlets', 'Epic gauntlets +4 ATK', 'COSMETIC', 'GLOVES', '#FFD700', 9, 250, 4, 0), " +
                    "('Leather Boots', 'Basic foot protection +1 DEF', 'COSMETIC', 'BOOTS', '#8B4513', 1, 0, 0, 1), " +
                    "('Nice Boots', 'Nice boots +1 ATK +1 DEF', 'COSMETIC', 'BOOTS', '#654321', 3, 50, 1, 1), " +
                    "('Swift Boots', 'Swift boots +2 ATK +1 DEF', 'COSMETIC', 'BOOTS', '#2F4F4F', 5, 100, 2, 1), " +
                    "(' Shadow Boots', 'Shadow boots +3 ATK +2 DEF', 'COSMETIC', 'BOOTS', '#191970', 8, 200, 3, 2), " +
                    "('Lucky Charm', 'Accessory +2 LCK', 'FUNCTIONAL', 'ACCESSORY', '#FFD700', 2, 50, 0, 0), " +
                    "('Power Ring', 'Ring of power +1 ATK +1 DEF', 'FUNCTIONAL', 'ACCESSORY', '#B9F2FF', 4, 75, 1, 1), " +
                    "('Amulet', 'Amulet +3 DEF', 'FUNCTIONAL', 'ACCESSORY', '#9932CC', 6, 100, 0, 3), " +
                    "('Talisman', 'Epic talisman +2 ATK +2 DEF', 'FUNCTIONAL', 'ACCESSORY', '#FFD700', 10, 250, 2, 2), " +
                    "('Strength Potion', 'Battle buff +5 ATK for 1 battle', 'FUNCTIONAL', 'CONSUMABLE', '#FF4500', 1, 25, 5, 0, TRUE), " +
                    "('Defense Potion', 'Battle buff +5 DEF for 1 battle', 'FUNCTIONAL', 'CONSUMABLE', '#4169E1', 1, 25, 0, 5, TRUE), " +
                    "('Power Elixir', 'Battle buff +3 ATK +3 DEF', 'FUNCTIONAL', 'CONSUMABLE', '#9932CC', 5, 50, 3, 3, TRUE), " +
                    "('Legendary Elixir', 'Battle buff +5 ATK +5 DEF', 'FUNCTIONAL', 'CONSUMABLE', '#FFD700', 10, 100, 5, 5, TRUE)");
            }

            rs = stmt.executeQuery("SELECT COUNT(*) FROM achievements");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO achievements (name, description) VALUES " +
                    "('First Quest', 'Complete your first quest'), " +
                    "('Level 5', 'Reach level 5'), " +
                    "('Coin Collector', 'Earn 100 coins total'), " +
                    "('Quest Master', 'Complete 50 quests'), " +
                    "('Level 10', 'Reach level 10'), " +
                    "('Wealthy', 'Have 200 coins at once')");
            }
        } catch (SQLException e) {
            System.out.println("Seed data may already exist: " + e.getMessage());
        }
    }
}