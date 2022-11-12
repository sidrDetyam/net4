package ru.nsu.gemuev.net4.util;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.Main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Log4j2
public class PropertyGetter {

    private final static Map<String, String> properties = new HashMap<>();

    static {
        try(var is =
                    Main.class.getResourceAsStream("api_keys.properties")){
            if(is == null){
                throw new IOException("api keys not found");
            }
            var prop = new Properties();
            prop.load(is);
            for(var i : prop.entrySet()){
                properties.put((String) i.getKey(), (String) i.getValue());
            }
        } catch (IOException e) {
            log.error("Load properties fail " + e.getMessage());
        }
    }

    private PropertyGetter(){}

    public static Optional<String> getProperty(@NonNull String key){
        return Optional.ofNullable(properties.get(key));
    }

    public static String getPropertyOrThrow(@NonNull String key){
        return getProperty(key).orElseThrow(() -> new RuntimeException(key));
    }
}
