package ChineseParse;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Wenzhao on 5/5/16.
 */
public class Rule {
    public String left;
    public List<String> right;
    double prob;

    public Rule(String left, List<String> right, double prob) {
        this.left = left;
        this.right = new ArrayList<String>(right);
        this.prob = prob;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(left + "\t");
        for (String s: right) {
            builder.append(s + "\t");
        }
        builder.append(prob);
        return builder.toString();
    }
}
