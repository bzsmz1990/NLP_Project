package ChineseParse;

import java.io.*;
import java.util.*;

/**
 * Created by Wenzhao on 5/5/16.
 */
public class ChangeCNF {
    private static final String USAGE = "java ChangeCNF dataFolderPath emptyRuleFilePath emptyOriginalTagFilePath";
    private static Set<String> terminals;
    private static List<Rule> ruleList;

    private static void processRules() {
        int pos = 0;
        int number = 0;
        HashMap<String, String> newRules = new HashMap<String, String>();
        while (pos < ruleList.size()) {
            Rule rule = ruleList.get(pos);
            if (needNonTerminal(rule)) {
                List<String> right = rule.right;
                for (int i = 0; i <= 1; i++) {
                    if (terminals.contains(right.get(i))) {
                        String label;
                        if (newRules.containsKey(right.get(i))) {
                            label = newRules.get(right.get(i));
                        }
                        else {
                            label = "X" + number;
                            number++;
                            Rule newRule = new Rule(label, new ArrayList<String>(), 1);
                            newRule.right.add(right.get(i));
                            ruleList.add(newRule);
                            newRules.put(right.get(i), label);
                        }
                        right.set(i, label);
                    }
                }
                pos++;
            }
            else if (needBinalize(rule)) {
                List<String> right = rule.right;
                while (right.size() > 2) {
                    String tagOne = right.get(0);
                    String tagTwo = right.get(1);
                    String combo = tagOne + "|" + tagTwo;
                    String label;
                    if (newRules.containsKey(combo)) {
                        label = newRules.get(combo);
                    }
                    else {
                        label = "X" + number;
                        number++;
                        Rule newRule = new Rule(label, new ArrayList<String>(), 1);
                        newRule.right.add(tagOne);
                        newRule.right.add(tagTwo);
                        ruleList.add(newRule);
                        newRules.put(combo, label);
                    }
                    right.remove(0);
                    right.remove(0);
                    right.add(0, label);
                }
            }
            else {
                pos++;
            }
//            else if (needUnitProduction(rule)) {
//                List<String> right = rule.right;
//                List<Rule> startsWithRight = searchRules(right.get(0));
//                double firstProb = rule.prob;
//                for (Rule rightRule: startsWithRight) {
//                    double secondProb = rightRule.prob;
//                    Rule newRule = new Rule(rule.left, rightRule.right, firstProb * secondProb);
//                    ruleList.add(newRule);
//                }
//                deletedRule.add(rule);
//                ruleList.remove(pos);
//            }
//            else {
//                System.out.println("There is a bug, " +
//                        "because the rule doesn't apply to any situation!");
//            }
        }
    }

    private static boolean needNonTerminal(Rule rule) {
        List<String> right = rule.right;
        if (right.size() == 2 &&
                (terminals.contains(right.get(0)) || terminals.contains(right.get(1)))) {
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean needBinalize(Rule rule) {
        List<String> right = rule.right;
        return right.size() >= 3;
    }

//    private static boolean needUnitProduction(Rule rule) {
//        List<String> right = rule.right;
//        if (right.size() == 1 && !terminals.contains(right.get(0))) {
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
//
//    private static boolean isValid(Rule rule) {
//        List<String> right = rule.right;
//        if (right.size() == 1 && terminals.contains(right.get(0))) {
//            return true;
//        }
//        else if (right.size() == 2 && !terminals.contains(right.get(0)) &&
//                !terminals.contains(right.get(1))) {
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
//
//    private static List<Rule> searchRules(String left) {
//        List<Rule> results = new ArrayList<Rule>();
//        for (Rule rule: ruleList) {
//            if (rule.left.equals(left)) {
//                results.add(rule);
//            }
//        }
//        return results;
//    }

    private static void saveRules(String ruleFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(ruleFile));
            for (Rule rule: ruleList) {
                writer.write(rule + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("save rules not successful");
        }
    }

    private static List<Rule> getRules(Map<String, CFGNode> cfgs) {
        ruleList = new ArrayList<Rule>();
        for (Map.Entry<String, CFGNode> entry: cfgs.entrySet()) {
            String left = entry.getKey();
            CFGNode node = entry.getValue();
            int count = 0;
            Map<Production, Integer> children = node.childern;
            for (Map.Entry<Production, Integer> innerEntry: children.entrySet()) {
                count += innerEntry.getValue();
            }
            for (Map.Entry<Production, Integer> innerEntry: children.entrySet()) {
                int currentCount = innerEntry.getValue();
                Production p = innerEntry.getKey();
                String[] rightTags = new String[p.size()];
                for (int i = 0; i < p.size(); i++) {
                    rightTags[i] = p.get(i);
                }
                double prob = (double)currentCount / count;
                ruleList.add(new Rule(left, Arrays.asList(rightTags), prob));
            }
        }
        return ruleList;
    }

    private static void saveOriginalTag(Map<String, CFGNode> cfgs, String originalTagFile) {
        HashSet<String> set = new HashSet<String>();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(originalTagFile));
            for (Map.Entry<String, CFGNode> entry: cfgs.entrySet()) {
                String left = entry.getKey();
                set.add(left);
                Map<Production, Integer> children = entry.getValue().childern;
                for (Map.Entry<Production, Integer> innerEntry: children.entrySet()) {
                    Production p = innerEntry.getKey();
                    for (int i = 0; i < p.size(); i++) {
                        set.add(p.get(i));
                    }
                }
            }
            for (String s: set) {
                writer.write(s + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("save unique tag file not successful");
        }
    }

    public static void main(String[] args) {
        String dataFolder = args[0];
        String ruleFile = args[1];
        String originalTagFile = args[2];
        TrainCorpus trainCorpus = new TrainCorpus(dataFolder);
        trainCorpus.Training();
        Map<String, CFGNode> cfgs = trainCorpus.getCFGs();
        terminals = trainCorpus.getTerminals();
        saveOriginalTag(cfgs, originalTagFile);
        ruleList = getRules(cfgs);
        processRules();
        saveRules(ruleFile);
    }
}
