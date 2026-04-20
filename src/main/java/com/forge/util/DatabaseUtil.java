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
                ResultSet rs = null;

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
                    "attack_type VARCHAR(20) NOT NULL," +
                    "defense_type VARCHAR(20) NOT NULL," +
                    "damage_dealt INT NOT NULL," +
                    "damage_reduced INT DEFAULT 0," +
                    "turn_order INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (battle_id) REFERENCES battles(id))");

                stmt.execute("CREATE TABLE IF NOT EXISTS battle_queue (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'WAITING'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

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
                stmt.execute("INSERT INTO items (name, description, type, slot, sprite_color, unlock_level, cost) VALUES " +
                    "('Iron Helmet', 'Basic head protection', 'COSMETIC', 'HEAD', '#C0C0C0', 1, 0), " +
                    "('Golden Crown', 'Royal headgear', 'COSMETIC', 'HEAD', '#FFD700', 3, 100), " +
                    "('Diamond Band', 'Epic headgear', 'COSMETIC', 'HEAD', '#B9F2FF', 7, 250), " +
                    "('Iron Chestplate', 'Basic body armor', 'COSMETIC', 'CHEST', '#C0C0C0', 1, 0), " +
                    "('Golden Armor', 'Royal chestpiece', 'COSMETIC', 'CHEST', '#FFD700', 3, 100), " +
                    "('Diamond Chest', 'Epic body armor', 'COSMETIC', 'CHEST', '#B9F2FF', 7, 250), " +
                    "('Iron Gloves', 'Basic hand protection', 'COSMETIC', 'GLOVES', '#C0C0C0', 1, 0), " +
                    "('Golden Gloves', 'Royal gloves', 'COSMETIC', 'GLOVES', '#FFD700', 3, 100), " +
                    "('Diamond Gloves', 'Epic gloves', 'COSMETIC', 'GLOVES', '#B9F2FF', 7, 250), " +
                    "('Iron Leggings', 'Basic leg protection', 'COSMETIC', 'LEGS', '#C0C0C0', 1, 0), " +
                    "('Golden Leggings', 'Royal leg armor', 'COSMETIC', 'LEGS', '#FFD700', 3, 100), " +
                    "('Diamond Pants', 'Epic leg armor', 'COSMETIC', 'LEGS', '#B9F2FF', 7, 250), " +
                    "('Iron Boots', 'Basic foot protection', 'COSMETIC', 'BOOTS', '#C0C0C0', 1, 0), " +
                    "('Golden Boots', 'Royal boots', 'COSMETIC', 'BOOTS', '#FFD700', 3, 100), " +
                    "('Diamond Boots', 'Epic boots', 'COSMETIC', 'BOOTS', '#B9F2FF', 7, 250), " +
                    "('Wooden Sword', 'Basic weapon', 'COSMETIC', 'WEAPON', '#8B4513', 1, 0), " +
                    "('Iron Sword', 'Standard weapon', 'COSMETIC', 'WEAPON', '#C0C0C0', 2, 50), " +
                    "('Golden Blade', 'Royal sword', 'COSMETIC', 'WEAPON', '#FFD700', 5, 150), " +
                    "('Legendary Blade', 'Epic weapon', 'COSMETIC', 'WEAPON', '#B9F2FF', 10, 300), " +
                    "('Coin Pouch', '2x coins earned', 'FUNCTIONAL', 'ACCESSORY', '#FFD700', 2, 50), " +
                    "('XP Ring', '2x XP earned', 'FUNCTIONAL', 'ACCESSORY', '#B9F2FF', 4, 75)");
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