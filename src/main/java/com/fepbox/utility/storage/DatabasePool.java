package com.fepbox.utility.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {
    private final HikariDataSource ds;

    public DatabasePool(JavaPlugin plugin) {
        HikariConfig config = new HikariConfig();
        String type = plugin.getConfig().getString("storage.type", "YAML").toUpperCase();
        switch (type) {
            case "MYSQL" -> {
                String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
                String db = plugin.getConfig().getString("storage.mysql.database", "fepbox");
                String user = plugin.getConfig().getString("storage.mysql.user", "root");
                String pass = plugin.getConfig().getString("storage.mysql.password", "password");
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true");
                config.setUsername(user);
                config.setPassword(pass);
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            }
            default -> {
                File file = new File(plugin.getDataFolder(), "data.db");
                config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            }
        }
        config.setPoolName("Fepbox-Utility-Pool");
        config.setMaximumPoolSize(10);
        this.ds = new HikariDataSource(config);
    }

    public Connection connection() throws SQLException {
        return ds.getConnection();
    }

    public void close() { ds.close(); }
}
