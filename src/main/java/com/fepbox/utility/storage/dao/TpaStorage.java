package com.fepbox.utility.storage.dao;

import com.fepbox.utility.model.TpaRequest;
import java.util.List;
import java.util.UUID;

public interface TpaStorage {
    void init();
    void saveRequest(TpaRequest req);
    void removeRequest(UUID sender, UUID target);
    List<TpaRequest> requestsTo(UUID target);
    List<TpaRequest> requestsFrom(UUID sender);
    void clearAllFrom(UUID sender);
    boolean toggle(UUID player);
    boolean isToggled(UUID player);
    void ignore(UUID player, UUID target, boolean state);
    boolean isIgnoring(UUID player, UUID target);
}
