package com.nosal;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String configPath;
        System.out.println("Podaj ścieżkę pliku config.txt");
        Scanner scanner = new Scanner(System.in);
        configPath = scanner.nextLine();
        WbmBrowser wbmBrowser = new WbmBrowser(configPath);
        wbmBrowser.readDictionaryFromFile();
        wbmBrowser.translateWbm();

    }
}
