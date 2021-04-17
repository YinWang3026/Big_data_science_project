import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*; //Properties, Hashmap
// import java.lang.*;
import java.util.ArrayList;
//import edu.stanford.nlp.simple.*;
import edu.stanford.nlp.ling.*; //CoreAnnoations
import edu.stanford.nlp.pipeline.*; //StanfordCoreNLP, Annotation, CoreDocument
import edu.stanford.nlp.util.*; //CoreMap, CollectionUtils
import edu.stanford.nlp.semgraph.*; //Dependency parser
// import edu.stanford.nlp.trees.*;

public class App {
    // public static String text = "Joe Smith was born in California. " +
    //   "In 2017, he went to Paris, France in the summer. " +
    //   "His flight left at 3:00pm on July 10th, 2017. " +
    //   "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
    //   "He sent a postcard to his sister Jane Smith. " +
    //   "After hearing about Joe's trip, Jane decided she might go to France one day.";

    public static String delim = " |,|\"|=|%|^|&|\t|;|\\.|\\?|!|-|:|\\[|\\]|\\(|\\)|\\{|\\}|\\*|/";
    public static String sentimentFile = "";
    public static String stopWordsFile = "./data/stopwords.txt";
    public static String atFile = "./output/top_20_at.csv";
    public static String hashTagFile = "./output/top_20_hashtag.csv";
    public static String ngram = "./output/ngram.csv";
    public static String ngram_pos0 = "./output/ngram_POS0.csv";
    public static String ngram_pos1 = "./output/ngram_POS1.csv";
    public static String ngram_pos2 = "./output/ngram_POS2.csv";
    public static String ngram_pos3 = "./output/ngram_POS3.csv";
    public static String ner_out = "./output/ngram_NER.csv";
    public static String depparse_out = "./output/ngram_DEP.csv";
    public static String ngram_pos_adj_nn = "./output/ngram_pos_adjnn.csv";
    public static Set<String> stopWords = new HashSet<String>();

