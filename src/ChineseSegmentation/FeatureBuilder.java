package ChineseSegmentation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

/**
 * Created by ChenChen on 4/19/16.
 */
public class FeatureBuilder {
    public enum FileType {
        TRAINING, TESTING;
    }

    private String      dataPath;
    private String      resultPath;
    private FileType    fileType;

    FeatureBuilder(String dp, String rp, FileType ft) {
        dataPath = dp;
        resultPath = rp;
        fileType = ft;

        if (fileType == FileType.TRAINING) {
            resultPath += File.separator + "trainingFeatures";
        } else if (fileType == FileType.TESTING){
            resultPath += File.separator + "testingFeatures";
        }
    }


    private void Process() {
        int sentence = 0;
        int lineCount = 1;
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
                } else {
                    String[] pair = curline.split("\\s+");
                    words.add(pair[0]);
                    if (fileType == FileType.TRAINING) {
                        states.add(pair[1]);
                    }
                }
                lineCount++;
            }

//            if (!words.isEmpty()) {
//                List<List<String>> features = GenerateFeature(words, states);
//                if (!words.isEmpty()) {
//                    sentence++;
//                }
//                StoreFeaturesFile(features, bufferedWriter);
//                ClearList(words, states);
//            }

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
        for (int i = 0; i < words.size(); i++) {
            List<String> wordFeature = new ArrayList<String>();
            //current word -- 1
            String curWord = words.get(i);
            wordFeature.add(curWord);
            if (i > 0) {
                //first previous word -- 2
            	String prev1Word = words.get(i-1);
            	wordFeature.add(prev1Word);
            	//if current word is identical to first previous word -- 3
            	if (curWord.equals(prev1Word)) {
            		wordFeature.add("SameAsPrevious");
            	}
            	//combination of first previous word and current word -- 4
            	String tmp = prev1Word + curWord;
            	wordFeature.add(tmp);
            	if (i > 1) {
            		//second previous word -- 5
            		String prev2Word = words.get(i-2);
            		wordFeature.add(prev2Word);
            		//combination of second previous and first previous word -- 6
            		String tmp2 = prev2Word + prev1Word;
            		wordFeature.add(tmp2);
            	} else {
            		//second previous word counterpart -- 5
            		wordFeature.add("@@");
            		//combination of second previous and first previous word counterpart -- 6
            		wordFeature.add("@@");
            	}
            } else {
            	//first previous word counterpart -- 2
            	wordFeature.add("@@");
            	//SameAsPrevious counter part -- 3
            	wordFeature.add("@@");
            	//combination of first previous word and current word counterpart -- 4
            	wordFeature.add("@@");
            	//second previous word counterpart -- 5
        		wordFeature.add("@@");
        		//combination of second previous and first previous word counterpart -- 6
        		wordFeature.add("@@");
            }
            
            if (i < words.size() - 1) {
                //first next word -- 7
            	String next1Word = words.get(i+1);
            	wordFeature.add(next1Word);
            	//combination of current word and first next word -- 8
            	String tmp = curWord + next1Word;
            	wordFeature.add(tmp);
            	if (i < words.size() - 2) {
            		//second next word -- 9
            		String next2Word = words.get(i+2);
            		wordFeature.add(next2Word);
            	}
            } else {
            	//first next word counterpart -- 7
            	wordFeature.add("@@");
            	//combination of current word and first next word counterpart -- 8
            	wordFeature.add("@@");
            	//second next word counterpart -- 9
            	wordFeature.add("@@");
            }
            
            if (i > 0 && i < words.size() - 1) {
            	//jump, combination of first previous word and first next word -- 10
            	String tmp = words.get(i-1) + curWord + words.get(i+1);
            	wordFeature.add(tmp);
            } else {
            	//jump, combination of first previous word and first next word counterpart -- 10
            	wordFeature.add("@@");
            }
            if (fileType == FileType.TRAINING) {
            	//tag for current word if training file -- 11
                wordFeature.add(states.get(i));
            } else {
            	//tag for current word if training file counterpart -- 11
            	wordFeature.add("@@");
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
        if (fileType == FileType.TRAINING) {
            states.clear();
        }
    }

    public static void main(String[] args) {
        String      dataPath = "";
        String      resultPath = "";
        FileType    fileType = null;

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
                } else {
                    fileType = FileType.TESTING;
                }
            }
        }

        FeatureBuilder fb = new FeatureBuilder(dataPath, resultPath, fileType);
        fb.Process();
    }

}
