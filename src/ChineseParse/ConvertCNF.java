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

public class ConvertCNF {
    private int sudoNTcount = 1;
    private Map<String, Map<Production, Double>> fromGrammar;
    private Map<String, Map<Production, Double>> toGrammar;

    public ConvertCNF(Map<String, Map<Production, Double>> grammar) {
        fromGrammar = grammar;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String nt : toGrammar.keySet()) {
            for (Production p : toGrammar.get(nt).keySet()) {
                builder.append(nt);
                builder.append("\t");
                builder.append(p);
                builder.append(toGrammar.get(nt).get(p));
                builder.append("\n");
            }
            builder.append("\n");
        }
        return builder.toString();
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
                        if (!Terminal.containTerminal(p.get(i))) {
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
                if (p.size() == 1 && !Terminal.containTerminal(p.get(0))) {
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
                    while (toGrammar.keySet().contains("X" + Integer.toString(sudoNTcount))) {
                        sudoNTcount++;
                    }
                    String head = "X" + Integer.toString(sudoNTcount);
                    Production headProduction = new Production(2);
                    headProduction.set(0, p.get(0));
                    headProduction.set(1, p.get(1));
                    result.put(head, new HashMap<Production, Double>());
                    result.get(head).put(headProduction, 1.0);
                    sudoNTcount++;
                    for (int i = 2; i < p.size() - 1 ; i++ ) {
                        String xi = "X" + Integer.toString(sudoNTcount);
                        Production pi = new Production(2);
                        pi.set(0, "X" + Integer.toString(sudoNTcount - 1));
                        pi.set(1, p.get(i));
                        result.put(xi, new HashMap<Production, Double>());
                        result.get(xi).put(pi, 1.0);
                        sudoNTcount++;
                    }
                    Production tailProduction = new Production(2);
                    tailProduction.set(0, "X" + Integer.toString(sudoNTcount - 1));
                    tailProduction.set(1, p.get(p.size() - 1));
                    result.get(nt).put(tailProduction, toGrammar.get(nt).get(p));
                } else {
                    result.get(nt).put(p, toGrammar.get(nt).get(p));
                }
            }
        }
        toGrammar = result;
    }

    public Map<String, Map<Production, Double>> calcProb(Map<String, Map<Production, Integer>> grammar) {
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
        bw.write(this.toString());
        bw.flush();
        bw.close();
        fw.close();
    }

//	public static void main(String[] args) throws IOException {
//		Map<String, Map<Production, Double>> original = new HashMap<String, Map<Production, Double>>();
//		String s = "S";
//		String a = "A";
//		String b = "B";
//		String c = "C";
//
//		original.put(s, new HashMap<Production, Double>());
//
//		Production p8 = new Production(1);
//		p8.set(0, a.toString());
//		original.get(s).put(p8, 1.0);
//
//		original.put(a, new HashMap<Production, Double>());
//
//		Production p9 = new Production(4);
//		p9.set(0, "to");
//		p9.set(1, "be");
//		p9.set(2, "continued");
//		p9.set(3, c);
//		original.get(a).put(p9, 0.5);
//
//		Production p10 = new Production(1);
//		p10.set(0, b.toString());
//		original.get(a).put(p10, 0.5);
//
//		original.put(b, new HashMap<Production, Double>());
//
//		Production p11 = new Production(1);
//		p11.set(0, "to");
//		original.get(b).put(p11, 0.5);
//
//		Production p12 = new Production(1);
//		p12.set(0, "be");
//		original.get(b).put(p12, 0.5);
//
//		ConvertCNF convert = new ConvertCNF(original);
//		convert.convertToCNF();
//		System.out.println(convert);
//		File f = new File("mirrortest");
//		convert.saveResultToFile(f);
//	}
}