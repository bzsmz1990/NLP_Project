package ChineseParse;

import java.util.*;
import java.io.*;

/**
 * Created by Wenzhao on 5/3/16.
 */
public class PCFGParser {
    private HashMap<String, HashMap<String, Double>> rules =
            new HashMap<String, HashMap<String, Double>>();
    private HashMap<String, Integer> tagToId =
            new HashMap<String, Integer>();
    private HashMap<String, String> deletedRule =
            new HashMap<String, String>();
    private HashSet<String> originalTag =
            new HashSet<String>();

    public void run(String sentenceFile, String ruleFile, String originalTagFile, String saveFile) {
        //prepare(ruleFile, originalTagFile);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sentenceFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            String line;
            List<String> list = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    if (list.size() != 0) {
                        String parseResult = CKY(list);
                        writer.write(parseResult + "\n");
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

//    private void prepare(String ruleFile, String originalTagFile) {
//        // get map
//        for (Map.Entry<NonTerminal, HashMap<>>) {
//            String left = entry.getKey().toString();
//            if (!tagToId.containsKey(left)) {
//                int index = tagToId.size();
//                tagToId.put(left, index);
//                idToTag.put(index, left);
//            }
//            HashMap<Production, Double> temp = entry.getValue();
//            for (Map.Entry<Production, Double> innerEntry: temp.entrySet()) {
//                String[] rightArray = innerEntry.getKey().getArray();
//                if (rightArray.length == 1) {
//                    if (!tagToId.containsKey(rightArray[0])) {
//                        int index = tagToId.size();
//                        tagToId.put(rightArray[0], index);
//                        idToTag.put(index, rightArray[0]);
//                    }
//                }
//                String right = organize(Arrays.asList(rightArray));
//                HashMap<String, Double> innerMap;
//                if (rules.containsKey(right)) {
//                    innerMap = rules.get(right);
//                }
//                else {
//                    innerMap = new HashMap<String, Double>();
//                    rules.put(right, innerMap);
//                }
//                innerMap.put(left, innerEntry.getValue());
//            }
//        }
//    }

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
                            String[] separate = right.split("|");
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
                            }
                        }
                    }
                }
            }
        }
        return generateResult(dp);
    }

    private String generateResult(PCFGNode[][][] dp) {
        PCFGNode root = null;
        double maxProb = 0;
        int n = dp.length;
        for (int i = 0; i < dp[0][n - 1].length; i++) {
            if (dp[0][n - 1][i].prob > maxProb) {
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
        // in this case, may need to add deleted rules in the middle
        else if (children.size() == 1) {
//            PCFGNode leftNode = node;
//            // must be a terminal here or else there's error in the CNF creation step
//            PCFGNode terminalNode = children.get(0);
//            leftNode.children.clear();
//            while (deletedRule.containsKey(leftNode.thisTag)) {
//                String right = deletedRule.get(leftNode.thisTag);
//                PCFGNode rightNode = new PCFGNode(right, null, new ArrayList<PCFGNode>(), 1);
//                leftNode.children.add(rightNode);
//                leftNode = rightNode;
//            }
//            leftNode.children.add(terminalNode);
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
            builder.append(s + "|");
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

