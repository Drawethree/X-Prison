package me.drawethree.wildprisontokens.database;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class DatabaseCredentialsTest {

    private YamlConfiguration config;

    @Before
    public void init() {
        config = YamlConfiguration.loadConfiguration(new File("C:\\Users\\janci\\OneDrive\\Desktop\\IntelliJ\\IntelliJ Projects\\WildPrisonCore\\WildPrisonTokens\\src\\test\\resources\\config.yml"));
    }

    @Test
    public void testValidLoadingFromConfig() {
        System.out.println("Testing loading credentials from config.yml...");
        assertEquals("test", config.getString("sql.database"));
        assertEquals("localhost", config.getString("sql.host"));
        assertEquals("test", config.getString("sql.username"));
        assertEquals("test", config.getString("sql.password"));
        assertEquals(3306, config.getInt("sql.port"));
    }

}