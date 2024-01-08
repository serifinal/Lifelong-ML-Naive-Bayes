package Library;

import DataStructure.MyHashMap;
import Model.Document;
import Model.DomainData;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Library {

    public Library() {

    }

    private boolean isACorrectWord(String str) {
        //Nếu chuỗi là kí tự đặc biệt
        List<String> deleteWords = Arrays.asList(
                ".", "#", "''", "?", ",", "`", "%", ":", "-"
        );
        if (deleteWords.contains(str)) {
            return false;
        }

        //Nếu chuỗi không có kí tự nào từ a -> z  (False)
        int count_char = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 'a' && str.charAt(i) <= 'z') {
                ++count_char;
            }
        }
        if (count_char == 0) {
            return false;
        }

        //Nếu chuỗi có chứa kí tự đặc biệt
        return !(str.contains("-") || str.contains(":") || str.contains("%") || str.contains(".") || str.contains("#") || str.contains("?") || str.contains(",") || str.contains("`"));
    }

    //n-grams
    public Document readDocument(File file) {
        Scanner scanner;
        MyHashMap listWord = new MyHashMap();
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] subStr = line.split(" ");
                //Unigram
                for (String str : subStr) {
                    if (isACorrectWord(str)) {
                        listWord.putAdd(str, 1.00);
                    }
                }
                //Bigram
                for (int i = 1; i < subStr.length; i++) {
                    String str = subStr[i - 1] + " " + subStr[i];
                    if (isACorrectWord(str)) {
                        listWord.putAdd(str, 1.00);
                    }
                }
                //Trigram
                for (int i = 2; i < subStr.length; i++) {
                    String str = subStr[i - 2] + " " + subStr[i - 1] + " " + subStr[i];
                    if (isACorrectWord(str)) {
                        listWord.putAdd(str, 1.00);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Document document = new Document();
        document.setListWord(listWord);
        return document;
    }

    public DomainData[] readAllDomainData() {
        DomainData[] domainData = new DomainData[4];

        String domainName[] = {"avforums", "avsforum", "digitalcamerareview", "howard"};

        for (int i = 0; i < 4; i++) {
            domainData[i] = new DomainData();
            String pathFolder = "dataset/" + domainName[i];
            for (int j = 0; j <= 1; j++) {
                String pathFolderLabel = pathFolder + "/" + j;
                File folder = new File(pathFolderLabel);
                for (File file : folder.listFiles()) {
                    Document document = readDocument(file);
                    document.setDocumentLabel(j);
                    document.setFilePath(file.getPath());
                    if (j == 0) {
                        domainData[i].getListNegativeDocument().add(document);
                    } else {
                        domainData[i].getListPositiveDocument().add(document);
                    }
                }
            }
        }

        return domainData;
    }

    public static void main(String[] args) {
        Library l = new Library();
        File f = new File("dataset/avforums/0/108973.txt");
        Document d =  l.readDocument(f);
        MyHashMap mhm = d.getListWord();
        for(String s : mhm.getListKeys()){
            System.out.println(s+" "+mhm.get(s));
        }


//        Library l = new Library();
//        String test = "I want to eat chinese food";
//        MyHashMap b = new MyHashMap();
//        String[] subStr = test.split(" ");
//        //Unigram
//        for (String str : subStr) {
//            if (l.isACorrectWord(str)) {
//                b.putAdd(str, 1.00);
//            }
//        }
//        //Bigram
//        for (int i = 1; i < subStr.length; i++) {
//            String str = subStr[i - 1] + " " + subStr[i];
//            if (l.isACorrectWord(str)) {
//                b.putAdd(str, 1.00);
//            }
//        }
//        //Trigram
//        for (int i = 2; i < subStr.length; i++) {
//            String str = subStr[i - 2] + " " + subStr[i - 1] + " " + subStr[i];
//            if (l.isACorrectWord(str)) {
//                b.putAdd(str, 1.00);
//            }
//        }
//
//        for(String s : b.getListKeys()){
//            System.out.println(s + " : "+ b.get(s));
//        }
    }
}
