package com.forge.service;

import com.forge.model.Mode;
import com.forge.repository.ModeRepository;
import com.forge.repository.UserActiveModesRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ModeService {
    private final ModeRepository modeRepository;
    private final UserActiveModesRepository activeModesRepository;

    public ModeService() {
        this.modeRepository = new ModeRepository();
        this.activeModesRepository = new UserActiveModesRepository();
    }

    public List<Mode> getAllModes() throws SQLException {
        return modeRepository.findAll();
    }

    public List<Mode> getAvailableModes(int userLevel) throws SQLException {
        return modeRepository.findAvailableForLevel(userLevel);
    }

    public Optional<Mode> getModeById(int id) throws SQLException {
        return modeRepository.findById(id);
    }

    public int createCustomMode(Mode mode) throws SQLException {
        mode.setCustom(true);
        return modeRepository.create(mode);
    }

    public List<Mode> getActiveModes(int userId) throws SQLException {
        return activeModesRepository.findActiveModesByUserId(userId);
    }

    public void addActiveMode(int userId, int modeId) throws SQLException {
        activeModesRepository.addActiveMode(userId, modeId);
    }

    public void removeActiveMode(int userId, int modeId) throws SQLException {
        activeModesRepository.removeActiveMode(userId, modeId);
    }
    
    public void deleteMode(int modeId) throws SQLException {
        modeRepository.deleteById(modeId);
    }
}