    public static void add_to_stopWords(){
        //Opening file
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(stopWordsFile));
            String row;
            while ((row = buffReader.readLine()) != null){
                stopWords.add(row);
            }
            buffReader.close();
        } catch (IOException e) {
            System.out.println("Cannot open stopWordsFile.");
            e.printStackTrace();
        }
    }

    public static void write_to_file(String filename, HashMap<String,Integer> map) {
        // List<Pair<String, Integer>> array = new ArrayList<Pair<String, Integer>>();
        // for (String key: map.keySet()) {
        //     Integer value = map.get(key);
        //     Pair<String, Integer> pair = new Pair<String, Integer>(key,value);
        //     Boolean added = false;
        //     for(Integer i = 0; i < array.size(); i++){
        //         if (pair.second <= array.get(i).second){
        //             array.add(i,pair);
        //             added = true;
        //             break;
        //         }
        //     }
        //     if (added == false){
        //         array.add(pair);
        //     }
        // }
        try {
            FileWriter myWriter = new FileWriter(filename);
            // for(Integer i = 0; i < array.size(); i++){
            //     myWriter.write(i.toString() + "," + array.get(i).first + "," + array.get(i).second.toString() + "\n");
            // }
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

    public static void data_exploration() {
        //Counters
        HashMap<String,Integer> HashtagCounter = new HashMap<>();
        HashMap<String,Integer> AtCounter = new HashMap<>();
        //Opening file
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            while ((row = buffReader.readLine()) != null){
                //Spliting CSV
                //Concatenate everything from index 5 to the end
                List<String> rowArray = Arrays.asList(row.split(","));
                row = String.join("",rowArray.subList(5,rowArray.size()));
                //Spliting sentence
                for (String word : row.split(delim)){ 
                    word = word.toLowerCase();
                    if (word.length() > 0) {
                        // System.out.println(word);
                        //Count @
                        if (word.charAt(0) == '@') {
                            if(AtCounter.containsKey(word)) {
                                AtCounter.replace(word, AtCounter.get(word)+1);
                            } else {
                                AtCounter.put(word, 1);
                            }
                        //Count #
                        } else if (word.charAt(0) == '#') {
                            if(HashtagCounter.containsKey(word)) {
                                HashtagCounter.replace(word, HashtagCounter.get(word)+1);
                            } else {
                                HashtagCounter.put(word, 1);
                            }                       
                        }
                    }
                }
            }
            //Closing file
            buffReader.close();
            //Write result to file
            write_to_file(hashTagFile, HashtagCounter);
            write_to_file(atFile, AtCounter);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }

    public static void n_gram_analysis(int ngramS, int ngramE) {
        //Annotator
        //tokenize - Tokenizer Annotator
        //ssplit - Words to Sentences Annotator
        String mode = "tokenize, ssplit";
        Properties props = new Properties();
        props.setProperty("annotators", mode);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //Counter
        HashMap<String,Integer> nGramCounter = new HashMap<>();
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            while ((row = buffReader.readLine()) != null){
                row = sentence_cleaning(row);
                //Document object for NLP
                CoreDocument doc = new CoreDocument(row);
                pipeline.annotate(doc); //Getting sentences
                for (CoreSentence sent : doc.sentences()) {
                    List<String> rowArrayCopy = Arrays.asList(sent.text().split(" "));
                    // System.out.println("sent: " + sent.text());
                    // System.out.println("rowArrayCopy: " + rowArrayCopy);

                    //N-gram list
                    List<List<String>> ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                    // System.out.println("ngram: "+ ngramList);

                    //Adding n-gram to dictionary
                    for (int i = 0; i < ngramList.size(); i++){
                        String word = String.join(" ",ngramList.get(i));
                        // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                        if(nGramCounter.containsKey(word)) {
                            nGramCounter.replace(word, nGramCounter.get(word)+1);
                        } else {
                            nGramCounter.put(word, 1);
                        }      
                    }
                }
            }
            //Closing file
            buffReader.close();
            write_to_file(ngram, nGramCounter);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }
    public static void n_gram_analysis_pos(int ngramS, int ngramE) {
        //Annotator
        //tokenize - Tokenizer Annotator
        //ssplit - Words to Sentences Annotator
        //pos - Parts of Speech - token, ssplit
        String mode = "tokenize, ssplit, pos";
        Properties props = new Properties();
        props.setProperty("annotators", mode);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //Counter
        HashMap<String,Integer> nGramCounter0 = new HashMap<>(); //noun
        HashMap<String,Integer> nGramCounter1 = new HashMap<>(); //adj noun
        HashMap<String,Integer> nGramCounter2 = new HashMap<>(); //adv verb
        HashMap<String,Integer> nGramCounter3 = new HashMap<>(); //pronoun verb
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            List<List<String>> ngramList; 
            while ((row = buffReader.readLine()) != null){
                row = sentence_cleaning(row);
                // System.out.println("row: " + row);
                CoreDocument doc = new CoreDocument(row);
                pipeline.annotate(doc); //Getting sentences
                for (CoreSentence sent : doc.sentences()) {
                    //Getting the POS tags for each word in sentence
                    List<String> posTags = sent.posTags();
                    // System.out.println("Sent: " + sent.text());
                    // System.out.println("POS: " + posTags);
                    List<String> rowArray = Arrays.asList(sent.text().split(" "));
                    if (rowArray.size() > posTags.size()) {
                        continue;
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////
                    //Grabbing the nouns
                    List<String> rowArrayCopy = new ArrayList<String>();
                    for(int i = 0; i < rowArray.size(); i++){
                        //PoS = Noun
                        if (posTags.get(i).equals("NN") || posTags.get(i).equals("NNS") ||
                            posTags.get(i).equals("NNP") ||posTags.get(i).equals("NNPS")){ 
                            rowArrayCopy.add(rowArray.get(i));
                        }
                    }
                    //System.out.println("NN: " + rowArrayCopy);

                    //N-gram list
                    ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                    //System.out.println("ngram: "+ ngramList);

                    //Adding n-gram to dictionary
                    for (int i = 0; i < ngramList.size(); i++){
                        String word = String.join(" ",ngramList.get(i));
                        // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                        if(nGramCounter0.containsKey(word)) {
                            nGramCounter0.replace(word, nGramCounter0.get(word)+1);
                        } else {
                            nGramCounter0.put(word, 1);
                        }      
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////
                    rowArrayCopy = new ArrayList<String>();
                    for(int i = 0; i < rowArray.size()-1; i++){
                        //PoS = ADJ Nonn
                        String pos1 = posTags.get(i);
                        String pos2 = posTags.get(i+1);
                        if ((pos1.equals("JJ") || pos1.equals("JJR") || pos1.equals("JJS")) &&
                            (pos2.equals("NN") || pos2.equals("NNS") || pos2.equals("NNP") || pos2.equals("NNPS"))){
                            rowArrayCopy.add(rowArray.get(i) + " " + rowArray.get(i+1));
                        }
                    }
                    //System.out.println("rowArrayCopy: " + rowArrayCopy);

                    //N-gram list
                    ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                    //System.out.println("ngram: "+ ngramList);

                    //Adding n-gram to dictionary
                    for (int i = 0; i < ngramList.size(); i++){
                        String word = String.join(" ",ngramList.get(i));
                        // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                        if(nGramCounter1.containsKey(word)) {
                            nGramCounter1.replace(word, nGramCounter1.get(word)+1);
                        } else {
                            nGramCounter1.put(word, 1);
                        }      
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////
                    rowArrayCopy = new ArrayList<String>();
                    for(int i = 0; i < rowArray.size()-1; i++){
                        //PoS = adv verb
                        String pos1 = posTags.get(i);
                        String pos2 = posTags.get(i+1);
                        if ((pos1.equals("RB") || pos1.equals("RBR") || pos1.equals("RBS")) &&
                            (pos2.equals("VB") || pos2.equals("VBD") || pos2.equals("VBG") || pos2.equals("VBN") || pos2.equals("VBP") || pos2.equals("VBZ"))){
                            rowArrayCopy.add(rowArray.get(i) + " " + rowArray.get(i+1));
                            }
                    }
                    //System.out.println("rowArrayCopy: " + rowArrayCopy);

                    //N-gram list
                    ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                    //System.out.println("ngram: "+ ngramList);

                    //Adding n-gram to dictionary
                    for (int i = 0; i < ngramList.size(); i++){
                        String word = String.join(" ",ngramList.get(i));
                        // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                        if(nGramCounter2.containsKey(word)) {
                            nGramCounter2.replace(word, nGramCounter2.get(word)+1);
                        } else {
                            nGramCounter2.put(word, 1);
                        }      
                    }
////////////////////////////////////////////////////////////////////////////////////////////////////
                    rowArrayCopy = new ArrayList<String>();
                    for(int i = 0; i < rowArray.size()-1; i++){
                        //PoS = pronoun verb
                        String pos1 = posTags.get(i);
                        String pos2 = posTags.get(i+1);
                        if ((pos1.equals("PRP") || pos1.equals("PRP$")) &&
                            (pos2.equals("VB") || pos2.equals("VBD") || pos2.equals("VBG") || pos2.equals("VBN") || pos2.equals("VBP") || pos2.equals("VBZ"))){
                            rowArrayCopy.add(rowArray.get(i) + " " + rowArray.get(i+1));
                            }
                    }
                    //System.out.println("rowArrayCopy: " + rowArrayCopy);

                    //N-gram list
                    ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                    //System.out.println("ngram: "+ ngramList);

                    //Adding n-gram to dictionary
                    for (int i = 0; i < ngramList.size(); i++){
                        String word = String.join(" ",ngramList.get(i));
                        // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                        if(nGramCounter3.containsKey(word)) {
                            nGramCounter3.replace(word, nGramCounter3.get(word)+1);
                        } else {
                            nGramCounter3.put(word, 1);
                        }      
                    }
                }
            }
            //Closing file
            buffReader.close();
            write_to_file(ngram_pos0, nGramCounter0);
            write_to_file(ngram_pos1, nGramCounter1);
            write_to_file(ngram_pos2, nGramCounter2);
            write_to_file(ngram_pos3, nGramCounter3);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }
    public static void n_gram_pos_adj_nn(int ngramS, int ngramE) {
        //Annotator
        //tokenize - Tokenizer Annotator
        //ssplit - Words to Sentences Annotator
        //pos - Parts of Speech - token, ssplit
        String mode = "tokenize, ssplit, pos";
        Properties props = new Properties();
        props.setProperty("annotators", mode);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //Counter
        HashMap<String,Integer> nGramCounter = new HashMap<>(); //noun
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            List<List<String>> ngramList; 
            while ((row = buffReader.readLine()) != null){
                // System.out.println("row: " + row);
                List<String> temp = Arrays.asList(row.split(","));
                // System.out.println("row: " + temp);
                if (temp.get(0).equals("\"4\"") || temp.get(0).equals("\"0\"")){
                    row = sentence_cleaning(row);
                    // System.out.println("row: " + row);
                    CoreDocument doc = new CoreDocument(row);
                    pipeline.annotate(doc); //Getting sentences
                    for (CoreSentence sent : doc.sentences()) {
                        //Getting the POS tags for each word in sentence
                        List<String> posTags = sent.posTags();
                        // System.out.println("Sent: " + sent.text());
                        // System.out.println("POS: " + posTags);
                        List<String> rowArray = Arrays.asList(sent.text().split(" "));
                        if (rowArray.size() > posTags.size()) {
                            continue;
                        }
                        List<String> rowArrayCopy = new ArrayList<String>();
                        rowArrayCopy = new ArrayList<String>();
                        for(int i = 0; i < rowArray.size()-1; i++){
                            //PoS = ADJ Nonn
                            String pos1 = posTags.get(i);
                            String pos2 = posTags.get(i+1);
                            if ((pos1.equals("JJ") || pos1.equals("JJR") || pos1.equals("JJS")) &&
                                (pos2.equals("NN") || pos2.equals("NNS") || pos2.equals("NNP") || pos2.equals("NNPS"))){
                                rowArrayCopy.add(rowArray.get(i) + " " + rowArray.get(i+1));
                            }
                        }
                        //System.out.println("rowArrayCopy: " + rowArrayCopy);

                        //N-gram list
                        ngramList = CollectionUtils.getNGrams(rowArrayCopy, ngramS, ngramE);
                        //System.out.println("ngram: "+ ngramList);

                        //Adding n-gram to dictionary
                        for (int i = 0; i < ngramList.size(); i++){
                            String word = String.join(" ",ngramList.get(i));
                            // System.out.println("Word: "+ word + " " + ngramList.get(i).size());
                            if(nGramCounter.containsKey(word)) {
                                nGramCounter.replace(word, nGramCounter.get(word)+1);
                            } else {
                                nGramCounter.put(word, 1);
                            }      
                        }
                    }
                }
            }
            //Closing file
            buffReader.close();
            write_to_file(ngram_pos_adj_nn, nGramCounter);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }
    public static void analysis_depparse() {
        //Annotator
        //tokenize - Tokenizer Annotator
        //ssplit - Words to Sentences Annotator
        //pos - Parts of Speech - token, ssplit
        //depparse - Dependency Parse Annotator - tokenize, ssplit, pos
        String mode = "tokenize, ssplit, pos, depparse";
        Properties props = new Properties();
        props.setProperty("annotators", mode);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //Counter
        HashMap<String,Integer> nGramCounter = new HashMap<>();
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            while ((row = buffReader.readLine()) != null){
                row = sentence_cleaning(row); 
                //Document object for NLP
                CoreDocument doc = new CoreDocument(row);
                pipeline.annotate(doc); //Getting sentences
                for (CoreSentence sent : doc.sentences()) {
                    //Depparse
                    SemanticGraph dependencyParse = sent.dependencyParse();
                    // System.out.println("Sent: " + sent.text());
                    // System.out.println(dependencyParse);
                    //Tried to parse the tree ...        
                    IndexedWord r = dependencyParse.getFirstRoot();
                    for (IndexedWord cw : dependencyParse.getChildList(r)) {
                        SemanticGraphEdge edge = dependencyParse.getEdge(r, cw);
                        String shortName = edge.getRelation().getShortName();
                        if (shortName == "nsubj" || shortName == "obj"){
                            String word = r.word() + " " + cw.word();
                            if(nGramCounter.containsKey(word)) {
                                nGramCounter.replace(word, nGramCounter.get(word)+1);
                            } else {
                                nGramCounter.put(word, 1);
                            }
                        }
                    }
                }
            }
            //Closing file
            buffReader.close();
            write_to_file(depparse_out, nGramCounter);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }
    public static void analysis_ner() {
        //Annotator
        //tokenize - Tokenizer Annotator
        //ssplit - Words to Sentences Annotator
        //pos - Parts of Speech - token, ssplit
        //lemma - Morpha Annotator, token, ssplit, pos
        //ner - Named Entity Tag Annotator - token, ssplit, pos lemma
        String mode = "tokenize, ssplit, pos, lemma, ner";
        Properties props = new Properties();
        props.setProperty("annotators", mode);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        //Counter
        HashMap<String,Integer> nerCounter = new HashMap<>();
        try {
            BufferedReader buffReader = new BufferedReader(new FileReader(sentimentFile));
            String row;
            while ((row = buffReader.readLine()) != null){
                row = sentence_cleaning(row);
                //Document object for NLP
                CoreDocument doc = new CoreDocument(row);
                pipeline.annotate(doc); //Getting sentences
                for (CoreSentence sent : doc.sentences()) {
                    // Getting the NER tags for each word in sentence
                    List<String> nerTags = sent.nerTags();
                    // System.out.println("Sent: " + sent.text());
                    // System.out.println("NER: " + nerTags);
                    
                    //Adding NER to counter
                    for (int i = 0; i < nerTags.size(); i++){
                        String word = nerTags.get(i);
                        if(nerCounter.containsKey(word)) {
                            nerCounter.replace(word, nerCounter.get(word)+1);
                        } else {
                            nerCounter.put(word, 1);
                        }    
                    }
                }
            }
            //Closing file
            buffReader.close();
            write_to_file(ner_out, nerCounter);
        } catch (IOException e) {
            System.out.println("Cannot open sentimentFile.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println("Invalid cmd line arguments.");
            return;
        }
        add_to_stopWords();
        switch (args[1]){
            case "nocopy":
                sentimentFile = "./data/sentiment140.csv";
                break;
            case "copy":
                sentimentFile = "./data/sentiment140_copy.csv";
                break;
        }
        switch (args[0]){
            case "data_exploration":
                data_exploration();
                break;
            case "n_gram":
                n_gram_analysis(1, 4);
                break;
            case "n_gram_pos":
                n_gram_analysis_pos(1, 4);
                break;
            case "analysis_depparse":
                analysis_depparse();
                break;
            case "analysis_ner":
                analysis_ner();
                break;
            case "pos_adjnn":
                n_gram_pos_adj_nn(1,4);
                break;
            default:
                System.out.println("Invalid algo");
                break;
        }  
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