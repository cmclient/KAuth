package pl.kuezese.auth.shared.type;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DatabaseType {
    SQLITE("sqlite"),
    MYSQL("mysql");

    private final String name;

    DatabaseType(String name) {
        this.name = name;
    }

    public static DatabaseType findByName(String name) {
        return Arrays.stream(values()).filter(d -> d.getName().equalsIgnoreCase(name)).findAny().orElse(SQLITE);
    }
}
