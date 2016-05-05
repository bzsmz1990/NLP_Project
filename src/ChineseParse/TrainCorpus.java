package ChineseParse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
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
    private Set<String> terminals;

    public TrainCorpus(String df) {
        dataFolder = df;
        cfgs = new HashMap<String, CFGNode>();
        terminals = new HashSet<String>();
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
        sentence = sentence.replace("\n", " ");
        sentence = sentence.replaceAll("\\s+", " ");
        Stack<MyNode> tagStack = new Stack<MyNode>();
        int bracket = 0;
        String token = "";
        for (int i = 0; i < sentence.length(); i++) {
            if (sentence.charAt(i) == '(') {
                bracket++;
                if (token.length() > 0) {
                    token = FilterSpace(token);
                    tagStack.push(new MyNode(token));
                    token = "";
                } else {
                    continue;
                }
            } else if (sentence.charAt(i) == ')') {
                bracket--;
                if (token.length() > 0) {
                    token = FilterSpace(token);
                    String[] temps = token.split("\\s+");
                    if (temps.length != 2) {
                        System.out.println("Token has problem: " + token);
                    }
                    tagStack.push(new MyNode(temps[0]));
                    token = "";
                }
                if (tagStack.isEmpty()) {
                    continue;
                }

                MyNode child = tagStack.pop();
                // because child node pop up, we know all its children
                // in BuildCFGs(), child is actually the parent
                BuildCFGs(child);

                if (!tagStack.isEmpty()) {
                    MyNode parent = tagStack.peek();
                    parent.children.add(child.GetName());
                } else {
                    // already reach root
                    // add dummy node
                    MyNode dummy = new MyNode("SENTENCE");
                    dummy.children.add(child.GetName());
                    BuildCFGs(dummy);
                    continue;
                }

            } else if (sentence.charAt(i) == ' ') {
                if (token.length() == 0) {
                    continue;
                } else {
                    token += sentence.charAt(i);
                }
            } else {
                token += sentence.charAt(i);
            }
        }
    }

    private String FilterSpace(String token) {
        int begin = 0;
        int end = token.length() - 1;
        while (begin < token.length()) {
            if (token.charAt(begin) == ' ') {
                begin++;
            } else {
                break;
            }
        }
        while (end >= begin) {
            if (token.charAt(end) == ' ') {
                end--;
            } else {
                break;
            }
        }
        if (begin == token.length()-1) {
            System.out.println("Token has problem: " + token);  // for debug
            return "";
        }
        return token.substring(begin, end+1);
    }

    private void BuildCFGs(MyNode node) {
        if (node.children.size() == 0) {
            // if has no child, it means this node is a leaf
            // also add it into terminals
            terminals.add(node.GetName());
            return;
        }

        String directive = "";
//        for (int i = 0; i < node.children.size(); i++) {
//            if (i < node.children.size() - 1) {
//                directive += node.children.get(i) + "\t";
//            } else {
//                directive += node.children.get(i);
//            }
//        }

        Production production = new Production(node.children.size());
        for (int i = 0; i < node.children.size(); i++) {
            production.set(i, node.children.get(i));
        }

        // store into CFG rules map
        String name = node.GetName();
        CFGNode cfgNode = null;
        if (cfgs.containsKey(name)) {
            cfgNode = cfgs.get(name);
            if (cfgNode.childern.containsKey(production)) {
                int count = cfgNode.childern.get(production) + 1;
                cfgNode.childern.put(production, count);
            } else {
                cfgNode.childern.put(production, 1);
            }
        } else {
            cfgNode = new CFGNode(name);
            cfgNode.childern.put(production, 1);
        }
        cfgs.put(name, cfgNode);
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

    public Map<String, CFGNode> getCFGs() {
        return cfgs;
    }

    public Set<String> getTerminals() {
        return terminals;
    }

}
