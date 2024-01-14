package View;

import DataStructure.MyHashMap;
import Library.Library;
import Model.Document;
import Model.DomainData;

import java.io.File;
import java.util.List;
import FeatureSelection.FeatureSelection;
import LmlComponents.PastInformationStore;
import java.io.IOException;
import java.util.ArrayList;

public class ThreeTrainOneTest {

    private DomainData[] domainData;
    private PastInformationStore[] pis; //KB
    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; //Từ điển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public ThreeTrainOneTest() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        pis = new PastInformationStore[4];
        Library library = new Library();
        //Đọc dữ liệu từ folder dataset rồi lưu vào domain
        domainData = library.readAllDomainData();
        for (int i = 0; i < 4; i++) {
            pis[i] = new PastInformationStore();
            //Lưu giữ thông tin quá khứ
            pis[i].addDomainData(domainData[i]);
        }
    }

    public void buildModelAndTest(int targetDomain) {

        System.out.print("TEST DOMAIN: " + targetDomain);
        List<Document> trainData = new ArrayList<>();
        List<Document> testData = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (i != targetDomain) {
                //3 domain khác target domain sẽ được lấy làm dữ liệu huấn luyện
                trainData.addAll(pis[i].getTrainData());
            }
        }

        //Target domain được lấy làm dữ liệu kiểm thử
        testData.addAll(pis[targetDomain].getTrainData());

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

        System.out.println("");
//        System.out.println("NUM POS: " + numPositiveDocument);
//        System.out.println("NUM NEG: " + numNegativeDocument);

        //Trích chọn đặc trưng trong train data
        FeatureSelection featureSelection = new FeatureSelection(trainData, 3000);
        //List lưu 3000 đặc trưng tốt nhất
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

        int tp = 0, fn = 0, fp = 0;

        for (Document document : testData) {

            // Dự đoán
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
                //Số lần xuất hiện của 1 từ trong văn bản d1
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
        ThreeTrainOneTest threeTrainOneTest = new ThreeTrainOneTest();
        for (int i = 0; i < 4; i++) {
            threeTrainOneTest.buildModelAndTest(i);
        }
    }
}
