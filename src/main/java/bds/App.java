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
    public static String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
   
    public static String output = "./output/";
    public static String stopwords_file = "./data/stopwords.txt";
    public static String stopwords_def_file = "./data/stopwords_default.txt";

    public static Set<String> stopWords;
    public static Set<String> stopWords_default;
    public static HashMap<String,Integer> posLabels = new HashMap<>();
    public static HashMap<String,Integer> nerLabels = new HashMap<>();
    public static HashMap<String,Integer> nouns = new HashMap<>();
    public static HashMap<String,Integer> nerner = new HashMap<>();
    public static HashMap<String,Integer> pos_adj_nn = new HashMap<>();
    public static HashMap<String,Integer> pos_pron_vb = new HashMap<>();
    public static HashMap<String,Integer> pos_adv_vb = new HashMap<>();

    public static Boolean genLabelFlag = false;

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

    public static void write_hashmap_to_file(String filename, HashMap<String,Integer> map) {
        try {
            FileWriter myWriter = new FileWriter(output+filename);
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

    // public static void write_set_to_file(String filename, Set<String> s) {
    //     try {
    //         FileWriter myWriter = new FileWriter(output+filename);
    //         for (String key : s){
    //             myWriter.write(key + ",");
    //         }
    //         myWriter.close();
    //         System.out.println("Successfully wrote to: " + filename);
    //     } catch (IOException e) {
    //         System.out.println("Write to file failed: " + filename);
    //         e.printStackTrace();
    //     }
    // }

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
                // String subject = row[2]; 

                System.out.println(counter + ": " + row[0] + "\t" + row[2] + "\t" + row[3]);
                //Removing emoji and foreign characters
                title = title.replaceAll(characterFilter,"");
                text = text.replaceAll(characterFilter,"");
                //NLP
                sentenceAnalyze(title);
                sentenceAnalyze(text);
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
            // Models load when they are needed
            List<Token> tokens = sent.tokens();
            List<String> lemmas = sent.lemmas(); // Lemma is the word in dictionary, was => be
            List<String> pos = sent.posTags(); //Parts of speech
            List<String> ner = sent.nerTags(); // Name entity recognition
            String sentiment = sent.sentiment().toString(); // Positive Neutral Negative

            if (tokens.size() != pos.size() && pos.size() != lemmas.size() && lemmas.size() != ner.size()) {
                System.out.println("FOUND MISMATCH");
                continue;
            }
            // System.out.println("sent: " + sent);
            // System.out.println("lemma: " + lemmas);
            // System.out.println("pos: " + pos);
            // System.out.println("ner: " + ner);

            // System.out.println("sentiment: " + sentiment);

            for (int i = 0; i < tokens.size(); i++) {
                String word = lemmas.get(i).toLowerCase();
                //Not interested in stop words or characters
                if (stopWords.contains(word) || word.length() <= 1) {
                    continue;
                }
                if (genLabelFlag){
                    //Pos Labels
                    int count = posLabels.containsKey(pos.get(i)) ? posLabels.get(pos.get(i)) : 0;
                    posLabels.put(pos.get(i), count + 1);
                    //Ner Labels
                    count = nerLabels.containsKey(ner.get(i)) ? nerLabels.get(ner.get(i)) : 0;
                    nerLabels.put(ner.get(i), count + 1);
                    //Nouns
                    if (pos.get(i).equals("NN") || pos.get(i).equals("NNS") || pos.get(i).equals("NNP") ||pos.get(i).equals("NNPS")){
                        count = nouns.containsKey(word) ? nouns.get(word) : 0;
                        nouns.put(word, count + 1);
                    }

                    //Combinational
                    if (i < tokens.size()-1) {
                        String pos1 = pos.get(i);
                        String pos2 = pos.get(i+1);
                        String newWord = word + " " + lemmas.get(i+1).toLowerCase();

                        // Adjective Noun - Ex. Fat Cat
                        if ((pos1.equals("JJ") || pos1.equals("JJR") || pos1.equals("JJS")) &&
                            (pos2.equals("NN") || pos2.equals("NNS") || pos2.equals("NNP") || pos2.equals("NNPS"))){ 
                            count = pos_adj_nn.containsKey(newWord) ? pos_adj_nn.get(newWord) : 0;
                            pos_adj_nn.put(newWord, count + 1);
                        } 
                        // Pronoun verb - Ex. I do
                        if ((pos1.equals("PRP") || pos1.equals("PRP$")) &&
                            (pos2.equals("VB") || pos2.equals("VBD") || pos2.equals("VBG") || pos2.equals("VBN") || pos2.equals("VBP") || pos2.equals("VBZ"))){
                            count = pos_pron_vb.containsKey(newWord) ? pos_pron_vb.get(newWord) : 0;
                            pos_pron_vb.put(newWord, count + 1);   
                        }
                        // Adverb verb - Slowly eating
                        if ((pos1.equals("RB") || pos1.equals("RBR") || pos1.equals("RBS")) &&
                            (pos2.equals("VB") || pos2.equals("VBD") || pos2.equals("VBG") || pos2.equals("VBN") || pos2.equals("VBP") || pos2.equals("VBZ"))){
                            count = pos_adv_vb.containsKey(newWord) ? pos_adv_vb.get(newWord) : 0;
                            pos_adv_vb.put(newWord, count + 1);                          
                        }
                        // Ner Ner - Ex. New York
                        if (ner.get(i).equals(ner.get(i+1))) {
                            count = nerner.containsKey(newWord) ? nerner.get(newWord) : 0;
                            nerner.put(newWord, count + 1);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // [true/false] [fake file] [true file] 
        if (args.length != 3){
            System.out.println("Invalid cmd line arguments.");
            return;
        }
        stopWords = add_to_set(stopwords_file);
        stopWords_default = add_to_set(stopwords_def_file);

        genLabelFlag = Boolean.valueOf(args[0]); //true or false
        System.out.println(genLabelFlag);

        String fakeFile = args[1];
        String trueFile = args[2];
        data_exploration(fakeFile, "fake");
        data_exploration(trueFile, "true");

        if (genLabelFlag) {
            write_hashmap_to_file("posLabels.txt", posLabels);
            write_hashmap_to_file("nerLabels.txt", nerLabels);
            write_hashmap_to_file("nouns.txt", nouns);
            write_hashmap_to_file("nerner.txt", nerner);
            write_hashmap_to_file("adj_nn.txt", pos_adj_nn);
            write_hashmap_to_file("pron_vb.txt", pos_pron_vb);
            write_hashmap_to_file("adv_vb.txt", pos_adv_vb);
        }
    }
}