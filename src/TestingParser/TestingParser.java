package TestingParser;

import java.io.*;
import java.util.*;

/**
 * Created by Wenzhao on 4/19/16.
 */
public class TestingParser {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[1] + "pku_testing.test"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    char current = line.charAt(i);
                    if (current == ' ') {
                        continue;
                    }
                    writer.write(current + "\n");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
