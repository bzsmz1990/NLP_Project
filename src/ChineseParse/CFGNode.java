package ChineseParse;

import java.util.List;
import java.util.Map;

/**
 * Created by ChenChen on 5/3/16.
 */
public class CFGNode {
    // CFG grammer: A -> B C
    // A is grammer left part
    // B is grammer right part

    private String name;    // grammer left part
    private Map<String, Integer> childern;  // grammer right part -> count
}
