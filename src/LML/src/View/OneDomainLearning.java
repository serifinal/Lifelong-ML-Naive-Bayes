package View;

import DataStructure.MyHashMap;
import DataStructure.Pair;
import Library.Library;
import Model.Document;
import Model.DomainData;
import java.util.List;
import FeatureSelection.FeatureSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class OneDomainLearning {

    private DomainData[] domainData;
    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; //Từ điển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public OneDomainLearning() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        Library library = new Library();
        //Đọc dữ liệu từ dataset rồi lưu vào các domain
        domainData = library.readAllDomainData();
    }

    //Đọc nội dung trong file rồi lưu list path
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

        //List các file path dùng để train
        List<String> lfTrain = readLF(fTrain);
        //List các file path dùng để test
        List<String> lfTest = readLF(fTest);

        System.out.print("TEST DOMAIN: " + targetDomain + " / FOLD: " + fold + " : ");
        //Lấy ra cặp file train và file test
        Pair<List<Document>, List<Document>> pair = domainData[targetDomain].getDataFoldDivide(lfTrain, lfTest);
        //Lấy file train
        List<Document> trainData = pair.getSecond();
        //Lấy file test
        List<Document> testData = pair.getFirst();

        // Build model
        buildModel(trainData);

        // Test model
        testing(testData);
    }

    public void buildModel(List<Document> trainData) {
        numPositiveDocument = 0;
        numNegativeDocument = 0;
        xPos.clear();
        xNeg.clear();
        dictionary.clear();

        for (Document document : trainData) {
            if (document.getDocumentLabel() == 0) {
                //Thêm số văn bản (-)
                ++numNegativeDocument;
            } else {
                //Thêm số văn bản (+)
                ++numPositiveDocument;
            }
            for (String word : document.getListWord().getListKeys()) {
                if (document.getDocumentLabel() == 0) {
                    //Cập nhật xNeg
                    xNeg.putAdd(word, document.getListWord().get(word));
                } else {
                    //Cập nhật xPos
                    xPos.putAdd(word, document.getListWord().get(word));
                }
            }
        }

        //Trích chọn đặc trưng
        FeatureSelection featureSelection = new FeatureSelection(trainData, 1500);
        //List lưu 1500 đặc trưng tốt nhất
        List<String> listFeatureSave = featureSelection.getListFeatureSave();

        for (String str : xPos.getListKeys()) {
            //Bỏ đi những đặc trưng trong văn bản nhãn dương không có trong list save
            if (!listFeatureSave.contains(str)) {
                xPos.remove(str);
            }
        }

        for (String str : xNeg.getListKeys()) {
            //Bỏ đi những đặc trưng trong văn bản nhãn âm không có trong list save
            if (!listFeatureSave.contains(str)) {
                xNeg.remove(str);
            }
        }

        //Combine vào từ điển
        dictionary.combine(xPos);
        //Combine vào từ điển
        dictionary.combine(xNeg);

        //Tổng số từ xuất hiện trong các văn bản nhãn (+)
        sumPos = xPos.getSumAllValues();
        //Tổng số từ xuất hiện trong các văn bản nhãn (-)
        sumNeg = xNeg.getSumAllValues();
    }

    //Tính Precision, Recall, F1
    public void testing(List<Document> testData) {

        int tp = 0, fn = 0, fp = 0, tn = 0;

        for (Document document : testData) {
            int predict = predictLabel(document);

            if (document.getDocumentLabel() == 0){
                if(predict==1){
                    fp++;
                }
                else {
                    tn++;
                }
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

        System.out.println("TP: "+tp + " FN: " + fn + " FP: " + fp + " TN: "+ tn +" F1: " + F1);
        System.out.println("====================================================");
    }

    //Naive Bayes
    public int predictLabel(Document di) {
        //P(+) = (N+) / (N)
        double pPos = Math.log(numPositiveDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        //P(-) = (N-) / (N)
        double pNeg = Math.log(numNegativeDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        for (String word : di.getListWord().getListKeys()) {
            if (dictionary.get(word) > 0) {
                //Số lần xuất hiện của 1 từ trong văn bản d1
                long numWord = (long) di.getListWord().get(word);
                //Xác suất để di mang nhãn dương
                pPos = pPos + numWord * getpPos(word, sumPos);
                //Xác suất để di mang nhãn âm
                pNeg = pNeg + numWord * getpNeg(word, sumNeg);
            }
        }
        if (pPos > pNeg) {
            return 1;
        } else {
            return 0;
        }
    }

    //P(w|+) laplace = (1 + X(+,w)) / (|V| + sumPos)
    public double getpPos(String word, double sumPos) {
        return Math.log(1 + xPos.get(word)) - Math.log(dictionary.size() + sumPos);
    }

    //P(w|-) laplace = (1 + X(-,w)) / (|V| + sumNeg)
    public double getpNeg(String word, double sumNeg) {
        return Math.log(1 + xNeg.get(word)) - Math.log(dictionary.size() + sumNeg);
    }

    public static void main(String[] args) throws IOException {
        int test = 1;
        OneDomainLearning oneDomainLearning = new OneDomainLearning();
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 10; j++) {
                oneDomainLearning.buildModelAndTest(i, j, test);
                ++test;
            }
        }
    }
}
