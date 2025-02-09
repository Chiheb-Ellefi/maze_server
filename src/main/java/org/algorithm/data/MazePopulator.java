package org.algorithm.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MazePopulator {
    private ObjectMapper mapper ;
    private SimpleModule module ;
    private List<String> themes;
    private Random random;
    public MazePopulator(  ){
        this.mapper = new ObjectMapper();
        this.module = new SimpleModule();
        this.themes= Arrays.asList("technology","sports","animals","countries","food","music","science","geography");
        this.random=new Random();
    }
    public List<String> getData() {
        module.addDeserializer(WordData.class, new WordDataDeserializer());
        mapper.registerModule(module);
        File file = new File("src/main/resources/dictionary.json");
        try{
            WordData wordData = mapper.readValue(file, WordData.class);
            String theme=themes.get(random.nextInt(themes.size()));
            return wordData.getWords().get(theme);
        }catch(IOException e){
            e.printStackTrace();
            return List.of();
        }
    }
}
