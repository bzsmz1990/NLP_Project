package ChineseParse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

/**
 * In fid files and the rule files, there are lots of '-NONE-' tags,
 * but in develop.txt files, there are no such tags. So have to use this program
 * to generate test files from fid files in order to match, instead of using develop.txt
 *
 * Adapted from TrainCorpus.java by Chen Chen
 * Created by Wenzhao on 5/5/16.
 */
public class generateTestFile {
    private static final String USAGE = "java ParseScorer goldFolderPath generatedFilePath";
    private static List<String> testSentences = new ArrayList<String>();

    public static void generateList(String goldFolder) {
        File folder = new File(goldFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Your data path should be a folder!");
            System.exit(1);
        }
        File[] dataFiles = folder.listFiles();
        Arrays.sort(dataFiles, new Comp());
        for (File file : dataFiles) {
            String fileName = file.getName();
            String ext = GetExtension(fileName);
            // only process files with extension "fid"
            if (ext.equals("fid")) {
                ProcessOneFile(file);
            }
            else {
                System.out.println("goldFile must be in fid extension");
            }
        }
    }

    private static void saveToFile(String filePath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            for (int i = 0; i < testSentences.size(); i++) {
                System.out.println("processing " + i + " sentence");
                String s = testSentences.get(i);
                int index = s.indexOf("(", 0);
                while (index >= 0 && index < s.length()) {
                    int nextLeft = s.indexOf("(", index + 1);
                    int nextRight = s.indexOf(")", index + 1);
                    if (nextRight < nextLeft || nextLeft == -1) {
                        String part = s.substring(index + 1, nextRight);
                        String[] data = part.split("\\s+");
                        writer.write(data[1] + "\t" + data[0] + "\n");
                    }
                    index = nextLeft;
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("save to file not successful");
        }
    }

    private static void ProcessOneFile(File file) {
        String content = GetFileContent(file);
        Scorer.FileType fileType = null;
        if (content.contains("<DOC>")) {
            fileType = Scorer.FileType.HTML;
            ProcessHTMLFile(content);
        } else if (content.charAt(0) == '('){
            fileType = Scorer.FileType.RAW;
            ProcessRAWFile(content);
        } else {
            System.out.println("File type is wrong: " + file.getName());
            return;
        }
    }

    private static String GetFileContent(File file) {
        String content = "";
        String curLine = "";
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            while ((curLine = bufferedReader.readLine()) != null) {
                content += curLine;
            }
            bufferedReader.close();
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("There is no file:" + file.getName());
        } catch (UnsupportedEncodingException e) {
            System.out.println("Cannot encode the file using UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    private static void ProcessHTMLFile(String content) {
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("s");
        for (Element element : elements) {
            if (element.text().length() != 0) {
                String sentence = element.text();
                ProcessSentence(sentence);
            }
        }
    }

    private static void ProcessRAWFile(String content) {
        String sentence = "";
        int bracket = 0;
        for (int i = 0; i < content.length(); i++) {
            sentence += content.charAt(i);
            if (content.charAt(i) == '(') {
                bracket++;
            } else if (content.charAt(i) == ')') {
                bracket--;
                if (bracket == 0) {
                    ProcessSentence(sentence);
                    // reset sentence
                    sentence = "";
                }
            }
        }
    }

    private static void ProcessSentence(String sentence) {
        // make up the sentence first:
        // replace "\n"
        // replace all those multiple spaces with a single space
        sentence = sentence.replace("\n", " ");
        sentence = sentence.replaceAll("\\s+", " ");
        sentence = sentence.trim();
        testSentences.add(sentence);
    }

    private static String GetExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);

        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    private static class Comp implements Comparator<File> {
        @Override
        public int compare(File one, File two) {
            String nameOne = one.getName();
            String nameTwo = two.getName();
            // hard code
            int numberOne = Integer.parseInt(nameOne.substring(5, nameOne.indexOf(".", 0)));
            int numberTwo = Integer.parseInt(nameTwo.substring(5, nameTwo.indexOf(".", 0)));
            if (numberOne < numberTwo) {
                return -1;
            }
            else if (numberOne > numberTwo) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    public static void main(String[] args) {
        String goldFolderPath = args[0];
        String generatedFilePath = args[1];
        generateList(goldFolderPath);
        saveToFile(generatedFilePath);
    }
}
