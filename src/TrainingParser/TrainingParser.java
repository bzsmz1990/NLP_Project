package TrainingParser;

import java.io.*;
import java.util.*;

/**
 * Created by Wenzhao on 4/19/16.
 */
public class TrainingParser {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer_2 = new BufferedWriter(new FileWriter(args[1] + File.separator + "pku_training_2.tag"));
            BufferedWriter writer_4 = new BufferedWriter(new FileWriter(args[1] + File.separator + "pku_training_4.tag"));
            BufferedWriter writer_5 = new BufferedWriter(new FileWriter(args[1] + File.separator + "pku_training_5.tag"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (isP(word)) {
                        for (int pos = 0; pos < word.length(); pos++) {
                            char current = word.charAt(pos);
                            writer_2.write(current + "\t" + current + "\n");
                            writer_4.write(current + "\t" + current + "\n");
                            writer_5.write(current + "\t" + current + "\n");
                        }
                    }
                    else if (word.length() == 1) {
                        writer_2.write(word + "\t" + "B" + "\n");
                        writer_4.write(word + "\t" + "S" + "\n");
                        writer_5.write(word + "\t" + "S" + "\n");
                    }
                    else {
                        for (int pos = 0; pos < word.length(); pos++) {
                            char current = word.charAt(pos);
                            if (pos == 0) {
                                writer_2.write(current + "\t" + "B" + "\n");
                                writer_4.write(current + "\t" + "B" + "\n");
                                writer_5.write(current + "\t" + "B" + "\n");
                            }
                            else if (pos == 1 && pos != word.length() - 1) {
                                writer_2.write(current + "\t" + "E" + "\n");
                                writer_4.write(current + "\t" + "M" + "\n");
                                writer_5.write(current + "\t" + "B2" + "\n");
                            }
                            else if (pos == word.length() - 1) {
                                writer_2.write(current + "\t" + "E" + "\n");
                                writer_4.write(current + "\t" + "E" + "\n");
                                writer_5.write(current + "\t" + "E" + "\n");
                            }
                            else {
                                writer_2.write(current + "\t" + "E" + "\n");
                                writer_4.write(current + "\t" + "M" + "\n");
                                writer_5.write(current + "\t" + "M" + "\n");
                            }
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

    private static boolean isP(String word) {
        for (int i = 0; i < word.length(); i++) {
            char current = word.charAt(i);
            if (isHan(current) || Character.isLetterOrDigit(current)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHan(char current) {
        if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) {
            return true;
        }
        else {
            return false;
        }
    }
}
