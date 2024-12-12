package org.example.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author by liangzj
 * @since 2022/8/30 23:40
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String from(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parse(String json, Class<T> clazz) {

        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parse(InputStream in, Class<T> clazz) {
        try {
            return mapper.readValue(in, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
