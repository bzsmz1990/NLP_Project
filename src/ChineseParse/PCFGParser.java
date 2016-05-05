package ChineseParse;

import java.util.*;
import java.io.*;

/**
 * Created by Wenzhao on 5/3/16.
 */
public class PCFGParser {
    private final String USAGE = "java PCFGParser sentenceFilePath ruleFilePath originalTagFilePath saveFilePath";
    private HashMap<String, HashMap<String, Double>> rules =
            new HashMap<String, HashMap<String, Double>>();
    private HashMap<String, Integer> tagToId =
            new HashMap<String, Integer>();
    private HashSet<String> originalTag =
            new HashSet<String>();
    private HashMap<String, HashMap<String, Double>> invertedUnit =
            new HashMap<String, HashMap<String, Double>>();

    public void run(String sentenceFile, String ruleFile, String originalTagFile, String saveFile) {
        prepare(ruleFile, originalTagFile);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sentenceFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            String line;
            List<String> list = new ArrayList<String>();
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    if (list.size() != 0) {
                        String parseResult = CKY(list);
                        writer.write(parseResult + "\n");
                        count++;
                        System.out.println(parseResult);
                        System.out.println("processed " + count + " sentences");
                        list.clear();
                    }
                }
                else {
                    list.add(line);
                }
            }
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void prepare(String ruleFile, String originalTagFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\s+");
                for (int i = 0; i <= data.length - 2; i++) {
                    assignID(data[i]);
                }
                String left = data[0];
                double prob = Double.parseDouble(data[data.length - 1]);
                List<String> rightList = new ArrayList<String>();
                for (int i = 1; i <= data.length - 2; i++) {
                    rightList.add(data[i]);
                }
                String rightCombo = organize(rightList);
                HashMap<String, Double> innerMap;
                if (rules.containsKey(left)) {
                    innerMap = rules.get(left);
                }
                else {
                    innerMap = new HashMap<String, Double>();
                    rules.put(left, innerMap);
                }
                innerMap.put(rightCombo, prob);
                if (data.length == 3) {
                    String right = data[1];
                    HashMap<String, Double> inner;
                    if (invertedUnit.containsKey(right)) {
                        inner = invertedUnit.get(right);
                    }
                    else {
                        inner = new HashMap<String, Double>();
                        invertedUnit.put(left, inner);
                    }
                    inner.put(left, prob);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("read in rule file not successful");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(originalTagFile));
            String line;
            while ((line = reader.readLine()) != null) {
                originalTag.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println();
        }
    }

    private void assignID(String tag) {
        if (!tagToId.containsKey(tag)) {
            int id = tagToId.size();
            tagToId.put(tag, id);
        }
    }

    private String CKY(List<String> input) {
        int length = input.size();
        String[] words = new String[length];
        String[] posTag = new String[length];
        for (int i = 0; i < input.size(); i++) {
            String[] data = input.get(i).split("\\s+");
            words[i] = data[0];
            posTag[i] = data[1];
        }
        int tCount = tagToId.size();
        PCFGNode[][][] dp = new PCFGNode[length][length][tCount];
        for (int j = 0; j < length; j++) {
            // initialize one-word probability
            String pos = posTag[j];
            for (Map.Entry<String, HashMap<String, Double>> entry: rules.entrySet()) {
                String left = entry.getKey();
                HashMap<String, Double> temp = entry.getValue();
                for (Map.Entry<String, Double> tempEntry: temp.entrySet()) {
                    String right = tempEntry.getKey();
                    if (right.equals(pos)) {
                        double prob = tempEntry.getValue();
                        List<PCFGNode> children = new ArrayList<PCFGNode>();
                        dp[j][j][getID(left)] = new PCFGNode(left, words[j], children, prob);
                    }
                }
            }
            // go up
            for (int i = j - 1; i >= 0; i--) {
                for (int k = i; k < j; k++) {
                    for (Map.Entry<String, HashMap<String, Double>> entry: rules.entrySet()) {
                        String left = entry.getKey();
                        HashMap<String, Double> temp = entry.getValue();
                        for (Map.Entry<String, Double> tempEntry: temp.entrySet()) {
                            String right = tempEntry.getKey();
                            double ruleProb = tempEntry.getValue();
                            String[] separate = right.split("\t");
                            if (separate.length == 1) {
                                continue;
                            }
                            int idOne = getID(separate[0]);
                            int idTwo = getID(separate[1]);
                            int idLeft = getID(left);
                            if (dp[i][k][idOne] != null && dp[k + 1][j][idTwo] != null) {
                                double prob = ruleProb * dp[i][k][idOne].prob * dp[k + 1][j][idTwo].prob;
                                if (dp[i][j][idLeft] != null && dp[i][j][idLeft].prob > prob) {
                                    continue;
                                }
                                List<PCFGNode> children = new ArrayList<PCFGNode>();
                                children.add(dp[i][k][idOne]);
                                children.add(dp[k + 1][j][idTwo]);
                                dp[i][j][idLeft] = new PCFGNode(left, null, children, prob);
                                calculateUnit(dp, idLeft, i, j);
                            }
                        }
                    }
                }
            }
        }
        return generateResult(dp);
    }

    private void calculateUnit(PCFGNode[][][] dp, int tagID, int leftIndex, int rightIndex) {
        PCFGNode node = dp[leftIndex][rightIndex][tagID];
        if (!invertedUnit.containsKey(node.thisTag)) {
            return;
        }
        HashMap<String, Double> innerMap = invertedUnit.get(node.thisTag);
        List<String> ancestors = new ArrayList<String>();
        for (String left: innerMap.keySet()) {
            ancestors.add(left);
        }
        for (String left: ancestors) {
            double addedProb = innerMap.get(left);
            double prob = addedProb * node.prob;
            if (dp[leftIndex][rightIndex][getID(left)] != null &&
                    dp[leftIndex][rightIndex][getID(left)].prob > prob) {
                continue;
            }
            List<PCFGNode> children = new ArrayList<PCFGNode>();
            children.add(dp[leftIndex][rightIndex][tagID]);
            dp[leftIndex][rightIndex][getID(left)] = new PCFGNode(left, null, children, prob);
            calculateUnit(dp, getID(left), leftIndex, rightIndex);
        }
    }

    private String generateResult(PCFGNode[][][] dp) {
        PCFGNode root = null;
        double maxProb = 0;
        int n = dp.length;
        for (int i = 0; i < dp[0][n - 1].length; i++) {
            if (dp[0][n - 1][i] != null && dp[0][n - 1][i].prob > maxProb) {
                maxProb = dp[0][n - 1][i].prob;
                root = dp[0][n - 1][i];
            }
        }
        if (root == null) {
            return "ERROR";
        }
        eliminateAddedTags(root);
        String result = assembleResult(root);
        return "( (" + result + "))";
    }

    private void eliminateAddedTags(PCFGNode node) {
        List<PCFGNode> children = node.children;
        // in this case, this node must be terminal
        if (children.size() == 0) {
            return;
        }
        // in this case, need to make sure all the node's children are original tags, before
        // moving down to the next level
        else {
            int pos = 0;
            while (pos < children.size()) {
                String thisTag = children.get(pos).thisTag;
                if (originalTag.contains(thisTag)) {
                    pos++;
                }
                else {
                    List<PCFGNode> subChildren = children.get(pos).children;
                    children.remove(pos);
                    for (int i = subChildren.size() - 1; i >= 0; i--) {
                        children.add(pos, subChildren.get(i));
                    }
                }
            }
            for (int i = 0; i < children.size(); i++) {
                eliminateAddedTags(children.get(i));
            }
        }
    }

    private String assembleResult(PCFGNode node) {
        StringBuilder builder = new StringBuilder();
        if (node.children.size() == 0) {
            builder.append(node.thisTag + " " + node.thisWord);
        }
        else {
            builder.append(node.thisTag);
            for (PCFGNode childNode: node.children) {
                builder.append(" (" + assembleResult(childNode) + ")");
            }
        }
        return builder.toString();
    }

    private String organize(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String s: list) {
            builder.append(s + "\t");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private int getID(String t) {
        return tagToId.get(t);
    }

    public static void main(String[] args) {
        String sentenceFile = args[0];
        String ruleFile = args[1];
        String originalTagFile = args[2];
        String saveFile = args[3];
        PCFGParser parser = new PCFGParser();
        parser.run(sentenceFile, ruleFile, originalTagFile, saveFile);
    }
}

