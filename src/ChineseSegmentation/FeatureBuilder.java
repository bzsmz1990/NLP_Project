package ChineseSegmentation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;

/**
 * Created by ChenChen on 4/19/16.
 */
public class FeatureBuilder {
    public enum FileType {
        TRAINING, DEVELOP, TESTING;
    }

    private String              dataPath;
    private String              resultPath;
    private FileType            fileType;
    private String              dicPath;
    private int                 statusType;
    private DictionaryFeature   dict;

    private static final List<Character> chineseNumber = Arrays.asList('零', '一', '二', '三', '四', '五',
            '六', '七', '八', '九', '十');
    private static final List<Character> chineseDates = Arrays.asList('年', '月', '日');

    FeatureBuilder(String dp, String rp, FileType ft, String dicp, int st) {
        dataPath    = dp;
        resultPath  = rp;
        fileType    = ft;
        dicPath     = dicp;
        statusType  = st;

        if (fileType == FileType.TRAINING) {
            resultPath += File.separator + "trainingFeatures";
        } else if (fileType == FileType.DEVELOP){
            resultPath += File.separator + "developFeatures";
        }

        dict = DictionaryFeature.getInstance();
        dict.readDict(dicPath);
    }


    private void Process() {
        int sentence = 0;
        int lineCount = 0;
        String curline = "";
        List<String> words = new ArrayList<String>();
        List<String> states = new ArrayList<String>();

        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(new File(dataPath)),"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(read);

            File featurefile = new File(resultPath);
            if (!featurefile.exists()) {
                featurefile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(featurefile);
            OutputStreamWriter write = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(write);

            while((curline = bufferedReader.readLine()) != null){
                if (curline.equals("")) {
                    List<List<String>> features = GenerateFeature(words, states);
                    if (!words.isEmpty()) {
                        sentence++;
                    }
                    StoreFeaturesFile(features, bufferedWriter);
                    ClearList(words, states);
                    if (words.isEmpty()) {
                        System.out.println("Sentence: " + sentence);
                        System.out.println("Line: " + (lineCount+1) + "\n");
                    }
                } else {
                    String[] pair = curline.split("\\s+");
                    words.add(pair[0]);
                    if (fileType == FileType.TRAINING || fileType == FileType.DEVELOP) {
                        states.add(pair[1]);
                    }
                }
                lineCount++;
            }

            bufferedWriter.close();
            write.close();
            fos.close();
            bufferedReader.close();
            read.close();

        } catch (FileNotFoundException e) {
            System.out.println("Data file doesn't exit");
            System.exit(1);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Doesn't exit the encoding way");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Cannot create result file");
            System.exit(1);
        }
    }

    /*
     * key method
     */
    private List<List<String>> GenerateFeature(List<String> words, List<String> states) {
        List<List<String>> features = new ArrayList<List<String>>();

        String[] predict = dict.analysis(words, statusType);     // feature -- predict status
        if (predict.length == 0) {
            return features;
        }
        for (int i = 0; i < words.size(); i++) {
            List<String> wordFeature = new ArrayList<String>();
            //current word -- 1
            String curWord = words.get(i);
            wordFeature.add(curWord);

            String preWord = "NULL";
            String nextWord = "NULL";
            String prepreWord = "NULL2";
            String nextnextWord = "NULL2";

            //first previous word -- 2
            if (i > 0) {
                preWord = words.get(i - 1);
            }
//            wordFeature.add(preWord);
//
//            //first next word -- 3
            if (i < words.size() - 1) {
                nextWord = words.get(i + 1);
            }
//            wordFeature.add(nextWord);
//
//            //second previous word counterpart -- 4
            if (i > 1) {
                prepreWord = words.get(i - 2);
            }
//            wordFeature.add(prepreWord);

            // second next word
            if (i < words.size() - 2) {
                nextnextWord = words.get(i + 2);
            }
//
//            //combination of first previous word and current word -- 5
//            wordFeature.add(preWord + curWord);
//
//            //combination of current word and first next word counterpart -- 6
//            wordFeature.add(curWord + nextWord);
//
//            // combination of first previous word and first next word -- 7
//            wordFeature.add(preWord + nextWord);
//
//            // combination of second previous word and first previous word -- 8
//            wordFeature.add(prepreWord + preWord);
//
//            // combination of second previous word and current word -- 9
//            wordFeature.add(prepreWord + curWord);

            //current word is identical to first previous word -- 10
            if (curWord.equals(preWord)) {
                wordFeature.add("AA");
            } else {
                wordFeature.add("AB");
            }

            // whether it is punctuation    -- 11
//            if (IsPunctuation(curWord)) {
//                wordFeature.add("1");
//            } else {
//                wordFeature.add("0");
//            }

            // 5 chars window format
            List<String> window = new ArrayList<String>();
            window.add(prepreWord);
            window.add(preWord);
            window.add(curWord);
            window.add(nextWord);
            window.add(nextnextWord);
            String format = GetFormate(window);
            wordFeature.add(format);

            // predict status
            wordFeature.add(predict[i]);


            //status for current word
            if (fileType == FileType.TRAINING || fileType == FileType.DEVELOP) {
                wordFeature.add(states.get(i));
            }

            features.add(wordFeature);
        }
        return features;
    }

