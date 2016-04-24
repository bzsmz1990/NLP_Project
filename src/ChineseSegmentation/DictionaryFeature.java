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
            numberDict.add("零");
            numberDict.add("百");
            numberDict.add("千");
            numberDict.add("万");
            numberDict.add("亿");
        } catch (IOException e) {
            throw new RuntimeException("Read map not successful");
        }
    }

    public String[] analysis(String sentence, int type) {
        if (type != 2 && type != 4 && type != 5) {
            throw new RuntimeException("Type must be 2, 4 or 5");
        }
        List<String> result = new ArrayList<String>();
        int lastIndex = 0;
        for (int i = 0; i <= sentence.length(); i++) {
            char current = '-';
            if (i < sentence.length()) {
                current = sentence.charAt(i);
            }
            if (isHan(current) && !numberDict.contains(current)) {
                continue;
            }
            else if (lastIndex == i) {
                if (i != sentence.length()) {
                    result.add("NONWORD");
                }
                lastIndex++;
            }
            else {
                String part = sentence.substring(lastIndex, i);
                List<List<String>> temp = find(part);
                while (temp.isEmpty()) {
                    result.add("FAIL");
                    if (part.length() == 1) {
                        break;
                    }
                    part = part.substring(1, part.length());
                    temp = find(part);
                }
                if (temp.isEmpty()) {
                    continue;
                }
                List<String> first = temp.get(0);
                for (String str: first) {
                    result.add(str);
                }
                if (i != sentence.length()) {
                    result.add("NONWORD");
                }
                lastIndex = i + 1;
            }
        }
        String[] tag = toTag(result, type);
        return tag;
    }

    private boolean isHan(char current) {
        if (Character.UnicodeScript.of(current) == Character.UnicodeScript.HAN) {
            return true;
        }
        else {
            return false;
        }
    }

    private List<List<String>> find(String s) {
        List<List<String>> result = new ArrayList<List<String>>();
        helper(result, new ArrayList<String>(), s, 0);
        return result;
    }

    private boolean helper(List<List<String>> result, List<String> current, String s, int pos) {
        if (pos == s.length()) {
            result.add(current);
            return true;
        }
        else {
            for (int length = maxWord; length >= 1; length--) {
                int end = pos + length - 1;
                if (end >= s.length()) {
                    continue;
                }
                String token = s.substring(pos, end + 1);
                if (!dict.contains(token)) {
                    continue;
                }
                current.add(token);
                if (helper(result, current, s, end + 1)) {
                    return true;
                }
                current.remove(current.size() - 1);
            }
            return false;
        }
    }

    private String[] toTag(List<String> result, int type) {
        List<String> removeFail = new ArrayList<String>();
        int index = 0;
        while (index < result.size()) {
            String current = result.get(index);
            if (current.equals("FAIL")) {
                String placeHolder = "?";
                index++;
                while (index < result.size() && result.get(index).equals("FAIL")) {
                    placeHolder += "?";
                    index++;
                }
                removeFail.add(placeHolder);
            }
            else if (current.equals("NONWORD")) {
                String placeHolder = "0";
                index++;
                while (index < result.size() && result.get(index).equals("NONWORD")) {
                    placeHolder += "0";
                    index++;
                }
                removeFail.add(placeHolder);
            }
            else {
                removeFail.add(current);
                index++;
            }
        }
        result = removeFail;
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
