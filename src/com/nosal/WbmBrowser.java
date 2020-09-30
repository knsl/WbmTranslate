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
    private String pathOfWbm;
    private String pathOfDictionary;


    public WbmBrowser(String path) {
        this.dictionary = new LinkedHashMap<>();
        this.regexTab = new LinkedList<>();
        this.pathOfWbm = findPathOfWbm(path);
        this.wbmPath = Paths.get(this.pathOfWbm);
        this.pathOfDictionary = findPathOfDictionary(path);
        regexTab.add("(?<=\"title\":\\{\"default\":\")[^\"{}]+?(?=\",)");
        regexTab.add("(?<=\\{title:\\{default:\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=\\{fallback:\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=fallback:\")[^\",{}]+?(?=\"[}])");
        regexTab.add("(?<=\\{\"fallback\":\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=\\{.\"fallback\":.\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=\\{id:\"networking\",title:\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=\\{id:\"device-status\",title:\")[^\",{}]+?(?=\",)");
        regexTab.add("(?<=[>])[a-zA-Z]+?(?=[<][/]button[>])");
        regexTab.add("(?<=note:\\{default:\")[^\"{}]+?(?=\",)");
        regexTab.add("(?<=note:\\{default:['])[^{}]+?(?=['],)");
        regexTab.add("(?<=\"note\":\\{\"default\":\")[^\"{}]+?(?=\",)");
        regexTab.add("(?<=\"note\":\\{\"default\":['])[^{}]+?(?=['],)");
        regexTab.add("(?<=textContent[=]\")[a-zA-Z]+?(?=\")");
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
                        content = new String(Files.readAllBytes(Paths.get(object.toString())));
                    } catch (Exception e) {
                        System.out.println("error");
                    }
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        if (!dictionary.containsKey(matcher.group())) {
                            if (!matcher.group().equals("Reboot")) {
                                dictionary.put(matcher.group(), matcher.group());
                                System.out.println("new key: " + matcher.group());
                            }
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
                        content = new String(Files.readAllBytes(Paths.get(object.toString())));
                    } catch (Exception e) {
                        System.out.println("open error");
                    }
                    for (String regex : this.regexTab) {
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(content);
                        while (matcher.find()) {

                            if (dictionary.containsKey(matcher.group())) {

                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(content.substring(0, matcher.start()));
                                stringBuilder.append(findTranslation(matcher.group()));
                                stringBuilder.append(content.substring(matcher.end()));
                                if (!matcher.group().equals(findTranslation(matcher.group()))) {
                                    content = stringBuilder.toString();

                                    System.out.println("translated: " + matcher.group());
                                    matcher = pattern.matcher(content);
                                }

                            }
                        }
                    }
                    try {
                        Files.delete(Paths.get(object.toString()));
                    } catch (Exception e) {
                        System.out.println("delete error");
                    }
                    try {
                        Files.write(Paths.get(object.toString()), content.getBytes(), StandardOpenOption.CREATE_NEW);
                    } catch (Exception e) {
                        System.out.println("save error");
                    }
                }
            }

        }
    }

    public void saveDictionaryToFile() {
        String path = this.pathOfDictionary;
        StringBuilder stringBuilder = new StringBuilder();
        Object[] keys = dictionary.keySet().toArray();
        for (int i = 0; i < this.dictionary.size(); i++) {
            stringBuilder.append("&\t");
            stringBuilder.append(keys[i].toString());
            stringBuilder.append("\t$\t");
            stringBuilder.append(dictionary.get(keys[i].toString()));
            stringBuilder.append("\r\n");
        }
        try {
            Files.write(Paths.get(path), stringBuilder.toString().getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            System.out.println("save error");
        }
    }

    public void readDictionaryFromFile() {
        String path = this.pathOfDictionary;
        String content = null;
        System.out.println("trying to open: " + path);
        try {
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            System.out.println("error");
            System.out.println(e);
        }
        Pattern pattern = Pattern.compile("[&][^$&]+?[$][^$&]+?\r\n");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            System.out.println("matcher: " + matcher.group());
            Pattern pattern1 = Pattern.compile("(?<=[&]).*?(?=[$])");
            Matcher matcher1 = pattern1.matcher(matcher.group());
            matcher1.find();
            System.out.println(matcher1.group());
            Pattern pattern2 = Pattern.compile("(?<=[$]).*?(?=\r\n)");
            Matcher matcher2 = pattern2.matcher(matcher.group());
            matcher2.find();
            System.out.println(matcher2.group());
            dictionary.put(matcher1.group().replaceAll("^\\s", "").replaceAll("//s$", ""), matcher2.group().replaceAll("^\\s", "").replaceAll("//s$", ""));
        }

    }

    private String findPathOfWbm(String path) {
        String currentPath = path;
        System.out.println(currentPath);
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            System.out.println("error test 1");
        }
        Pattern pattern = Pattern.compile("(?<=wbmPath=\").*?(?=\")");
        Matcher matcher = pattern.matcher(content);
        matcher.find();
        return matcher.group();
    }

    private String findPathOfDictionary(String path) {
        String currentPath = path;
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            System.out.println("error");
        }
        Pattern pattern = Pattern.compile("(?<=dictionaryPath=\").*?(?=\")");
        Matcher matcher = pattern.matcher(content);
        matcher.find();
        return matcher.group();
    }
}
