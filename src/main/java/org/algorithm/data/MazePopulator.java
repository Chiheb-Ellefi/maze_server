package org.algorithm.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.algorithm.game_server.components.GameHandler;
import org.algorithm.visualizer.MazeVisualizer;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MazePopulator {
    private final ObjectMapper mapper;
    private final SimpleModule module;
    private final List<String> themes;
    private final Random random;


    public MazePopulator() {
        this.mapper = new ObjectMapper();
        this.module = new SimpleModule();
        this.themes = Arrays.asList("technology", "sports", "animals", "countries", "food", "music", "science", "geography");
        this.random = new Random();
        this.module.addDeserializer(WordData.class, new WordDataDeserializer());
        this.mapper.registerModule(module);

    }




    public List<String> getData() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("dictionary.json")) {
            if (inputStream == null) {
                throw new RuntimeException("File not found: dictionary.json");
            }

            WordData wordData = mapper.readValue(inputStream, WordData.class);
            String theme = themes.get(random.nextInt(themes.size()));

           GameHandler.theme =theme;
            MazeVisualizer.themeProperty.set(theme);
            return wordData.getWords().getOrDefault(theme, List.of());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
