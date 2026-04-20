package com.forge.service;

import com.forge.model.Item;
import com.forge.model.User;
import com.forge.model.UserInventory;
import com.forge.repository.ItemRepository;
import com.forge.repository.UserInventoryRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final ItemRepository itemRepository;
    private final UserInventoryRepository inventoryRepository;
    private final UserService userService;

    public InventoryService() {
        this.itemRepository = new ItemRepository();
        this.inventoryRepository = new UserInventoryRepository();
        this.userService = new UserService();
    }

    public List<Item> getAllItems() throws SQLException {
        return itemRepository.findAll();
    }

    public List<Item> getAvailableItems(int userLevel) throws SQLException {
        return itemRepository.findAvailableForLevel(userLevel);
    }

    public List<UserInventory> getUserInventory(int userId) throws SQLException {
        return inventoryRepository.findByUserId(userId);
    }

    public boolean purchaseItem(int userId, int itemId) throws SQLException {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return false;
        
        Item item = itemOpt.get();
        if (inventoryRepository.hasItem(userId, itemId)) return false;
        
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        if (user.getCoins() < item.getCost()) return false;
        
        userService.subtractCoins(userId, item.getCost());
        UserInventory inventory = new UserInventory(userId, itemId);
        inventoryRepository.create(inventory);
        return true;
    }

    public void equipItem(int inventoryId, boolean equipped) throws SQLException {
        inventoryRepository.updateEquipped(inventoryId, equipped);
    }
}