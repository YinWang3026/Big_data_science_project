package bds;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import com.opencsv.CSVReader; //CSV Reader
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.*; //Properties, Hashmap
import java.lang.*;
import java.util.ArrayList;
//import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.ling.*; //CoreAnnoations
import edu.stanford.nlp.pipeline.*; //StanfordCoreNLP, Annotation, CoreDocument
import edu.stanford.nlp.util.*; //CoreMap, CollectionUtils
import edu.stanford.nlp.semgraph.*; //Dependency parser
// import edu.stanford.nlp.trees.*;

public class App {
    public static String delim = " |,|\"|=|%|^|&|\t|;|\\.|\\?|!|-|:|\\[|\\]|\\(|\\)|\\{|\\}|\\*|/";
    public static String stopWordsFile = "";
    // public static String stopwords_default = "./data/stopwords_default.txt";
    public static String fakeCSV = "./data/Fake.csv";
    public static String trueCSV = "./data/True.csv";
    public static String output = "./output/";

    public static Set<String> stopWords = new HashSet<String>();

    public static void add_to_stopWords(String file_name){
        //Opening file
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(file_name));
            String row;
            while ((row = buffReader.readLine()) != null){
                stopWords.add(row);
            }
            buffReader.close();
        } catch (IOException e) {
            System.out.printf("Cannot open stop words: %s\n", file_name);
            e.printStackTrace();
        }
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

    public static String sentence_cleaning(String row){
        //Splitting CSV
        List<String> rowArray = Arrays.asList(row.split(","));
        List<String> rowArrayCopy = new ArrayList<String>();
        row = String.join(",",rowArray.subList(5,rowArray.size()));  
        // System.out.println("Row before: " + row);
        //Filter out emoji and such
        String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
        // System.out.println("Row before filter:" + row);
        row = row.replaceAll(characterFilter,"");
        // System.out.println("Row after filter:" + row);
        //Removing all the delim chars from sentence
        rowArray = Arrays.asList(row.strip().split(delim));
        //Removing empty words, @, #, emoji, and stop words
        for (int i = 0; i < rowArray.size(); i++){
            String temp = rowArray.get(i).toLowerCase(); //Convert to lower case
            if (temp.length() > 0 && stopWords.contains(temp) == false && 
                temp.charAt(0) != ' ' && temp.charAt(0) != '#' && temp.charAt(0) != '@' &&
                temp.charAt(0) != ';' && temp.charAt(0) != ':') {
                rowArrayCopy.add(temp);
            }
        }
        //Document object for NLP
        row = String.join(" ", rowArrayCopy);
        // System.out.println("Row after: " + row);
        return row;
    }

    public static void data_exploration(String input_file) {
        //Counters
        // HashMap<String,Integer> HashtagCounter = new HashMap<>();
        // HashMap<String,Integer> AtCounter = new HashMap<>();
        //Opening file
        try {
            CSVReader csvReader = new CSVReader(new FileReader(input_file));
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                System.out.println(row[0] + row[1]);
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

    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("Invalid cmd line arguments.");
            return;
        }
        String csvFile = args[0];
        String stopwordsFile = args[1];
        add_to_stopWords(stopwordsFile);
        data_exploration(csvFile);
    }
}

/*
        //creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        //String text = "What time is it in New York City?";

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println(String.format("Print: word: [%s] pos: [%s] ne: [%s]", word, pos, ne));
            }
        }
        
/*
        //SIMPLE CLASS - TWO MAJOR - Sentence and Document
        Sentence s = new Sentence("Lucy is in the sky with diamonds.");
        List<String> nerTags = s.nerTags();  // [PERSON, O, O, O, O, O, O, O]
        String firstPOSTag = s.posTag(0);   // NNP
        
        
        Document doc = new Document(text);
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
            // When we ask for the lemma, it will load and run the part of speech tagger
            System.out.println("The third lemma of the sentence '" + sent + "' is " + sent.lemmas());
            // When we ask for the parse, it will load and run the parser
            System.out.println("The parse of the sentence '" + sent + "' is " + sent.parse());
            // ...
        }
*/

// for (int i = 0; i < rowArray.size(); i++){
//     String word = rowArray.get(i);
//     List<IndexedWord> parent = dependencyParse.getAllNodesByWordPattern(word);
//     for (IndexedWord pw : parent){
//         for (IndexedWord cw : dependencyParse.getChildList(pw)) {
//             SemanticGraphEdge edge = dependencyParse.getEdge(pw, cw);
//             if (edge.getRelation().getShortName() == ""){

//             }
//         }
//     }
// }

// for (int i = 0; i < rowArray.size()-2; i++){
//     System.out.println(rowArray.get(i));
//     System.out.println(rowArray.get(i+1));
//     System.out.println(rowArray.get(i+2));


//     IndexedWord word1 = dependencyParse.getNodeByWordPattern(rowArray.get(i));
//     IndexedWord word2 = dependencyParse.getNodeByWordPattern(rowArray.get(i+1));
//     IndexedWord word3 = dependencyParse.getNodeByWordPattern(rowArray.get(i+2));
//     System.out.println(word1.value());
//     System.out.println(word2.value());
//     System.out.println(word2.value());
//     SemanticGraphEdge edge1 = dependencyParse.getEdge(word1, word2);
//     SemanticGraphEdge edge2 = dependencyParse.getEdge(word3, word2);

//     if (edge1 != null && edge2 != null){
//         System.out.println(edge1.getRelation().getShortName());
//         System.out.println(edge2.getRelation().getShortName());

//         if (edge1.getRelation().getShortName() == "nsubj" && edge2.getRelation().getShortName() == "obj") {
//             String word = word1.word() + " " + word2.word() + " " + word3.word();
//             System.out.println(word);
//         }
//     }
// }
// dependencyParse.getNodeByWordPattern(pattern);


// javac -cp "../lib/stanford-corenlp-4.2.0/stanford-corenlp-4.2.0.jar" App.java