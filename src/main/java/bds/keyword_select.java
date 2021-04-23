package bds;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import com.opencsv.CSVReader; // CSV Reader
import com.opencsv.CSVWriter; // CSV Writer
import com.opencsv.exceptions.CsvException;

import java.util.*; // Properties, Hashmap
import edu.stanford.nlp.simple.*; // NLP

public class keyword_select {
    public static String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
    public static String output = "./output/select_result.csv";

    public static String stopwords_file = "./data/stopwords.txt";
    public static Set<String> stopWords;
    public static String[] header = {"Label", "Length", "NEGATIVE", "POSITIVE", "NEUTRAL", "ORGANIZATION", 
        "COUNTRY", "IDEOLOGY", "NNP/NNS", "JJ", "VB", "tax", "president", "russia", 
        "sexual harassment", "national security", "social media", "white house"};

    public static CSVWriter file_writer;

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

    public static void data_exploration(String input_file, String correctness) {
        //Opening file
        try {
            CSVReader csvReader = new CSVReader(new FileReader(input_file));
            String[] row;
            int counter = 0;
            row = csvReader.readNext(); //Removing header
            while ((row = csvReader.readNext()) != null) {
                String text = row[1];
                System.out.println(counter + ": " + row[0] + "\t" + row[2] + "\t" + row[3]);
                //Removing emoji and foreign characters
                text = text.replaceAll(characterFilter,"");
                //NLP - Analyze body only
                sentenceAnalyze(correctness, text);
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

    public static void sentenceAnalyze(String correctness, String line) {
        List<Integer> counter = new ArrayList<>();
        for (int i = 0; i< header.length-1; i++){
            counter.add(0);
        }
        Integer sentenceCount = 0;
        Document doc = new Document(line);
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            sentenceCount += 1;
            // Models load when they are needed
            List<Token> tokens = sent.tokens();
            List<String> lemmas = sent.lemmas(); // Lemma is the word in dictionary, was => be
            List<String> pos = sent.posTags(); //Parts of speech
            List<String> ner = sent.nerTags(); // Name entity recognition
            String sentiment = sent.sentiment().toString(); // Positive Neutral Negative

            //Length header
            counter.set(0, counter.get(0)+tokens.size());

            // System.out.println("Sentiment: " + sentiment);
            switch (sentiment) {
                case "NEGATIVE":
                    counter.set(1, counter.get(1)+tokens.size());
                    break;
                case "POSITIVE":
                    counter.set(2, counter.get(2)+tokens.size());
                    break;
                case "NEUTRAL":
                    counter.set(3, counter.get(3)+tokens.size());
                    break;
            }
        
            if (tokens.size() != pos.size() && pos.size() != lemmas.size() && lemmas.size() != ner.size()) {
                System.out.println("FOUND MISMATCH");
                continue;
            }
            // System.out.println("sent: " + sent);
            // System.out.println("lemma: " + lemmas);
            // System.out.println("pos: " + pos);
            // System.out.println("ner: " + ner);

            for (int i = 0; i < tokens.size(); i++) {
                String word = lemmas.get(i).toLowerCase();
                //Not interested in stop words or characters
                if (stopWords.contains(word) || word.length() <= 1) {
                    counter.set(0, counter.get(0)-1);
                    continue;
                }            
                //Pos Labels
                if (pos.get(i).equals("NNP") || pos.get(i).equals("NNS")){
                    counter.set(7, counter.get(7)+1);
                } else if(pos.get(i).equals("JJ")) {
                    counter.set(8, counter.get(8)+1);
                } else if(pos.get(i).equals("VB")) {
                    counter.set(9, counter.get(9)+1);
                }
        
                //Ner Labels
                if (ner.get(i).equals("ORGANIZATION")){
                    counter.set(4, counter.get(4)+1);
                } else if(ner.get(i).equals("COUNTRY")) {
                    counter.set(5, counter.get(5)+1);
                } else if(ner.get(i).equals("IDEOLOGY")) {
                    counter.set(6, counter.get(6)+1);
                }
            
                //Nouns
                if (word.equals("tax")){
                    counter.set(10, counter.get(10)+1);
                } else if(word.equals("president")) {
                    counter.set(11, counter.get(11)+1);
                } else if(word.equals("russia")) {
                    counter.set(12, counter.get(12)+1);
                }
    
                //Combinational
                if (i < tokens.size()-1) {
                    String newWord = word + " " + lemmas.get(i+1).toLowerCase();
                    


                    if (newWord.equals("sexual harassment")){
                        counter.set(13, counter.get(13)+1);
                    } else if(newWord.equals("national security")) {
                        counter.set(14, counter.get(14)+1);
                    } else if(newWord.equals("social media")) {
                        counter.set(15, counter.get(15)+1);
                    } else if(newWord.equals("white house")) {
                        counter.set(16, counter.get(16)+1);
                    }                 
                }
            }
        }

        // {"Label", "Length", "NEGATIVE", "POSITIVE", "NEUTRAL", "ORGANIZATION", 
        // "COUNTRY", "IDEOLOGY", "NNP/NNS", "JJ", "VB", "tax", "president", "russia", 
        // "sexual harassment", "national security", "social media", "white house"};
        
        String[] row = new String[header.length];
        Double temp;
        row[0] = correctness;
        row[1] = counter.get(0).toString();
        
        temp = Double.valueOf(counter.get(1)) / Double.valueOf(sentenceCount);
        row[2] = temp.toString();
        
        temp = Double.valueOf(counter.get(2)) / Double.valueOf(sentenceCount);
        row[3] = temp.toString();
        
        temp = Double.valueOf(counter.get(3)) / Double.valueOf(sentenceCount);
        row[4] = temp.toString();
        
        for (int i = 4; i < counter.size(); i++){
            temp = Double.valueOf(counter.get(i)) / Double.valueOf(counter.get(0));
            row[1+i] = temp.toString();
        }
        file_writer.writeNext(row);
    }

    public static void main(String[] args) {
        // [fake file] [true file] 
        System.out.println("Running Keyword_select");
        if (args.length != 2){
            System.out.println("Invalid cmd line arguments.");
            return;
        }
        stopWords = add_to_set(stopwords_file);
        String fakeFile = args[0];
        String trueFile = args[1];
        try  {
            file_writer = new CSVWriter(new FileWriter(output));
            file_writer.writeNext(header);
            data_exploration(fakeFile, "fake");
            data_exploration(trueFile, "true");
            file_writer.close();
            System.out.println("Successfully wrote to: " + output);
        } catch (IOException e) {
            System.out.println("Cannot open file: " + output);
            e.printStackTrace();
        } 
    }
}