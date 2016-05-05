package ChineseParse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenChen on 5/3/16.
 */
public class CFGNode {
    // CFG grammer: A -> B C
    // A is grammer left part
    // B is grammer right part

    public String name;    // grammer left part
    public Map<Production, Integer> childern;  // grammer right part -> count

    public CFGNode(String n) {
        name = n;
        childern = new HashMap<Production, Integer>();
    }

}
