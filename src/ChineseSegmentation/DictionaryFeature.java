package ChineseSegmentation;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Wenzhao on 4/23/16.
 */
public class DictionaryFeature {
    private HashSet<String> dict = new HashSet<String>();

    private HashSet<String> numberDict = new HashSet<String>();

    private static DictionaryFeature instance = new DictionaryFeature();

    private DictionaryFeature() {}

    public static DictionaryFeature getInstance() {
        return instance;
    }

    private int maxWord;

    public void readDict(String path) {
        try {
            maxWord = 0;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] token = line.split("\\s+");
                dict.add(token[0]);
                maxWord = Math.max(maxWord, token[0].length());
            }
            reader.close();
            numberDict.add("○");
            numberDict.add("零");
            numberDict.add("一");
            numberDict.add("二");
            numberDict.add("三");
            numberDict.add("四");
            numberDict.add("五");
            numberDict.add("六");
            numberDict.add("七");
            numberDict.add("八");
            numberDict.add("九");
            numberDict.add("十");
            numberDict.add("百");
            numberDict.add("千");
            numberDict.add("万");
            numberDict.add("亿");
        } catch (IOException e) {
            throw new RuntimeException("Read map not successful");
        }
    }

    public String[] analysis(List<String> list, int type) {
        if (type != 2 && type != 4 && type != 5) {
            throw new RuntimeException("Type must be 2, 4 or 5");
        }
        StringBuilder builder = new StringBuilder();
        for (String s: list) {
            builder.append(s);
        }
        String sentence = builder.toString();
        List<String> result = new ArrayList<String>();
        for (int i = 0; i <= sentence.length(); i++) {
            char current = '-';
            if (i < sentence.length()) {
                current = sentence.charAt(i);
            }
            if (isHan(current) && !numberDict.contains(current + "")) {
                continue;
            }
            else if (numberDict.contains(current + "")) {
                if (i + 1 < sentence.length() && numberDict.contains(sentence.charAt(i + 1) + "")) {
                    int lastPos = getLastPos(result);
                    process(result, sentence, lastPos, i - 1);
                    String placeHolder = current + "";
                    while (i + 1 < sentence.length() && numberDict.contains(sentence.charAt(i + 1) + "")) {
                        placeHolder += sentence.charAt(i + 1);
                        i++;
                    }
                    result.add(placeHolder);
                }
            }
            else if (isP(current)) {
                int lastPos = getLastPos(result);
                process(result, sentence, lastPos, i - 1);
                result.add(current + "");
            }
            else {
                int lastPos = getLastPos(result);
                process(result, sentence, lastPos, i - 1);
                String placeHolder = current + "";
                while (i + 1 < sentence.length() && isOther(sentence.charAt(i + 1))) {
                    placeHolder += sentence.charAt(i + 1);
                    i++;
                }
                result.add(placeHolder);
            }
        }
        String[] tag = toTag(result, type);
        return tag;
    }

    private void process(List<String> result, String sentence, int lastPos, int currentPos) {
        List<String> temp = find(sentence, lastPos, currentPos);
        for (String str: temp) {
            if (str.equals("F") && result.size() > 0 && result.get(result.size() - 1).matches("F+")) {
                result.set(result.size() - 1, result.get(result.size() - 1) + "F");
            }
            else {
                result.add(str);
            }
        }
    }

    private boolean isHan(char current) {
        if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isP(char current) {
        if (isHan(current) || Character.isLetterOrDigit(current)) {
            return false;
        }
        else {
            return true;
        }
    }

    private boolean isOther(char current) {
        if (isHan(current) || isP(current) || numberDict.contains(current + "")) {
            return false;
        }
        else {
            return true;
        }
    }

    private int getLastPos(List<String> result) {
        int count = 0;
        for (String s: result) {
            count += s.length();
        }
        return count;
    }

    private List<String> find(String s, int i, int j) {
        List<String> result = new ArrayList<String>();
        j = Math.min(j, s.length() - 1);
        if (i >= j) {
            return result;
        }
        int index = i;
        while (index <= j) {
            int startIndex = index;
            for (int length = maxWord; length >= 1; length--) {
                int end = index + length - 1;
                if (end > j) {
                    continue;
                }
                String token = s.substring(index, end + 1);
                if (!dict.contains(token) && !numberDict.contains(token)) {
                    continue;
                }
                else {
                    result.add(token);
                    index = end + 1;
                    break;
                }
            }
            if (index == startIndex) {
                //result.add(s.substring(index, index + 1));
                result.add("F");
                index++;
            }
        }
        return result;
    }

//    private boolean helper(List<List<String>> result, List<String> current, String s, int pos) {
//        if (pos == s.length()) {
//            result.add(current);
//            return true;
//        }
//        else {
//            for (int length = maxWord; length >= 1; length--) {
//                int end = pos + length - 1;
//                if (end >= s.length()) {
//                    continue;
//                }
//                String token = s.substring(pos, end + 1);
//                if (!dict.contains(token) && !numberDict.contains(token)) {
//                    continue;
//                }
//                current.add(token);
//                if (helper(result, current, s, end + 1)) {
//                    return true;
//                }
//                current.remove(current.size() - 1);
//            }
//            return false;
//        }
//    }

    private String[] toTag(List<String> result, int type) {
//        List<String> removeFail = new ArrayList<String>();
//        int index = 0;
//        while (index < result.size()) {
//            String current = result.get(index);
//            if (current.equals("FAIL")) {
//                String placeHolder = "?";
//                index++;
//                while (index < result.size() && result.get(index).equals("FAIL")) {
//                    placeHolder += "?";
//                    index++;
//                }
//                removeFail.add(placeHolder);
//            }
//            else if (current.equals("NONWORD")) {
//                String placeHolder = "0";
//                index++;
//                while (index < result.size() && result.get(index).equals("NONWORD")) {
//                    placeHolder += "0";
//                    index++;
//                }
//                removeFail.add(placeHolder);
//            }
//            else {
//                removeFail.add(current);
//                index++;
//            }
//        }
//        result = removeFail;
        List<String> character = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        for (int i = 0; i < result.size(); i++) {
            String word = result.get(i);
            if (word.length() == 1) {
                character.add(word.charAt(0) + "");
                if (type == 2) {
                    tags.add("B");
                }
                else {
                    tags.add("S");
                }
                continue;
            }
            for (int pos = 0; pos < word.length(); pos++) {
                character.add(word.charAt(pos) + "");
                if (pos == 0) {
                    tags.add("B");
                }
                else if (pos == 1 && pos != word.length() - 1) {
                    if (type == 2) {
                        tags.add("E");
                    }
                    else if (type == 4) {
                        tags.add("M");
                    }
                    else {
                        tags.add("B2");
                    }
                }
                else if (pos == word.length() - 1) {
                    tags.add("E");
                }
                else {
                    if (type == 2) {
                        tags.add("E");
                    }
                    else {
                        tags.add("M");
                    }
                }
            }
        }
        String[] array = new String[character.size()];
        for (int i = 0; i < character.size(); i++) {
            array[i] = tags.get(i);
        }
        return array;
    }
}
