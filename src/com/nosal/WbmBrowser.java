package com.nosal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WbmBrowser {
    LinkedHashMap<String, String> dictionary;
    Path wbmPath;
    private LinkedList<String> regexTab;

    public WbmBrowser(String path) {
        this.dictionary = new LinkedHashMap<>();
        this.wbmPath = Paths.get(path);
        this.regexTab = new LinkedList<>();
        regexTab.add("(?<=\\[\")[a-z]*?(?=\"\\])");
    }

    public String findTranslation(String key) {
        if (dictionary.containsKey(key)) {
            return dictionary.get(key);
        } else {
            return null;
        }
    }

    public void lookForKeys(String regex) {
        Object[] paths = null;
        try {
            paths = Files.walk(this.wbmPath).toArray();
        } catch (Exception e) {
            System.out.println("error");
        }
        for (Object object : paths) {
            System.out.println(object.toString());
            File fileChosen = new File(object.toString());
            String content = null;
            if (!fileChosen.isDirectory()) {
                if (fileChosen.getName().endsWith(".json") || fileChosen.getName().endsWith(".js") || fileChosen.getName().endsWith(".php")) {
                    try {
                        content = Files.readString(Paths.get(object.toString()));
                    } catch (Exception e) {
                        System.out.println("error");
                    }
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        if (!dictionary.containsKey(matcher.group())) {
                            dictionary.put(matcher.group(), "PL_" + matcher.group());
                            System.out.println("new key: " + matcher.group());
                        }
                    }

                }
            }

        }

    }

    public void searchWbmForKeys() {
        for (String regex : regexTab) {
            lookForKeys(regex);
        }
    }

    public void translateWbm() {
        Object[] paths = null;
        try {
            paths = Files.walk(this.wbmPath).toArray();
        } catch (Exception e) {
            System.out.println("error");
        }
        for (Object object : paths) {
            System.out.println(object.toString());
            File fileChosen = new File(object.toString());
            String content = null;
            if (!fileChosen.isDirectory()) {
                if (fileChosen.getName().endsWith(".json") || fileChosen.getName().endsWith(".js") || fileChosen.getName().endsWith(".php")) {
                    try {
                        content = Files.readString(Paths.get(object.toString()));
                    } catch (Exception e) {
                        System.out.println("open error");
                    }
                    for (String regex : this.regexTab) {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(content);
                        while (matcher.find()) {

                            if (dictionary.containsKey(matcher.group())) {
                                //System.out.println("original: "+content.substring(matcher.start()-10, matcher.end())+10);
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(content.substring(0, matcher.start()));
                                stringBuilder.append(findTranslation(matcher.group()));
                                stringBuilder.append(content.substring(matcher.end()));
                                content = stringBuilder.toString();

                                System.out.println("translated: " + matcher.group() + " into: " + dictionary.get(matcher.group()));
                                matcher = pattern.matcher(content);
                                //System.out.println("result: "+content.substring(matcher.start()-10, matcher.end())+10);
                            }
                        }
                    }
                    try {
                        Files.delete(Paths.get(object.toString()));
                    } catch (Exception e) {
                        System.out.println("delete error");
                    }
                    try {
                        Files.writeString(Paths.get(object.toString()), content, StandardOpenOption.CREATE_NEW);
                    } catch (Exception e) {
                        System.out.println("save error");
                    }
                }
            }

        }
    }
}
