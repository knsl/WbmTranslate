package com.nosal;


public class Main {

    public static void main(String[] args) {

        WbmBrowser wbmBrowser = new WbmBrowser("D:\\__JAVA\\WbmTranslate\\src\\com\\nosal\\WBM_EN_SRC\\wbm");
        wbmBrowser.readDictionaryFromFile("D:\\__JAVA\\WbmTranslate\\src\\com\\nosal\\slownik.txt");
        System.out.println("wait");
        //wbmBrowser.searchWbmForKeys();
        //wbmBrowser.saveDictionaryToFile("D:\\__JAVA\\WbmTranslate\\src\\com\\nosal\\dictionary.csv");
        wbmBrowser.translateWbm();
//        Object[]paths=null;
//        try{
//            paths =Files.walk(Paths.get("D:\\__JAVA\\WbmTranslate\\src\\com\\nosal\\WBM_PL")).toArray();
//        }catch (Exception e){
//            System.out.println("error");
//        }
//        for(Object object:paths){
//            System.out.println(object.toString());
//            File fileChosen = new File(object.toString());
//            String content =null;
//            if(!fileChosen.isDirectory()){
//            try {
//                content = Files.readString(Paths.get(object.toString()));
//            } catch (Exception e) {
//                System.out.println("error");
//            }
//            System.out.println(content);}
//        }
//
//        try {
//            Files.walk(Paths.get("D:\\__JAVA\\WbmTranslate\\src\\com\\nosal\\WBM_PL")).filter(Files::isRegularFile).forEach(System.out::println);
//            // write your code here
//        }catch (Exception e){
//            System.out.println("error");
//        }
    }
}
