package com.fepbox.utility.storage.dao;

import com.fepbox.utility.model.Home;
import java.util.List;
import java.util.UUID;

public interface HomeStorage {
    void init();
    void save(UUID owner, Home home);
    Home load(UUID owner, String name);
    List<Home> list(UUID owner);
    void delete(UUID owner, String name);
    void rename(UUID owner, String oldName, String newName);
}
