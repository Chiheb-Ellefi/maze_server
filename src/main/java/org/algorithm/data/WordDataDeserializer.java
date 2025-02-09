package org.algorithm.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;

public class WordDataDeserializer extends JsonDeserializer<WordData> {

    @Override
    public WordData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode rootNode = p.getCodec().readTree(p);

        // The outer object is "words"
        JsonNode wordsNode = rootNode.get("words");
        if (wordsNode == null) {
            throw new IllegalArgumentException("JSON does not contain 'words' key");
        }

        Map<String, List<String>> wordsMap = new HashMap<>();

        // Iterate over each category (e.g., "technology", "sports")
        Iterator<Map.Entry<String, JsonNode>> categories = wordsNode.fields();
        while (categories.hasNext()) {
            Map.Entry<String, JsonNode> categoryEntry = categories.next();
            String category = categoryEntry.getKey(); // e.g., "technology"
            JsonNode wordsArray = categoryEntry.getValue();

            List<String> wordsList = new ArrayList<>();

            // Iterate over each word in the category
            for (JsonNode wordNode : wordsArray) {
                wordsList.add(wordNode.asText()); // Add each word to the list
            }

            wordsMap.put(category, wordsList); // Map category to its list of words
        }

        WordData wordData = new WordData();
        wordData.setWords(wordsMap);
        return wordData;
    }
}
