package ChineseParse;

import java.util.List;
import java.util.ArrayList;
/**
 * Created by Wenzhao on 5/4/16.
 */
public class PCFGNode {
    public String thisTag;
    public String thisWord;
    public List<PCFGNode> children;
    public double prob;

    public PCFGNode (String tag, String word, List<PCFGNode> list, double p) {
        thisTag = tag;
        thisWord = word;
        children = new ArrayList<PCFGNode>(list);
        prob = p;
    }
}
