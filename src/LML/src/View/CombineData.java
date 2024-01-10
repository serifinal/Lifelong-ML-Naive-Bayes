package View;

import DataStructure.MyHashMap;
import DataStructure.Pair;
import Library.Library;
import Model.Document;
import Model.DomainData;
import java.util.List;
import FeatureSelection.FeatureSelection;
import LmlComponents.PastInformationStore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CombineData {

    private DomainData[] domainData;
    private PastInformationStore[] pis;
    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; // Từ điển
    private double sumPos;
    private double sumNeg;
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public CombineData() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        pis = new PastInformationStore[4];
        Library library = new Library();
        domainData = library.readAllDomainData();
        for (int i = 0; i < 4; i++) {
            pis[i] = new PastInformationStore();
            pis[i].addDomainData(domainData[i]);
        }
    }

    public List<String> readLF(File f) throws FileNotFoundException {
        List<String> ans = new ArrayList<>();
        Scanner scanner = new Scanner(f);
        while (scanner.hasNext()) {
            ans.add(scanner.nextLine());
        }
        scanner.close();
        return ans;
    }

    public void buildModelAndTest(int targetDomain, int fold, int test) throws FileNotFoundException {
        File fTrain = new File("testing//" + test + "_Train.txt");
        File fTest = new File("testing//" + test + "_Test.txt");

        List<String> lfTrain = readLF(fTrain);
        List<String> lfTest = readLF(fTest);

        System.out.print("TEST DOMAIN: " + targetDomain + " / FOLD: " + fold + " : ");
        Pair<List<Document>, List<Document>> pair = domainData[targetDomain].getDataFoldDivide(lfTrain, lfTest);
        List<Document> trainData = pair.getSecond();
        List<Document> testData = pair.getFirst();

        // Build model
        buildModel(trainData, targetDomain);

        // Test model
        testing(testData);
    }

    public void buildModel(List<Document> trainData, int targetDomain) {
        numPositiveDocument = 0;
        numNegativeDocument = 0;
        xPos.clear();
        xNeg.clear();
        dictionary.clear();

        for (Document document : trainData) {
            if (document.getDocumentLabel() == 0) {
                ++numNegativeDocument;
            } else {
                ++numPositiveDocument;
            }
            for (String word : document.getListWord().getListKeys()) {
                if (document.getDocumentLabel() == 0) {
                    xNeg.putAdd(word, document.getListWord().get(word));
                } else {
                    xPos.putAdd(word, document.getListWord().get(word));
                }
            }
        }

        int numFS = 2500;
        FeatureSelection featureSelection = new FeatureSelection(trainData, numFS);
        List<String> listFeatureSave = featureSelection.getListFeatureSave();

        for (String str : xPos.getListKeys()) {
            if (!listFeatureSave.contains(str)) {
                xPos.remove(str);
            }
        }

        for (String str : xNeg.getListKeys()) {
            if (!listFeatureSave.contains(str)) {
                xNeg.remove(str);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (i != targetDomain) {
                xPos.combine(pis[i].getxPos());
                xNeg.combine(pis[i].getxNeg());
            }
        }

        dictionary.combine(xPos);
        dictionary.combine(xNeg);

        sumPos = xPos.getSumAllValues();
        sumNeg = xNeg.getSumAllValues();
    }

    //Tính Precision, Recall, F1
    public void testing(List<Document> testData) {

        int tp = 0, fn = 0, fp = 0;

        for (Document document : testData) {

            // Naive Bayes
            int predict = predictLabel(document);

            //Dự đoán là positive nhưng kết quả thực tế là negative (FP)
            if (document.getDocumentLabel() == 0 && predict == 1) {
                fp++;
            }

            if (document.getDocumentLabel() == 1) {
                if (predict == 1) {
                    tp++;
                }
                else {
                    fn++;
                }
            }
        }

        //Precision
        double P = (tp * 1.00) / (tp + fp);

        //Recall
        double R = (tp * 1.00) / (tp + fn);

        //F1
        double F1 = (2 * P * R) / (P + R);

        System.out.println("TP: "+tp + " FN: " + fn + " FP: " + fp + " F1: " + F1);
        System.out.println("====================================================");
    }

    //Naive Bayes
    public int predictLabel(Document di) {
        //P(+)
        double pPos = Math.log(numPositiveDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        //P(-)
        double pNeg = Math.log(numNegativeDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        for (String word : di.getListWord().getListKeys()) {
            if (dictionary.get(word) > 0) {
                long numWord = (long) di.getListWord().get(word);
                pPos = pPos + numWord * getpPos(word, sumPos);
                pNeg = pNeg + numWord * getpNeg(word, sumNeg);
            }
        }
        if (pPos > pNeg) {
            return 1;
        } else {
            return 0;
        }
    }

    //P(x_i|+) laplace
    public double getpPos(String word, double sumPos) {
        return Math.log(1 + xPos.get(word)) - Math.log(dictionary.size() + sumPos);
    }

    //P(x_i|-) laplace
    public double getpNeg(String word, double sumNeg) {
        return Math.log(1 + xNeg.get(word)) - Math.log(dictionary.size() + sumNeg);
    }

    public static void main(String[] args) throws IOException {
        int test = 1;
        CombineData combineData = new CombineData();
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 10; j++) {
                combineData.buildModelAndTest(i, j, test);
                ++test;
            }
        }
    }
}
