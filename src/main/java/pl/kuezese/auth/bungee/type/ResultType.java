package pl.kuezese.auth.bungee.type;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Arrays;

@Getter
public enum ResultType {
    PREMIUM("premium"),
    NON_PREMIUM("non-premium"),
    ERROR("error");

    private final String name;

    ResultType(String name) {
        this.name = name;
    }

    @Nullable
    public static ResultType findByName(String name) {
        return Arrays.stream(values()).filter(r -> r.getName().equals(name)).findAny().orElse(null);
    }
}