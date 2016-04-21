package Score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenChen on 4/20/16.
 */
public class Evaluate {
    public enum StateType {
        TWO, FOUR, FIVE;
    }

    private String          filePath;
    private StateType       stateType;
    private List<String>    testStates;
    private List<String>    goldStates;

    private int correct;
    private int incorrect;
    private int testGroupCount;
    private int goldGroupCount;
    private int correctGroupCount;

    private Evaluate(String path, StateType type) {
        filePath = path;
        stateType = type;
        testStates = new ArrayList<String>();
        goldStates = new ArrayList<String>();

        correct             = 0;
        incorrect           = 0;
        testGroupCount      = 0;
        goldGroupCount      = 0;
        correctGroupCount   = 0;
    }

    private void Process() {
        int lineCount = 0;
        String curLine = "";
        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(new File(filePath)), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(read);

            while ((curLine = bufferedReader.readLine()) != null) {
                lineCount++;
                String[] temps = curLine.split("\\s+");
                if (temps.length == 3) {
                    testStates.add(temps[1]);
                    goldStates.add(temps[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (stateType == StateType.TWO) {
            ScoreTwo();
        } else if (stateType == StateType.FOUR) {
            ScoreFour();
        } else if (stateType == StateType.FIVE) {
            ScoreFive();
        } else {
            // ???
        }
    }

    private void ScoreTwo() {
        boolean flag = true;
        for (int i = 0; i < testStates.size(); i++) {
            if (!testStates.get(i).equals(goldStates.get(i))) {
                flag = false;
                incorrect++;
            } else {
                correct++;
            }

            // there are three cases that means the end of a group
            // 1. current test state is not "B" or "E"
            // 2. next test state is "B"
            // 3. last line
            boolean case1 = !testStates.get(i).equals("B") && !testStates.get(i).equals("E");
            boolean case2 = false;
            if (i < (testStates.size() - 1) && testStates.get(i+1).equals("B")) {
                case2 = true;
            }
            boolean case3 = (i == testStates.size() - 1);

            if (case1 || case2 || case3) {
                testGroupCount++;
                if (flag) {
                    correctGroupCount++;
                }
                flag = true;
            }

            case1 = !goldStates.get(i).equals("B") && !goldStates.get(i).equals("E");
            case2 = false;
            if (i < (goldStates.size() - 1) && goldStates.get(i+1).equals("B")) {
                case2 = true;
            }
            case3 = (i == goldStates.size() - 1);
            if (case1 || case2 || case3) {
                goldGroupCount++;
            }
        }
        PrintResult();
    }

    private void ScoreFour() {
        boolean flag = true;
        for (int i = 0; i < testStates.size(); i++) {
            if (!testStates.get(i).equals(goldStates.get(i))) {
                flag = false;
                incorrect++;
            } else {
                correct++;
            }

//            if (testStates.get(i).equals("E") || testStates.get(i).equals("S")) {
//                testGroupCount++;
//                if (flag) {
//                    correctGroupCount++;
//                }
//                flag = true;
//            }
//
//            if (goldStates.get(i).equals("E") || goldStates.get(i).equals("S")) {
//                goldGroupCount++;
//            }
        }
        // PrintResult();
    }

    private void ScoreFive() {

    }

    private void PrintResult() {
        double accuracy = 100.0 * correct / (double) (correct + incorrect);
        double precision = 100.0 * correctGroupCount / (double) testGroupCount;
        double recall = 100.0 * correctGroupCount / (double) goldGroupCount;
        double F_value = 2.0 * precision * recall / (precision + recall);
        System.out.println(correct + " out of " + correct+incorrect + " states");
        System.out.println("Accuracy:\t" + accuracy);
        System.out.println("Groups in gold:\t" + goldGroupCount);
        System.out.println("Groups in test:\t" + testGroupCount);
        System.out.println("Correct groups:\t" + correctGroupCount);
        System.out.println("Precision:\t" + precision);
        System.out.println("Recall:\t" + recall);
        System.out.println("F_value:\t" + F_value);
    }

    public static void main(String[] args) {
        String filePath = "";
        StateType type = null;
        for (int i = 0; i < args.length; i++) {
            if ("-f".equals(args[i])) {
                filePath = args[i + 1];
                i++;
            } else if ("-t".equals(args[i])) {
                if ("2".equals(args[i+1])) {
                    type = StateType.TWO;
                } else if ("4".equals(args[i+1])) {
                    type = StateType.FOUR;
                } else if ("5".equals(args[i+1])) {
                    type = StateType.FIVE;
                } else {
                    System.out.println("State type is wrong!");
                    System.exit(1);
                }
                i++;
            }
        }

        Evaluate eval = new Evaluate(filePath, type);
        eval.Process();
    }
}
