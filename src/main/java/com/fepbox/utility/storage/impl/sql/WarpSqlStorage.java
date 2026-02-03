package com.fepbox.utility.storage.impl.sql;

import com.fepbox.utility.model.Warp;
import com.fepbox.utility.storage.DatabasePool;
import com.fepbox.utility.storage.dao.WarpStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WarpSqlStorage implements WarpStorage {
    private final JavaPlugin plugin;
    private final DatabasePool pool;

    public WarpSqlStorage(JavaPlugin plugin, DatabasePool pool) {
        this.plugin = plugin;
        this.pool = pool;
    }

    @Override
    public void init() {
        try (Connection c = pool.connection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS warps(name TEXT PRIMARY KEY, world TEXT, x REAL, y REAL, z REAL, yaw REAL, pitch REAL, slot INTEGER, icon TEXT)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void save(Warp warp) {
        String sql = "INSERT OR REPLACE INTO warps(name,world,x,y,z,yaw,pitch,slot,icon) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, warp.name());
            ps.setString(2, warp.world());
            ps.setDouble(3, warp.x());
            ps.setDouble(4, warp.y());
            ps.setDouble(5, warp.z());
            ps.setFloat(6, warp.yaw());
            ps.setFloat(7, warp.pitch());
            ps.setInt(8, warp.slot());
            ps.setString(9, warp.icon());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Warp load(String name) {
        String sql = "SELECT * FROM warps WHERE name=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int slot = safeInt(rs, "slot", -1);
                String icon = safeString(rs, "icon", "ENDER_PEARL");
                return new Warp(rs.getString("name"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"), slot, icon);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Warp> list() {
        List<Warp> list = new ArrayList<>();
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM warps")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int slot = safeInt(rs, "slot", -1);
                String icon = safeString(rs, "icon", "ENDER_PEARL");
                list.add(new Warp(rs.getString("name"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"), slot, icon));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void delete(String name) {
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM warps WHERE name=?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void rename(String oldName, String newName) {
        Warp w = load(oldName);
        delete(oldName);
        if (Objects.nonNull(w)) save(new Warp(newName, w.world(), w.x(), w.y(), w.z(), w.yaw(), w.pitch(), w.slot(), w.icon()));
    }

    private int safeInt(ResultSet rs, String col, int def){
        try { return rs.getInt(col); } catch (SQLException e){ return def; }
    }
    private String safeString(ResultSet rs, String col, String def){
        try { String v = rs.getString(col); return v==null?def:v; } catch (SQLException e){ return def; }
    }
}
