package ChineseParse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author mirrorlol
 *
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConvertCNF {
    private int sudoNTcount = 1;
    private Set<String> terminals;
    private Map<String, Map<Production, Double>> fromGrammar;
    private Map<String, Map<Production, Double>> toGrammar;

    public ConvertCNF(Map<String, Map<Production, Integer>> grammar) {
        fromGrammar = calcProb(grammar);
    }
    
    public void setTerminals(Set<String> terminals) {
    	this.terminals = terminals;
    }

    public Map<String, Map<Production, Double>> convertToCNF() {
        remedyMix();
        Map<String, Map<Production, Double>> tmp = toGrammar;
        remedyUnitProduction();
        while (!tmp.equals(toGrammar)) {
            tmp = toGrammar;
            remedyUnitProduction();
        }
        remedyLongProduction();
        return toGrammar;
    }

    private void remedyMix() {
        toGrammar = fromGrammar;
        Map<String, Map<Production, Double>> result = new HashMap<String, Map<Production, Double>>();
        for (String nt : toGrammar.keySet()) {
            result.put(nt, new HashMap<Production, Double>());
            for (Production p : toGrammar.get(nt).keySet()) {
                Production newProduction = new Production(p.size());
                if (p.size() > 1) {
                    for (int i = 0; i < newProduction.size(); i++) {
                        if (!terminals.contains(p.get(i))) {
                            //This elem is a NonTerminal
                            newProduction.set(i, p.get(i));
                        } else {
                            //This elem is a Terminal
                            String newNonTerminal = p.get(i).toUpperCase();
                            newProduction.set(i, newNonTerminal);
                            Production tmp = new Production(1);
                            tmp.set(0, p.get(i));
                            if (!result.containsKey(newNonTerminal)) {
                                HashMap<Production, Double> tmpRules = new HashMap<Production, Double>();
                                tmpRules.put(tmp, 1.0);
                                result.put(newNonTerminal, tmpRules);
                            }
                        }
                    }
                } else {
                    //single elem preserved
                    newProduction.set(0, p.get(0));
                }
                result.get(nt).put(newProduction, toGrammar.get(nt).get(p));
            }
        }
        toGrammar = result;
    }

    private void remedyUnitProduction() {
        Map<String, Map<Production, Double>> result = new HashMap<String, Map<Production, Double>>();
        for (String nt : toGrammar.keySet()) {
            result.put(nt, new HashMap<Production, Double>());
            for (Production p : toGrammar.get(nt).keySet()) {
                if (p.size() == 1 && !terminals.contains(p.get(0))) {
                    //unit production
                    double base = toGrammar.get(nt).get(p);
                    String tmp = p.get(0);
                    Map<Production, Double> transfer = toGrammar.get(tmp);
                    for (Production pp : transfer.keySet()) {
                        result.get(nt).put(pp, base * transfer.get(pp));
                    }
                    //result.get(nt).put(p, 0.0);
                } else {
                    result.get(nt).put(p, toGrammar.get(nt).get(p));
                }
            }
        }
        toGrammar = result;
    }

    private void remedyLongProduction() {
        Map<String, Map<Production, Double>> result = new HashMap<String, Map<Production, Double>>();
        for (String nt : toGrammar.keySet()) {
            result.put(nt, new HashMap<Production, Double>());
            for (Production p : toGrammar.get(nt).keySet()) {
                if (p.size() > 2) {
                    while (toGrammar.keySet().contains("X" + sudoNTcount)) {
                        sudoNTcount++;
                    }
                    String head = "X" + sudoNTcount;
                    Production headProduction = new Production(2);
                    headProduction.set(0, p.get(0));
                    headProduction.set(1, p.get(1));
                    result.put(head, new HashMap<Production, Double>());
                    result.get(head).put(headProduction, 1.0);
                    sudoNTcount++;
                    for (int i = 2; i < p.size() - 1 ; i++ ) {
                        String xi = "X" + sudoNTcount;
                        Production pi = new Production(2);
                        pi.set(0, "X" + (sudoNTcount - 1));
                        pi.set(1, p.get(i));
                        result.put(xi, new HashMap<Production, Double>());
                        result.get(xi).put(pi, 1.0);
                        sudoNTcount++;
                    }
                    Production tailProduction = new Production(2);
                    tailProduction.set(0, "X" + (sudoNTcount - 1));
                    tailProduction.set(1, p.get(p.size() - 1));
                    result.get(nt).put(tailProduction, toGrammar.get(nt).get(p));
                } else {
                    result.get(nt).put(p, toGrammar.get(nt).get(p));
                }
            }
        }
        toGrammar = result;
    }

    private Map<String, Map<Production, Double>> calcProb(Map<String, Map<Production, Integer>> grammar) {
        Map<String, Map<Production, Double>> result = new HashMap<String, Map<Production, Double>>();
        for (String nt : grammar.keySet()) {
            Map<Production, Integer> rulesWithCount = grammar.get(nt);
            int totalCount = 0;
            for (Integer i : rulesWithCount.values()) {
                totalCount += i.intValue();
            }
            result.put(nt, new HashMap<Production, Double>());
            for (Production p : rulesWithCount.keySet()) {
                result.get(nt).put(p, (double)rulesWithCount.get(p)/totalCount);
            }
        }
        return result;
    }

    public void saveResultToFile(File f) throws IOException {
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        StringBuilder builder = null;
        for (String nt : toGrammar.keySet()) {
            for (Production p : toGrammar.get(nt).keySet()) {
            	builder = new StringBuilder();
                builder.append(nt);
                builder.append("\t");
                builder.append(p.toString());
                builder.append(toGrammar.get(nt).get(p));
                builder.append("\n");
                bw.write(builder.toString());
            }
            //bw.newLine();
        }
        bw.flush();
        bw.close();
        fw.close();
    }
    
    public static void main(String[] args) throws IOException {
    	String dataFolder = args[0];
        String ruleFile = args[1];
        //String PosTagFile = args[2];
        TrainCorpus trainCorpus = new TrainCorpus(dataFolder);
        trainCorpus.Training();
        Map<String, CFGNode> cfgs = trainCorpus.getCFGs();
        Map<String, Map<Production, Integer>> original = new HashMap<String, Map<Production, Integer>>();
        for (String nt : cfgs.keySet()) {
        	CFGNode tmp = cfgs.get(nt);
        	original.put(tmp.name, tmp.childern);
        }
        ConvertCNF convert = new ConvertCNF(original);
        convert.setTerminals(trainCorpus.getTerminals());
        convert.convertToCNF();
        File f = new File(ruleFile);
        convert.saveResultToFile(f);
    }
}