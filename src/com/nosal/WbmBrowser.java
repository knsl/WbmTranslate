package com.nosal;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
        regexTab.add("(?<=\"title\":\\{\"default\":\")[^\"{}]+?(?=\",)");
        //regexTab.add("(?<=\\{\"title\":\\{\"default\":\").+?(?=[$])");
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
        //regexTab.add("(?<=timezone-and-format-form-title\"[,]default:\")[^\"{}]+?(?=\",)");
        // regexTab.add("(?<=\\[\\{\"title\":\\{\"default\":\").+?(?=\",)");
        //{"title":{"default":"/// //Submit</button>   {id:"networking",title:" [^",{}]
        // {id:"device-status",title:"Device Status",  e.textContent="Submit",
        //timezone-and-format-form-title",default:"
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
                                if (!matcher.group().equals(findTranslation(matcher.group()))) {
                                    content = stringBuilder.toString();

                                    System.out.println("translated: " + matcher.group());
                                    matcher = pattern.matcher(content);
                                }
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

    public void saveDictionaryToFile(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        Object[] keys = dictionary.keySet().toArray();
        for (int i = 0; i < this.dictionary.size(); i++) {
            stringBuilder.append(keys[i].toString());
            stringBuilder.append("\t");
            stringBuilder.append(dictionary.get(keys[i].toString()));
            stringBuilder.append("\n\r");
        }
        try {
            Files.writeString(Paths.get(path), stringBuilder.toString(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            System.out.println("save error");
        }
    }

    public void readDictionaryFromFile(String path) {
        String content = null;
        System.out.println("trying to open: " + path);
        try {
            content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
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
            dictionary.put(matcher1.group().stripLeading().stripTrailing(), matcher2.group().stripLeading().stripTrailing());
        }

    }
}
