package bds;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import com.opencsv.CSVReader; // CSV Reader
import com.opencsv.exceptions.CsvException;
import java.util.*; // Properties, Hashmap

import edu.stanford.nlp.simple.*; // NLP

public class App {
    public static String delim = " |,|\"|=|%|^|&|\t|;|\\.|\\?|!|-|:|\\[|\\]|\\(|\\)|\\{|\\}|\\*|/";
    public static String stopWordsFile = "";
    // public static String stopwords_default = "./data/stopwords_default.txt";
    public static String fakeCSV = "./data/Fake.csv";
    public static String trueCSV = "./data/True.csv";
    public static String fakeCSV_short = "./data/Fake_short.csv";
    public static String trueCSV_short = "./data/True_short.csv";
    public static String oneLine = "./data/True_oneline.csv";

    public static String output = "./output/";
    public static String stopwords_file = "./data/stopwords.txt";
    public static String stopwords_def_file = "./data/stopwords_default.txt";

    public static Set<String> stopWords;
    public static Set<String> stopWords_default;
    
    public static Set<String> add_to_set(String file_name){
        Set<String> s = new HashSet<String>();
        try {
            //Opening file
            BufferedReader buffReader = new BufferedReader(new FileReader(file_name));
            String row;
            while ((row = buffReader.readLine()) != null){
                s.add(row);
            }
            buffReader.close();
        } catch (IOException e) {
            System.out.printf("Cannot open stop words: %s\n", file_name);
            e.printStackTrace();
        }
        return s;
    }

    public static void write_to_file(String filename, HashMap<String,Integer> map) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            for (String key : map.keySet()){
                myWriter.write(key + "," + map.get(key)  + "\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to: " + filename);
        } catch (IOException e) {
            System.out.println("Write to file failed: " + filename);
            e.printStackTrace();
        }
    }

    public static void data_exploration(String input_file, String correctness) {
        //Counters
        // HashMap<String,Integer> HashtagCounter = new HashMap<>();
        // HashMap<String,Integer> AtCounter = new HashMap<>();
        //Opening file
        try {
            CSVReader csvReader = new CSVReader(new FileReader(input_file));
            String[] row;
            int counter = 0;
            row = csvReader.readNext(); //Removing header
            while ((row = csvReader.readNext()) != null) {
                String title = row[0];
                String text = row[1];
                String subject = row[2];

                System.out.println(counter + ": " + row[0] + "\t" + row[2] + "\t" + row[3]);
                sentenceAnalyze(row[0]);
                sentenceAnalyze(row[1]);
                // System.out.println(counter + ": " + row[1]);
                counter+=1;
            }
            //Closing file
            csvReader.close();
        } catch (IOException e) {
            System.out.printf("Cannot open file: %s\n", input_file);
            e.printStackTrace();
        } catch (CsvException e){
            System.out.printf("Error reading csv: %s\n", input_file);
            e.printStackTrace();
        }
    }

    public static void sentenceAnalyze(String line) {
        Document doc = new Document(line);
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            List<Token> tokens = sent.tokens();
            List<String> lemmas = sent.lemmas(); // Lemma is the word in dictionary, was => be
            List<String> pos = sent.posTags(); //Parts of speech
            List<String> ner = sent.nerTags(); // Name entity recognition
            String sentiment = sent.sentiment().toString();
            // System.out.println("sent: " + sent);
            // System.out.println("lemma: " + sent.lemmas());
            // System.out.println("parse: " + sent.parse());
            
        }
    }

    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Invalid cmd line arguments.");
            return;
        }
        stopWords = add_to_set(stopwords_file);
        stopWords_default = add_to_set(stopwords_def_file);
        switch (args[0]){
            case "short":
                data_exploration(fakeCSV_short, "fake");
                data_exploration(trueCSV_short, "true");
                break;
            case "long":
                data_exploration(fakeCSV, "fake");
                data_exploration(trueCSV, "true");
                break;
            case "oneline":
                data_exploration(oneLine, "true");
                break;
        }
    }
}