package me.drawethree.ultraprisoncore.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@AllArgsConstructor
public class DatabaseCredentials {
    private final String host, databaseName, userName, password;
    private final int port;


    public static DatabaseCredentials fromConfig(FileConfiguration config) {
        String host = config.getString("sql.host");
        String dbName = config.getString("sql.database");
        String userName = config.getString("sql.username");
        String password = config.getString("sql.password");
        int port = config.getInt("sql.port");

        Validate.notNull(host);
        Validate.notNull(dbName);
        Validate.notNull(userName);
        Validate.notNull(password);

        return new DatabaseCredentials(host,dbName,userName,password,port);
    }

}
