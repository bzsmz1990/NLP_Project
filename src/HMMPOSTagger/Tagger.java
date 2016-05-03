package HMMPOSTagger;

import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.lang.String;

/**
 * Created by Wenzhao on 5/2/16.
 */

public class Tagger  {
    private static HashMap<Integer, Integer> transition;
    private static HashMap<Integer, HashMap<String, Integer>> emit;
    private static HashMap<Integer, String> intToTag;
    private static HashMap<String, Integer> tagToInt;
    private static HashMap<String, String> dict;
    private static int total;
    private static double[] lambda = new double[3];;

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Tagger takes 5 arguments:  java Tagger training_path testing_path result_path"
                               + "dict_path mapping_path");
            System.exit(1);
        }
        transition = new HashMap<Integer, Integer>();
        emit = new HashMap<Integer, HashMap<String, Integer>>();
        intToTag = new HashMap<Integer, String>();
        tagToInt = new HashMap<String, Integer>();
        training(args[0]);
        loadInDict(args[3], args[4]);
        testing(args[1], args[2]);
    }

    private static void training(String path) {
        Scanner sc = new Scanner(System.in);
        try {
            sc = new Scanner(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found!");
            System.exit(1);
        }
        System.out.println("Training...");
        total = 0;
        int first = 1, second = 2;
        intToTag.put(1, "start-2");
        intToTag.put(2, "start-1");
        tagToInt.put("start-2", 1);
        tagToInt.put("start-1", 2);
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            int third = 0;
            //if it encounters the end of a sentence
            if (s.length() == 0) {
                if (tagToInt.containsKey("end")) {
                    third = tagToInt.get("end");
                }
                else {
                    third = intToTag.size() + 1;
                    intToTag.put(third, "end");
                    tagToInt.put("end", third);
                }
            }
            else {
                total++;
                //System.out.println(s);
                String[] tokens = s.split("\\s+");
                if (tokens.length != 2) {
                    System.err.println ("format error!");
                    System.exit(1);
                }
                String word = tokens[0];
                String tag = tokens[1];
                //if it's a newly encountered tag
                if (tagToInt.containsKey(tag)) {
                    third = tagToInt.get(tag);
                }
                else {
                    third = intToTag.size() + 1;
                    intToTag.put(third, tag);
                    tagToInt.put(tag, third);
                }
                //update the word list of the current tag
                HashMap<String, Integer> tagHash = new HashMap<String, Integer>();
                if (emit.containsKey(third)) {
                    tagHash = emit.get(third);
                }
                else {
                    emit.put(third, tagHash);
                }
                if (tagHash.containsKey(word)) {
                    tagHash.put(word, tagHash.get(word) + 1);
                }
                else {
                    tagHash.put(word, 1);
                }
                //add a special marker "wordsWithHyphen", which will be used later to estimate
                //the distribution of unknown words with hyphen
                if (word.contains("-")) {
                    if (tagHash.containsKey("wordsWithHyphen")) {
                        tagHash.put("wordsWithHyphen", tagHash.get("wordsWithHyphen") + 1);
                    }
                    else {
                        tagHash.put("wordsWithHyphen", 1);
                    }
                }
            }
            //update the unary count
            if (transition.containsKey(third)) {
                transition.put(third, transition.get(third) + 1);
            }
            else {
                transition.put(third, 1);
            }
            //update the binary count
            int binary = third + 100 * second;
            if (transition.containsKey(binary)) {
                transition.put(binary, transition.get(binary) + 1);
            }
            else {
                transition.put(binary, 1);
            }
            //update the ternary count
            int ternary = binary + 10000 * first;
            if (transition.containsKey(ternary)) {
                transition.put(ternary, transition.get(ternary) + 1);
            }
            else {
                transition.put(ternary, 1);
            }
            //for convenience of further calculation
            if (first == 1 && second == 2) {
                if (transition.containsKey(102)) {
                    transition.put(102, transition.get(102) + 1);
                }
                else {
                    transition.put(102, 1);
                }
                if (transition.containsKey(2)) {
                    transition.put(2, transition.get(2) + 1);
                }
                else {
                    transition.put(2, 1);
                }
            }
            //update for the next iteration
            if (s.length() == 0) {
                //initialize
                first = 1;
                second = 2;
            }
            else {
                //increment
                first = second;
                second = third;
            }
        }
        sc.close();
        //calculateLamda();
        //assign the values determined by validation
        lambda[0] = 0.1;
        lambda[1] = 0.1;
        lambda[2] = 0.8;
    }

    private static void calculateLamda() {
        for (Map.Entry<Integer, Integer> entry : transition.entrySet()) {
            int t1t2t3 = entry.getKey();
            if (t1t2t3 < 10000) {
                continue;
            }
            int t1t2 = t1t2t3 / 100;
            int t2t3 = t1t2t3 % 10000;
            int t2 = t2t3 / 100;
            int t3 = t2t3 % 100;
            int t1t2t3Count = entry.getValue();
            double three = 0;
            if (transition.containsKey(t1t2) && transition.get(t1t2) > 1) {
                three = (double)(t1t2t3Count - 1) / (transition.get(t1t2) - 1);
            }
            double two = 0;
            if (transition.containsKey(t2t3) && transition.containsKey(t2) && transition.get(t2) > 1) {
                two = (double)(transition.get(t2t3) - 1) / (transition.get(t2) - 1);
            }
            double one = 0;
            if (transition.containsKey(t3)) {
                one = (double)(transition.get(t3) - 1) / (total - 1);
            }
            if (three >= Math.max(one, two)) {
                lambda[2] += t1t2t3Count;
            }
            else if (two >= Math.max(one, three)) {
                lambda[1] += t1t2t3Count;
            }
            else {
                lambda[0] += t1t2t3Count;
            }
        }
        //normalize
        double sum = lambda[0] + lambda[1] + lambda[2];
        lambda[0] = lambda[0] / sum;
        lambda[1] = lambda[1] / sum;
        lambda[2] = lambda[2] / sum;
    }

    private static void loadInDict(String dictPath, String mappingPath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mappingPath));
            HashMap<String, String> mapping = new HashMap<String, String>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] data = line.split("\\s+");
                mapping.put(data[0], data[1]);
            }
            reader = new BufferedReader(new FileReader(dictPath));
            dict = new HashMap<String, String>();
            line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] data = line.split("\\s+");
                if (mapping.containsKey(data[1])) {
                    dict.put(data[0], mapping.get(data[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("loadInMap " + e);
        }
    }

    private static void testing(String path, String targetPath) {
        Scanner sc = new Scanner(System.in);
        try {
            sc = new Scanner(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found!");
            System.exit(1);
        }
        System.out.println("Testing...");
        File file = new File(targetPath);
        FileWriter writer = null;
        try {
            file.createNewFile();
            writer = new FileWriter(file);
        } catch (Exception e) {
            System.out.println("Create File Unsuccessful");
            System.exit(1);
        }
        //total number of different tags
        int tagCount = intToTag.size();
        List<String> sentence = new ArrayList<String>();
        while (sc.hasNextLine()) {
            String[] data = sc.nextLine().split("\\s+");
            String word = data[0];
            if (word.length() != 0) {
                sentence.add(word);
            }
            else {
                double[][][] prob = new double[tagCount + 1][sentence.size() + 1][tagCount + 1];
                //scan the sentence word by word
                for (int i = 0; i <= sentence.size(); i++) {
                    //calculate the first item: P(word | tag)
                    HashMap<Integer, Double> emitProb = new HashMap<Integer, Double>();
                    //specifically for words with hyphen
                    HashMap<Integer, Double> hyphenProb = new HashMap<Integer, Double>();
                    //if it's not the end placeholder
                    if (i < sentence.size()) {
                        String current = sentence.get(i);
                        //no need to try the first two start tags
                        for (int third = 3; third <= tagCount; third++) {
                            //no need to try the end tag
                            if (intToTag.get(third).equals("end")) {
                                continue;
                            }
                            //no actual need to check because emit must contain third
                            if (emit.containsKey(third)) {
                                HashMap<String, Integer> wordMap = emit.get(third);
                                if (wordMap.containsKey(current)) {
                                    double result = (double)wordMap.get(current) / transition.get(third);
                                    emitProb.put(third, result);
                                }
                                //if the word contains hyphen, calculate the distribution of the hyphen
                                //marker "wordsWithHyphen", in case the hyphened word is unknown
                                if (current.contains("-")) {
                                    if (wordMap.containsKey("wordsWithHyphen")) {
                                        double result = (double)wordMap.get("wordsWithHyphen") / transition.get(third);
                                        hyphenProb.put(third, result);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        //the end placeholder can be considered as having emit prob of 1.0 for the end tag
                        emitProb.put(tagToInt.get("end"), 1.0);
                    }
                    //if it's an unknown word
                    if (emitProb.size() == 0) {
                        String current = sentence.get(i);
                        //if this unknown word is with hyphen, and the hyphen distribution hashmap is
                        //not empty, then use hyphenProb as the emitProb. If the training data do not
                        //have words with hyphen, hyphenProb will be empty, and in that case the word
                        //with hyphen will be tagged as JJ in the unknownWord method
                        if (current.contains("-") && hyphenProb.size() != 0) {
                            emitProb = hyphenProb;
                        }
                        else {
                            //only assign one single most probable tag, 0.001 is an arbitrary number,
                            //any non-zero number is fine
                            emitProb.put(tagToInt.get(unknownWord(current)), 0.001);
                        }
                    }
                    for (int step = 1; step <= 3; step++) {
                        //step == 2 means that in the original tag assignment (no matter if it's an unknown word or not,
                        //all the transition probabilities are zero)
                        if (step == 2) {
                            emitProb.clear();
                            emitProb.put(tagToInt.get("NN"), 0.001);
                        }
                        //step == 3 means that there is no transition probabilities to an NN, so we have
                        //to assign equal probabilities to all tags (except the two start tags and the one end tag)
                        if (step == 3) {
                            emitProb.clear();
                            for (Map.Entry<String, Integer> entry: tagToInt.entrySet()) {
                                String currentTag = entry.getKey();
                                if (currentTag.equals("start-1") || currentTag.equals("start-2") || currentTag.equals("end")) {
                                    continue;
                                }
                                emitProb.put(tagToInt.get(currentTag), 0.001);
                            }
                        }
                        //for each possible tag of the word
                        for (Map.Entry<Integer, Double> entry : emitProb.entrySet()) {
                            int third = entry.getKey();
                            double currentEmit = entry.getValue();
                            for (int second = 1; second <= tagCount; second++) {
                                //the first word must have first == 1 (start-1), second == 2 (start-2)
                                if (i == 0 && second == 1) {
                                    continue;
                                }
                                if (i == 0 && second > 2) {
                                    break;
                                }
                                //the second word cannot have second <= 2 (start-1 and start-2)
                                if (i >= 1 && second <= 2) {
                                    continue;
                                }
                                for (int first = 1; first <= tagCount; first++) {
                                    //the first word must have first == 1 (start-1)
                                    if (i == 0 && first > 1) {
                                        break;
                                    }
                                    //the second word must have first == 2 (start-2)
                                    if (i == 1 && first == 1) {
                                        continue;
                                    }
                                    if (i == 1 && first > 2) {
                                        break;
                                    }
                                    //the third word cannot have first <= 2 (start-1 and start-2)
                                    if (i >= 2 && first <= 2) {
                                        continue;
                                    }
                                    //calculate the second term: P(first, second)
                                    double prior = 1;
                                    if (i >= 1) {
                                        prior = prob[second][i - 1][first];
                                    }
                                    //calculate the third term 'condition': P(third | first, second), which is
                                    //the weighted sum of firstItem, secondItem and thirdItem
                                    double firstItem = 0;
                                    int t1t2 = first * 100 + second;
                                    if (transition.containsKey(t1t2)) {
                                        int t1t2t3 = t1t2 * 100 + third;
                                        if (transition.containsKey(t1t2t3)) {
                                            firstItem = (double)transition.get(t1t2t3) / transition.get(t1t2);
                                        }
                                    }
                                    double secondItem = 0;
                                    int t2 = second;
                                    int t2t3 = t2 * 100 + third;
                                    if (transition.containsKey(t2t3)) {
                                        secondItem = (double)transition.get(t2t3) / transition.get(t2);
                                    }
                                    double thirdItem = (double)transition.get(third) / total;
                                    double condition = lambda[2] * firstItem + lambda[1] * secondItem + lambda[0] * thirdItem;
                                    //calculate the final result
                                    //the multiplier is selected as avoiding both overflow and underflow of the double type
                                    double result = currentEmit * 100 * prior * condition;
                                    //if at least one result is not zero, then no need to continue with the next step
                                    if (result != 0) {
                                        //less safe to use break, setting step as 3 ensures that the current iteration will finish
                                        //and the loop will then terminate
                                        step = 3;
                                    }
                                    prob[third][i][second] = Math.max(result, prob[third][i][second]);
                                }
                            }
                        }
                    }
                }
                //generate the final tag sequence
                List<String> result = new ArrayList<String>();
                int current = tagToInt.get("end");
                for (int i = sentence.size(); i >= 1; i--) {
                    double largest = 0;
                    int tag = 0;
                    for (int j = 1; j <= tagCount; j++) {
                        if (prob[current][i][j] > largest) {
                            largest = prob[current][i][j];
                            tag = j;
                        }
                    }
                    result.add(0, intToTag.get(tag));
                    current = tag;
                }
                //output the results
                for (int i = 0; i < sentence.size(); i++) {
                    try {
                        writer.write(sentence.get(i) + "\t" + result.get(i) + "\n");
                    } catch (Exception e) {
                        System.out.println("Write to file unsuccessful");
                        System.exit(1);
                    }
                }
                try {
                    writer.write("\n");
                } catch (Exception e) {
                    System.out.println("Write to file unsuccessful");
                    System.exit(1);
                }
                //clear the list for the next sentence
                sentence.clear();
            }
        }
        sc.close();
        try {
            writer.close();
        } catch (Exception e) {
            System.out.println("Writer close unsuccessful");
            System.exit(1);
        }
    }

    private static String unknownWord(String current) {
        String tag = null;
        if (dict.containsKey(current)) {
            tag = dict.get(current);
        }
        else {
            tag = "NR";
        }
        return tag;
    }
}

