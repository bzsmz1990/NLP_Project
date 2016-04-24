package ChineseSegmentation;

/**
 * Created by Wenzhao on 4/23/16.
 */
public class TestForDictionary {
    public static void main(String[] args) {
        String mapPath = args[0];
        String sentence =
                "１２月３１日，中共中央总书记、国家主席江泽民发表１９９８年新年讲话" +
                "《迈向充满希望的新世纪》。（新华社记者兰红光摄）";

        DictionaryFeature dict = DictionaryFeature.getInstance();
        dict.readDict(mapPath);
        String[][] result = dict.analysis(sentence, 2); // can be 2, 4 or 5 (the number of tags)

        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i][0] + " " + result[i][1]);
        }
    }
}
