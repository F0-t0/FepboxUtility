package com.fepbox.utility.storage.impl.sql;

import com.fepbox.utility.model.Home;
import com.fepbox.utility.storage.DatabasePool;
import com.fepbox.utility.storage.dao.HomeStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HomeSqlStorage implements HomeStorage {
    private final JavaPlugin plugin;
    private final DatabasePool pool;

    public HomeSqlStorage(JavaPlugin plugin, DatabasePool pool){ this.plugin=plugin; this.pool=pool; }

    @Override
    public void init() {
        try (Connection c = pool.connection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS homes(uuid TEXT, name TEXT, world TEXT, x REAL, y REAL, z REAL, yaw REAL, pitch REAL, PRIMARY KEY(uuid,name))");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void save(UUID owner, Home home) {
        String sql = "INSERT OR REPLACE INTO homes(uuid,name,world,x,y,z,yaw,pitch) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, owner.toString());
            ps.setString(2, home.name());
            ps.setString(3, home.world());
            ps.setDouble(4, home.x());
            ps.setDouble(5, home.y());
            ps.setDouble(6, home.z());
            ps.setFloat(7, home.yaw());
            ps.setFloat(8, home.pitch());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Home load(UUID owner, String name) {
        String sql = "SELECT * FROM homes WHERE uuid=? AND name=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, owner.toString());
            ps.setString(2, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Home(rs.getString("name"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Home> list(UUID owner) {
        List<Home> list = new ArrayList<>();
        String sql = "SELECT * FROM homes WHERE uuid=?";
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, owner.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new Home(rs.getString("name"), rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public void delete(UUID owner, String name) {
        try (Connection c = pool.connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM homes WHERE uuid=? AND name=?")) {
            ps.setString(1, owner.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void rename(UUID owner, String oldName, String newName) {
        Home h = load(owner, oldName);
        delete(owner, oldName);
        if (Objects.nonNull(h)) save(owner, new Home(newName, h.world(), h.x(), h.y(), h.z(), h.yaw(), h.pitch()));
    }
}
