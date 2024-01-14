package View;

import DataStructure.MyHashMap;
import DataStructure.Pair;
import Library.Library;
import Model.Document;
import Model.DomainData;
import java.util.List;
import FeatureSelection.FeatureSelection;
import LmlComponents.PastInformationStore;
import SGD.StochasticGradientDescent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LML {

    private DomainData[] domainData;
    private PastInformationStore[] pis; //KB
    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; //Từ điển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public LML() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        pis = new PastInformationStore[4];
        Library library = new Library();
        //Đọc dữ liệu từ folder dataset rồi lưu vào domain
        domainData = library.readAllDomainData();
        for (int i = 0; i < 4; i++) {
            pis[i] = new PastInformationStore();
            pis[i].addDomainData(domainData[i]);
        }
    }

    //Đọc nội dung file chia fold rồi lưu list path
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

        //List path các file dùng để train
        List<String> lfTrain = readLF(fTrain);
        //List path các file dùng để test
        List<String> lfTest = readLF(fTest);

        System.out.print("TEST DOMAIN: " + targetDomain + " / FOLD: " + fold + " : ");
        //Lấy ra cặp List file train và test theo fold
        Pair<List<Document>, List<Document>> pair = domainData[targetDomain].getDataFoldDivide(lfTrain, lfTest);

        //List các file dùng để train
        List<Document> trainData = pair.getSecond();
        //List các file dùng để test
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
                //Đếm số văn bản mang nhãn âm (-)
                ++numNegativeDocument;
            } else {
                //Đếm số văn bản mang nhãn dương (+)
                ++numPositiveDocument;
            }
            for (String word : document.getListWord().getListKeys()) {
                if (document.getDocumentLabel() == 0) {
                    //Thêm xNeg
                    xNeg.putAdd(word, document.getListWord().get(word));
                } else {
                    //Thêm xPos
                    xPos.putAdd(word, document.getListWord().get(word));
                }
            }
        }

        //Trích chọn đặc trưng trong train data
        int numFS = 2500;
        FeatureSelection featureSelection = new FeatureSelection(trainData, numFS);
        List<String> listFeatureSave = featureSelection.getListFeatureSave();

        //Bỏ đi những đặc trưng trong văn bản nhãn dương không có trong list save
        for (String str : xPos.getListKeys()) {
            if (!listFeatureSave.contains(str)) {
                xPos.remove(str);
            }
        }

        //Bỏ đi những đặc trưng trong văn bản nhãn âm không có trong list save
        for (String str : xNeg.getListKeys()) {
            if (!listFeatureSave.contains(str)) {
                xNeg.remove(str);
            }
        }

        //Toàn bộ dữ liệu của 3 domain khác target domain và 90% dữ liệu của target domain làm DLHL
        //10% dữ liệu còn lại của target domain làm DLKT
        for (int i = 0; i < 4; i++) {
            if (i != targetDomain) {
                xPos.combine(pis[i].getxPos());
                xNeg.combine(pis[i].getxNeg());
            }
        }

        // Thực hiện Stochastic gradient descent để tìm ra xPos và xNeg mới
        StochasticGradientDescent sgd = new StochasticGradientDescent(trainData, xPos, xNeg);
        sgd.run();

        // Cập nhật lại xPos và xNeg theo kết quả mới từ SGD
        xPos.clear();
        xNeg.clear();
        xPos.combine(sgd.getxPos());
        xNeg.combine(sgd.getxNeg());

        // Find dictionary:
        //Combine vào từ điển
        dictionary.combine(xPos);
        //Combine vào từ điển
        dictionary.combine(xNeg);

        // Find sumPos and sumNeg:
        //Tổng số từ xuất hiện trong các văn bản nhãn (+)
        sumPos = xPos.getSumAllValues();
        //Tổng số từ xuất hiện trong các văn bản nhãn (-)
        sumNeg = xNeg.getSumAllValues();
    }

    //Tính Precision, Recall, F1
    public void testing(List<Document> testData) {

        int tp = 0, fn = 0, fp = 0;

        for (Document document : testData) {

            // Naive Bayes
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
        //P(+) = (N+) / (N)
        double pPos = Math.log(numPositiveDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        //P(-) = (N-) / (N)
        double pNeg = Math.log(numNegativeDocument) - Math.log(numPositiveDocument + numNegativeDocument);
        for (String word : di.getListWord().getListKeys()) {
            if (dictionary.get(word) > 0) {
                long numWord = (long) di.getListWord().get(word);
                //Xác suất để di mang nhãn dương log P(+) + sum logP(x_i|+)
                pPos = pPos + numWord * getpPos(word, sumPos);
                //Xác suất để di mang nhãn âm log P(-) + sum logP(x_i| -)
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
        LML lml = new LML();
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 10; j++) {
                lml.buildModelAndTest(i, j, test);
                ++test;
            }
        }
    }
}
