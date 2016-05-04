package ChineseParse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ChenChen on 5/3/16.
 */
public class TrainCorpus {
    private enum FileType {
        HTML, RAW;
    }

    private String dataFolder;
    private Map<String, CFGNode> cfgs;

    public TrainCorpus(String df) {
        dataFolder = df;
        cfgs = new HashMap<String, CFGNode>();
    }

    public void Training() {
        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Your data path should be a folder!");
            System.exit(1);
        }

        File[] dataFiles = folder.listFiles();
        for (File file : dataFiles) {
            String fileName = file.getName();
            String ext = GetExtension(fileName);
            // only process files with extension "fid"
            if (ext.equals("fid")) {
                ProcessOneFile(file);
                System.out.println("Finish training file: " + fileName);
            }
        }
    }

    private void ProcessOneFile(File file) {
        String content = GetFileContent(file);
        FileType fileType = null;

        if (content.contains("<DOC>")) {
            fileType = FileType.HTML;
            ProcessHTMLFile(content);
        } else if (content.charAt(0) == '('){
            fileType = fileType.RAW;
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
        sentence.replace("\n", " ");
        sentence.replaceAll("\\s+", " ");


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

}
