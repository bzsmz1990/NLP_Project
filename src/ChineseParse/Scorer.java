package ChineseParse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * Can choose between running separately, or running together with PCFGParser
 * Adapted from TrainCorpus.java by Chen Chen
 * Created by Wenzhao on 5/4/16.
 */
public class Scorer {
    private final String USAGE = "java ParseScorer goldFolderPath ownFilePath";
    private List<String> goldSentences = new ArrayList<String>();
    private int currentPosFromParser = 0;
    private int correctFromParser = 0;

    public enum FileType {
        HTML, RAW;
    }

    public Scorer(String goldFolder) {
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

    public void runFromParser(String sentence) {
        if (currentPosFromParser > goldSentences.size() - 1) {
            System.out.println("The number of lines don't match");
            return;
        }
        System.out.println("gold parse:");
        System.out.println(goldSentences.get(currentPosFromParser));
        System.out.println("own parse");
        System.out.println(sentence);
        if (sentence.equals(goldSentences.get(currentPosFromParser))) {
            correctFromParser++;
            System.out.println("correct");
        }
        else {
            System.out.println("incorrect");
        }
        currentPosFromParser++;
        if (currentPosFromParser == goldSentences.size()) {
            System.out.println(correctFromParser + " out of " + goldSentences.size() +
                    " sentences are correct.");
            System.out.println("The parsing accuracy is " +
                    (double)correctFromParser / goldSentences.size());
        }
    }

    private void run(String ownFile) {
        int total = 0;
        int correct = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ownFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (total > goldSentences.size() - 1) {
                    System.out.println("The number of lines don't match");
                    return;
                }
                System.out.println("gold parse:");
                System.out.println(goldSentences.get(total));
                System.out.println("own parse:");
                System.out.println(line);
                if (line.equals(goldSentences.get(total))) {
                    correct++;
                    System.out.println("correct");
                }
                else {
                    System.out.println("incorrect");
                }
                total++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(correct + " out of " + total +" sentences are correct.");
        System.out.println("The parsing accuracy is " + (double)correct / total);
    }

    private void ProcessOneFile(File file) {
        String content = GetFileContent(file);
        FileType fileType = null;
        if (content.contains("<DOC>")) {
            fileType = FileType.HTML;
            ProcessHTMLFile(content);
        } else if (content.charAt(0) == '('){
            fileType = FileType.RAW;
            ProcessRAWFile(content);
        } else {
            System.out.println("File type is wrong: " + file.getName());
            return;
        }
    }

    private String GetFileContent(File file) {
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

    private void ProcessHTMLFile(String content) {
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("s");
        for (Element element : elements) {
            if (element.text().length() != 0) {
                String sentence = element.text();
                ProcessSentence(sentence);
            }
        }
    }

    private void ProcessRAWFile(String content) {
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

    private void ProcessSentence(String sentence) {
        // make up the sentence first:
        // replace "\n"
        // replace all those multiple spaces with a single space
        sentence = sentence.replace("\n", " ");
        sentence = sentence.replaceAll("\\s+", " ");
        sentence = sentence.trim();
        goldSentences.add(sentence);
    }

    private String GetExtension(String filename) {
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

    private class Comp implements Comparator<File> {
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
        String goldFolder = args[0];
        String ownFile = args[1];
        Scorer scorer = new Scorer(goldFolder);
        scorer.run(ownFile);
    }
}