    /*
     * store features based on CRF++
     */
    private void StoreFeaturesFile(List<List<String>> features, BufferedWriter bufferedWriter) {
        int size = features.size();
        try {
            for (int i = 0; i < size; i++) {
                String templine = "";
                int features_size = features.get(i).size();
                for (int j = 0; j < features_size; j++) {
                    if (j == features_size-1) {
                        templine += features.get(i).get(j);
                    } else {
                        templine += features.get(i).get(j) + "\t";
                    }
                }
                templine += "\n";
                bufferedWriter.write(templine);
            }
            bufferedWriter.write("\n");
        } catch (IOException e) {
            System.out.println("Cannot write features into file!");
            System.out.println(e);
        }
    }

    private void ClearList(List<String> words, List<String> states) {
        words.clear();
        if (fileType == FileType.TRAINING || fileType == FileType.DEVELOP) {
            states.clear();
        }
    }

    private boolean IsPunctuation(String word) {
        for (int i = 0; i < word.length(); i++) {
            char current = word.charAt(i);
            if (IsHan(current) || Character.isLetterOrDigit(current)) {
                return false;
            }
        }
        return true;
    }

    private boolean IsHan(char current) {
        if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) {
            return true;
        }
        else {
            return false;
        }
    }

    public String GetFormate(List<String> input) {
        String outPut = "";
        for (String word: input) {
            if (word.equals("NULL2") || word.equals("NULL")) {
                outPut += "0";
            } else if (chineseNumber.contains(word.charAt(0))) {
                outPut += "1";
            } else if (chineseDates.contains(word.charAt(0))){
                outPut += "2";
            } else if ((word.charAt(0) >= 65 && word.charAt(0) <= 90) ||
                    (word.charAt(0) >= 97 && word.charAt(0) <= 122)) {
                outPut += "3";
            } else {
                outPut += "4";
            }
        }
        return outPut;
    }

    public static void main(String[] args) {
        String dataPath     = "";
        String resultPath   = "";
        String dicPath      = "";
        FileType fileType   = null;
        int statusType      = 2;

        // command line parameters analysis
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                dataPath = args[i + 1];
                i++;
            } else if ("-r".equals(args[i])) {
                resultPath = args[i + 1];
                i++;
            } else if ("-t".equals(args[i])) {
                if ("train".equals(args[i + 1])) {
                    fileType = FileType.TRAINING;
                } else if ("develop".equals(args[i+1])) {
                    fileType = FileType.DEVELOP;
                } else if ("test".equals(args[i+1])) {
                    fileType = fileType.TESTING;
                } else {
                    System.out.println("Type can only be: train, develop or test");
                    System.exit(1);
                }
                i++;
            } else if ("-dic".equals(args[i])) {
                dicPath = args[i+1];
                i++;
            } else if ("-st".equals(args[i])) {
                statusType = Integer.parseInt(args[i+1]);
                i++;
            }
        }

        FeatureBuilder fb = new FeatureBuilder(dataPath, resultPath, fileType, dicPath, statusType);
        fb.Process();
    }

}
