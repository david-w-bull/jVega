package ic.jvega.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Map;

public class JsonData {

    public static JsonNode readJsonFileToJsonNode(String projectFileName) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(projectFileName);
            if (is == null) {
                throw new FileNotFoundException("File not found in classpath: " + projectFileName);
            }
            JsonNode jsonData = mapper.readTree(is);
            return jsonData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJsonNodeToFile(JsonNode jsonNode, String filename) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Path to the 'out' directory in the project root
            File outDirectory = new File("out");
            File outputFile = new File(outDirectory, filename);

            // Ensure 'out' directory exists
            if (!outDirectory.exists()) {
                outDirectory.mkdir();
            }

            // Convert JsonNode to a pretty formatted string
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

            // Write the pretty formatted JSON string to file
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                fileWriter.write(prettyJson);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static List<Map<String, Object>> jsonNodeToMap(JsonNode jsonValues) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        List<Map<String, Object>> dataMap;
        try {
            jsonString = mapper.writeValueAsString(jsonValues);
            dataMap = mapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return dataMap;
    }
}
