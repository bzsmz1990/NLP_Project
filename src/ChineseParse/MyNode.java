package ChineseParse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenChen on 5/4/16.
 */
public class MyNode {
    private String name;
    public List<String> children;

    public MyNode(String n) {
        name = n;
        children = new ArrayList<String>();
    }

    public String GetName() {
        return name;
    }
}
