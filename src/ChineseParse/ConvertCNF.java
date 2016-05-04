package ChineseParse;

/**
 * @author mirrorlol
 *
 */

import java.util.HashMap;
import java.util.Map;

public class ConvertCNF {
	private Map<NonTerminal, Map<Production, Double>> fromGrammar;
	private Map<NonTerminal, Map<Production, Double>> toGrammar;

	public ConvertCNF(Map<NonTerminal, Map<Production, Double>> grammar) {
		fromGrammar = grammar;
	}

	public void iterateConversion() {
		Map<NonTerminal, Map<Production, Double>> tmpGrammar = convertToCNF(fromGrammar);
		while (!convertToCNF(tmpGrammar).equals(tmpGrammar)) {
			tmpGrammar = convertToCNF(tmpGrammar);
		}
		toGrammar = tmpGrammar;
	}

	private Map<NonTerminal, Map<Production, Double>> convertToCNF(Map<NonTerminal, Map<Production, Double>> grammar) {
		Map<NonTerminal, Map<Production, Double>> result = new HashMap<NonTerminal, Map<Production, Double>>();
		for (NonTerminal nt : grammar.keySet()) {	
			Map<Production, Double> rules = grammar.get(nt);
			if (!result.containsKey(nt)) {
				result.put(nt, new HashMap<Production, Double>());
			}
			for (Production p : rules.keySet()) {
				if (p.size() == 2) {
					if (!Terminal.containTerminal(p.get(0)) && 
							!Terminal.containTerminal(p.get(1))) {
						result.get(nt).put(p, rules.get(p));
					} else if (Terminal.containTerminal(p.get(0)) && 
							!Terminal.containTerminal(p.get(1))) {
						//first elem is Terminal, second is not
						Production p1 = new Production(2);
						String newProductionName = p.get(0).toUpperCase();
						p1.set(0, newProductionName);
						p1.set(1, p.get(1));
						result.get(nt).put(p1, rules.get(p));					
						Map<Production, Double> toTerminal = new HashMap<Production, Double>();
						Production p2 = new Production(1);
						p2.set(0, p.get(0));
						toTerminal.put(p2, 1.0);
						NonTerminal newNT = new NonTerminal(newProductionName);
						result.put(newNT, toTerminal);
					} else if (!Terminal.containTerminal(p.get(0)) && 
							Terminal.containTerminal(p.get(1))) {
						//first elem is not, second is Terminal
						Production p1 = new Production(2);
						String newProductionName = p.get(1).toUpperCase();
						p1.set(0, p.get(0));
						p1.set(1, newProductionName);
						result.get(nt).put(p1, rules.get(p));					
						Map<Production, Double> toTerminal = new HashMap<Production, Double>();
						Production p2 = new Production(1);
						p2.set(0, p.get(1));
						toTerminal.put(p2, 1.0);
						NonTerminal newNT = new NonTerminal(newProductionName);
						result.put(newNT, toTerminal);
					}
				} else if (p.size() == 1) {
					if (Terminal.containTerminal(p.get(0))) {
						result.get(nt).put(p, rules.get(p));
					} else {
						//unit production
						NonTerminal snt = null;
						for (NonTerminal tmp : grammar.keySet()) {
							if (tmp.toString().equals(p.get(0))) {
								snt = tmp;
								break;
							}
						}
						Map<Production, Double> sntRules = grammar.get(snt);
						double base = rules.get(p);
						for (Production pp : sntRules.keySet()) {
							result.get(nt).put(pp, base * sntRules.get(pp));
						}
					}
				} else {
					//need to binarize
					String newNTname = "X";
					int i = 1;
					while (grammar.keySet().contains(newNTname + Integer.toString(i))) {
						i++;
					}
					newNTname += Integer.toString(i);
					NonTerminal newNT = new NonTerminal(newNTname);
					Production p1 = new Production(2);
					p1.set(0, p.get(0));
					p1.set(1, p.get(1));
					Map<Production, Double> p1Rules = new HashMap<Production, Double>();
					p1Rules.put(p1, 1.0);
					result.put(newNT, p1Rules);
					Production p2 = new Production(p.size() - 1);
					p2.set(0, newNTname);
					for (int j = 1; j < p2.size(); j++) {
						//shaffle
						p2.set(j, p.get(j+1));
					}
					result.get(nt).put(p2, rules.get(p));
				}
			}

		}
		return result;
	}

	public Map<NonTerminal, Map<Production, Double>> calcProb(Map<NonTerminal, Map<Production, Integer>> grammar) {
		Map<NonTerminal, Map<Production, Double>> result = new HashMap<NonTerminal, Map<Production, Double>>();
		for (NonTerminal nt : grammar.keySet()) {
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
}

