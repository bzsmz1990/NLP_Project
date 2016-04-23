package TrainingParser;

import java.io.*;

/**
 * Created by ChenChen on 4/23/16.
 */
public class TrainingParser2 {
    public static void main(String[] args) {
        BufferedWriter writer_2 = null;
        BufferedWriter writer_4 = null;
        BufferedWriter writer_5 = null;

        String dataPath = null;
        String resultPath = null;
        boolean train = true;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                dataPath = args[i+1];
                i++;
            } else if (args[i].equals("-r")) {
                resultPath = args[i+1];
                i++;
            } else if (args[i].equals("-t")) {
                if (args[i+1].equals("train")) {
                    train = true;
                } else {
                    train = false;
                }
                i++;
            }
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataPath));
            if (train) {
                writer_2 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_training_2_noP.tag"));
                writer_4 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_training_4_noP.tag"));
                writer_5 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_training_5_noP.tag"));
            } else {
                writer_2 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_testing_2_noP.tag"));
                writer_4 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_testing_4_noP.tag"));
                writer_5 = new BufferedWriter(new FileWriter(resultPath + File.separator + "pku_testing_5_noP.tag"));
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
