package View;

import DataStructure.MyHashMap;
import Library.Library;
import Model.Document;
import Model.DomainData;
import java.util.List;
import FeatureSelection.FeatureSelection;
import LmlComponents.PastInformationStore;
import java.io.IOException;
import java.util.ArrayList;

public class ThreeTrainOneTest {

    private DomainData[] domainData;
    private PastInformationStore[] pis;
    private MyHashMap xPos;
    private MyHashMap xNeg;
    private MyHashMap dictionary;
    private double sumPos;
    private double sumNeg;
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public ThreeTrainOneTest() {
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

    public void buildModelAndTest(int targetDomain) {

        System.out.print("TEST DOMAIN: " + targetDomain);
        List<Document> trainData = new ArrayList<>();
        List<Document> testData = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (i != targetDomain) {
                trainData.addAll(pis[i].getTrainData());
            }
        }

        testData.addAll(pis[targetDomain].getTrainData());

        // Build model
        buildModel(trainData);

        // Test model
        testing(testData);
    }

    private void buildModel(List<Document> trainData) {
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
        System.out.println("");
        System.out.println("NUM POS: " + numPositiveDocument);
        System.out.println("NUM NEG: " + numNegativeDocument);

        FeatureSelection featureSelection = new FeatureSelection(trainData, 3000);
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

        dictionary.combine(xPos);
        dictionary.combine(xNeg);

        sumPos = xPos.getSumAllValues();
        sumNeg = xNeg.getSumAllValues();
    }

    //Tính Precision, Recall, F1
    private void testing(List<Document> testData) {

        int tp = 0, fn = 0, fp = 0;

        for (Document document : testData) {

            // Use aggregate function:
            int predict = predictLabel(document);

            if (document.getDocumentLabel() == 0 && predict == 1) {
                fp++;
            }

            if (document.getDocumentLabel() == 1) {
                if (predict == 1) {
                    tp++;
                } else {
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
    private double getpPos(String word, double sumPos) {
        return Math.log(1 + xPos.get(word)) - Math.log(dictionary.size() + sumPos);
    }

    //P(x_i|-) laplace
    private double getpNeg(String word, double sumNeg) {
        return Math.log(1 + xNeg.get(word)) - Math.log(dictionary.size() + sumNeg);
    }

    public static void main(String[] args) throws IOException {
        ThreeTrainOneTest threeTrainOneTest = new ThreeTrainOneTest();
        for (int i = 0; i < 4; i++) {
            threeTrainOneTest.buildModelAndTest(i);
        }
    }
}
