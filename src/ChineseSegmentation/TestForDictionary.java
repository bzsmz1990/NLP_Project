package ChineseSegmentation;

import java.io.*;
import java.util.*;

/**
 * Created by Wenzhao on 4/23/16.
 */


// when use as API:
/*
*        DictionaryFeature dict = DictionaryFeature.getInstance();
*        dict.readDict(mapPath);                                         // use CoreNatureDictionary.txt
*        String[] result = dict.analysis(list, type);          // list is an arrayList of single characters,
*                                                              // the method inside will assemble them into a sentence
*                                                              //type can be 2, 4 or 5 (the number of tags)
*
* */


public class TestForDictionary {
    public static void main(String[] args) {
        String mapPath = args[0];
        String dataPath = args[1];
        String resultPath = args[2];
        int type = Integer.parseInt(args[3]); // can be 2, 4 or 5 (the number of tags)

        DictionaryFeature dict = DictionaryFeature.getInstance();
        dict.readDict(mapPath); // use CoreNatureDictionary.txt

        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(new File(dataPath)),"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(read);

            File resultfile = new File(resultPath);
            if (!resultfile.exists()) {
                resultfile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(resultfile);
            OutputStreamWriter write = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(write);

            String line = null;
            List<String> words = new ArrayList<String>();
            List<String> gold = new ArrayList<String>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.matches("")) {
                    if (words.isEmpty()) {
                        continue;
                    }
                    String[] result = dict.analysis(words, type);
//                    for (int i = 0; i < words.size(); i++) {
//                        System.out.print(words.get(i) + "\t" + result[i] + "\n");
//                    }
//                    System.out.println();
                    for (int i = 0; i < words.size(); i++) {
                        bufferedWriter.write(words.get(i) + "\t" + gold.get(i) + "\t" + result[i] + "\n");
                    }
                    bufferedWriter.write("\n");
                    words.clear();
                    gold.clear();
                }
                else {
                    String[] token = line.split("\\s+");
                    words.add(token[0]);
                    gold.add(token[1]);
                }
            }

            bufferedReader.close();
            bufferedWriter.close();

        } catch (IOException e) {

        }

    }
}
