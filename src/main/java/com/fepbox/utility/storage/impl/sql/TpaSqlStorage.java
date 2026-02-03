package com.fepbox.utility.storage.impl.sql;

import com.fepbox.utility.model.TpaRequest;
import com.fepbox.utility.storage.DatabasePool;
import com.fepbox.utility.storage.dao.TpaStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TpaSqlStorage implements TpaStorage {
    private final JavaPlugin plugin;
    private final DatabasePool pool;

    public TpaSqlStorage(JavaPlugin plugin, DatabasePool pool){ this.plugin=plugin; this.pool=pool; }

    @Override
    public void init() {
        try (Connection c = pool.connection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS tpa(sender TEXT, target TEXT, expires BIGINT, here BOOLEAN, PRIMARY KEY(sender,target))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS tpa_toggle(uuid TEXT PRIMARY KEY, state BOOLEAN)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS tpa_ignore(uuid TEXT, target TEXT, PRIMARY KEY(uuid,target))");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void saveRequest(TpaRequest req) {
        String sql = "INSERT OR REPLACE INTO tpa(sender,target,expires,here) VALUES(?,?,?,?)";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, req.sender().toString());
            ps.setString(2, req.target().toString());
            ps.setLong(3, req.expiresAt());
            ps.setBoolean(4, req.here());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void removeRequest(UUID sender, UUID target) {
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM tpa WHERE sender=? AND target=?")) {
            ps.setString(1, sender.toString());
            ps.setString(2, target.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<TpaRequest> requestsTo(UUID target) {
        List<TpaRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM tpa WHERE target=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, target.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TpaRequest(UUID.fromString(rs.getString("sender")), target, rs.getLong("expires"), rs.getBoolean("here")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<TpaRequest> requestsFrom(UUID sender) {
        List<TpaRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM tpa WHERE sender=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sender.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TpaRequest(sender, UUID.fromString(rs.getString("target")), rs.getLong("expires"), rs.getBoolean("here")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void clearAllFrom(UUID sender) {
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM tpa WHERE sender=?")) {
            ps.setString(1, sender.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public boolean toggle(UUID player) {
        boolean current = isToggled(player);
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO tpa_toggle(uuid,state) VALUES(?,?)")) {
            ps.setString(1, player.toString());
            ps.setBoolean(2, !current);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return !current;
    }

    @Override
    public boolean isToggled(UUID player) {
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("SELECT state FROM tpa_toggle WHERE uuid=?")) {
            ps.setString(1, player.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("state");
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void ignore(UUID player, UUID target, boolean state) {
        try (Connection c = pool.connection()) {
            if (state) {
                try (PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO tpa_ignore(uuid,target) VALUES(?,?)")) {
                    ps.setString(1, player.toString());
                    ps.setString(2, target.toString());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM tpa_ignore WHERE uuid=? AND target=?")) {
                    ps.setString(1, player.toString());
                    ps.setString(2, target.toString());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public boolean isIgnoring(UUID player, UUID target) {
        String sql = "SELECT 1 FROM tpa_ignore WHERE uuid=? AND target=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, target.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
