package TrainingParser;

import java.io.*;

/**
 * Created by ChenChen on 4/23/16.
 */
public class TrainingParser2 {
    public enum FileType {
        TRAINING, DEVELOP, TESTING;
    }

    public static void main(String[] args) {
        BufferedWriter writer_2 = null;
        BufferedWriter writer_4 = null;
        BufferedWriter writer_5 = null;

        String dataPath = null;
        String resultPath = null;
        FileType type = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                dataPath = args[i+1];
                i++;
            } else if (args[i].equals("-r")) {
                resultPath = args[i+1];
                i++;
            } else if (args[i].equals("-t")) {
                if (args[i+1].toLowerCase().equals("train")) {
                    type = FileType.TRAINING;
                } else if (args[i+1].toLowerCase().equals("develop")) {
                    type = FileType.DEVELOP;
                } else if (args[i+1].toLowerCase().equals("test")) {
                    type = FileType.TESTING;
                } else {
                    System.out.println("Type can only be: train, develop or test");
                    System.exit(1);
                }
                i++;
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataPath));
            if (type == FileType.TRAINING) {
                writer_2 = new BufferedWriter(new FileWriter(resultPath + File.separator + "training_2_noP.tag"));
                writer_4 = new BufferedWriter(new FileWriter(resultPath + File.separator + "training_4_noP.tag"));
                writer_5 = new BufferedWriter(new FileWriter(resultPath + File.separator + "training_5_noP.tag"));
            } else if (type == FileType.DEVELOP) {
                writer_2 = new BufferedWriter(new FileWriter(resultPath + File.separator + "develop_2_noP.tag"));
                writer_4 = new BufferedWriter(new FileWriter(resultPath + File.separator + "develop_4_noP.tag"));
                writer_5 = new BufferedWriter(new FileWriter(resultPath + File.separator + "develop_5_noP.tag"));
            } else {
                writer_2 = new BufferedWriter(new FileWriter(resultPath + File.separator + "testing_2_noP.tag"));
                writer_4 = new BufferedWriter(new FileWriter(resultPath + File.separator + "testing_4_noP.tag"));
                writer_5 = new BufferedWriter(new FileWriter(resultPath + File.separator + "testing_5_noP.tag"));
            }

            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (word.length() == 1) {
                        writer_2.write(word + "\t" + "B" + "\n");
                        writer_4.write(word + "\t" + "S" + "\n");
                        writer_5.write(word + "\t" + "S" + "\n");
                        continue;
                    }

                    for (int pos = 0; pos < word.length(); pos++) {
                        char current = word.charAt(pos);
                        if (pos == 0) {
                            writer_2.write(current + "\t" + "B" + "\n");
                            writer_4.write(current + "\t" + "B" + "\n");
                            writer_5.write(current + "\t" + "B" + "\n");
                        } else if (pos == 1 && pos != word.length() - 1) {
                            writer_2.write(current + "\t" + "E" + "\n");
                            writer_4.write(current + "\t" + "M" + "\n");
                            writer_5.write(current + "\t" + "B2" + "\n");
                        } else if (pos == word.length() - 1) {
                            writer_2.write(current + "\t" + "E" + "\n");
                            writer_4.write(current + "\t" + "E" + "\n");
                            writer_5.write(current + "\t" + "E" + "\n");
                        } else {
                            writer_2.write(current + "\t" + "E" + "\n");
                            writer_4.write(current + "\t" + "M" + "\n");
                            writer_5.write(current + "\t" + "M" + "\n");
                        }
                    }
                }
                writer_2.write("\n");
                writer_4.write("\n");
                writer_5.write("\n");
            }
            reader.close();
            writer_2.close();
            writer_4.close();
            writer_5.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
