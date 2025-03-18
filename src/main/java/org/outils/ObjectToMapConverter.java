package org.outils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class ObjectToMapConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF =
            new TypeReference<>() {};

    public static Map<String, Object> convertToMap(Object obj) throws JsonProcessingException {
        if (obj == null) {
            return Collections.emptyMap();
        }

        try {
            if (obj instanceof String str) {
                return objectMapper.readValue(str, MAP_TYPE_REF);
            }
            return objectMapper.convertValue(obj, MAP_TYPE_REF);
        } catch (IllegalArgumentException e) {
            throw new JsonProcessingException("Failed to convert object to map") {
                private static final long serialVersionUID = 1L;
            };
        }
    }
}