package ChineseParse;

import java.util.Map;
import java.util.Set;

/**
 * Created by ChenChen on 5/3/16.
 */
public class TestTrainCorpus {

    public static void main(String[] args) {
        String dataFolder = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                dataFolder = args[i+1];
                i++;
            }
        }
        TrainCorpus trainCorpus = new TrainCorpus(dataFolder);
        trainCorpus.Training();
        Map<String, CFGNode> cfgs = trainCorpus.getCFGs();
        Set<String> terminals = trainCorpus.getTerminals();
//        for (String tag : terminals) {
//            System.out.println("Tag:\t" + tag);
//        }
        System.out.println("Finish");
    }
}
