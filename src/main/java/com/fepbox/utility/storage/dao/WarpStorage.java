package com.fepbox.utility.storage.dao;

import com.fepbox.utility.model.Warp;
import java.util.List;

public interface WarpStorage {
    void init();
    void save(Warp warp);
    Warp load(String name);
    List<Warp> list();
    void delete(String name);
    void rename(String oldName, String newName);
}
