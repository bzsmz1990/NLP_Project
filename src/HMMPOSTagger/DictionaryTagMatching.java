package HMMPOSTagger;

import java.util.*;
import java.io.*;

/**
 * Created by Wenzhao on 5/2/16.
 */
public class DictionaryTagMatching {
    public static void main(String[] args) {
        String trainingFile = args[0];
        String dictFile = args[1];
        String targetFile = args[2];
        HashMap<String, String> training = parseTrain(trainingFile);
        HashMap<String, String> dict = parseDict(dictFile);
        combine(training, dict, targetFile);
    }

    private static HashMap<String, String> parseTrain(String path) {
        HashMap<String, HashMap<String, Integer>> temp =
                new HashMap<String, HashMap<String, Integer>>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] data = line.split("\\s+");
                String word = data[0];
                String tag = data[2];
                HashMap<String, Integer> inner = null;
                if (temp.containsKey(word)) {
                    inner = temp.get(word);
                }
                else {
                    inner = new HashMap<String, Integer>();
                    temp.put(word, inner);
                }
                if (inner.containsKey(tag)) {
                    inner.put(tag, inner.get(tag) + 1);
                }
                else {
                    inner.put(tag, 1);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("parseTrain " + e);
        }
        HashMap<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, HashMap<String, Integer>> entry: temp.entrySet()) {
            String word = entry.getKey();
            int max = 0;
            String maxTag = null;
            HashMap<String, Integer> inner = entry.getValue();
            for (Map.Entry<String, Integer> innerEntry: inner.entrySet()) {
                if (innerEntry.getValue() > max) {
                    max = innerEntry.getValue();
                    maxTag = innerEntry.getKey();
                }
            }
            result.put(word, maxTag);
        }
        return result;
    }

    private static HashMap<String, String> parseDict(String path) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] data = line.split("\\s+");
                String word = data[0];
                String tag = data[1];
                result.put(word, tag);
            }
        } catch (IOException e) {
            System.out.println("parseDict " + e);
        }
        return result;
    }

    private static void combine(HashMap<String, String> training, HashMap<String, String> dict,
                                String path) {
        HashMap<String, HashMap<String, Integer>> temp =
                new HashMap<String, HashMap<String, Integer>>();
        for (Map.Entry<String, String> entry: training.entrySet()) {
            String word = entry.getKey();
            if (!dict.containsKey(word)) {
                continue;
            }
            String tagTraining = entry.getValue();
            String tagDict = dict.get(word);
            HashMap<String, Integer> inner = null;
            if (temp.containsKey(tagDict)) {
                inner = temp.get(tagDict);
            }
            else {
                inner = new HashMap<String, Integer>();
                temp.put(tagDict, inner);
            }
            if (inner.containsKey(tagTraining)) {
                inner.put(tagTraining, inner.get(tagTraining) + 1);
            }
            else {
                inner.put(tagTraining, 1);
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            for (Map.Entry<String, HashMap<String, Integer>> entry: temp.entrySet()) {
                String tagDict = entry.getKey();
                HashMap<String, Integer> inner = entry.getValue();
                int max = 0;
                String tag = null;
                for (Map.Entry<String, Integer> entryInner: inner.entrySet()) {
                    if (entryInner.getValue() > max) {
                        max = entryInner.getValue();
                        tag = entryInner.getKey();
                    }
                }
                writer.write(tagDict + "\t" + tag + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("combine " + e);
        }
    }
}
