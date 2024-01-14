package View;

import DataStructure.MyHashMap;
import DataStructure.Pair;
import FeatureSelection.FeatureSelection;
import Library.Library;
import LmlComponents.PastInformationStore;
import Model.Document;
import Model.DomainData;
import SGD.StochasticGradientDescent;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LML_Test {

    private DomainData[] domainData;
    private PastInformationStore[] pis;
    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; //Từ điển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm

    public LML_Test() {
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

    public void buildModel(int targetDomain, int test) throws FileNotFoundException {
        File fTrain = new File("testing//" + test + "_Train.txt");
        File fTest = new File("testing//" + test + "_Test.txt");

        //List path các file dùng để train
        List<String> lfTrain = readLF(fTrain);
        //List path các file dùng để test
        List<String> lfTest = readLF(fTest);

//        System.out.print("TEST DOMAIN: " + targetDomain + " / FOLD: " + fold + " : ");
        //Lấy ra cặp List file train và test theo fold
        Pair<List<Document>, List<Document>> pair = domainData[targetDomain].getDataFoldDivide(lfTrain, lfTest);

        //List các file dùng để train
        List<Document> trainData = pair.getSecond();

        // Build model
        buildModel(trainData, targetDomain);
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

        //Combine vào từ điển
        dictionary.combine(xPos);
        //Combine vào từ điển
        dictionary.combine(xNeg);

        //Tổng số từ xuất hiện trong các văn bản nhãn (+)
        sumPos = xPos.getSumAllValues();
        //Tổng số từ xuất hiện trong các văn bản nhãn (-)
        sumNeg = xNeg.getSumAllValues();
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

    //Tách câu tách từ
    private void tokenizer(String inputFile, String outputFile) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(outputFile);
        Scanner scanner = new Scanner(new File(inputFile));

        String content = "";
        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            content = content + " " + line;
        }

        //Sử dụng thư viện Standford NLP tách nội dung trong content thành các câu
        DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(content));
        //Danh sách các từ trong câu
        for (List<HasWord> sentences : dp) {
            //Chuyển đổi danh sách các từ trong câu thành một chuỗi.
            String senStringString = SentenceUtils.listToString(sentences);
            //Chuyển thành chữ thường
            String ans = senStringString.toLowerCase();
            //Nếu không phải chuỗi rỗng
            if (!ans.trim().equals("")) {
                pw.write(ans + "\n");
            }
            //System.out.println(sentences);
        }
        pw.close();
    }

    public static void main(String[] args) throws IOException {
        // Training
        LML_Test lml = new LML_Test();
        lml.buildModel(0, 1);
        System.out.println("HOÀN THÀNH TRAINING");
//        System.out.println(lml.sumNeg);
//        System.out.println(lml.sumPos);

        Scanner scanner = new Scanner(System.in);
        // Test
        while (true) {
            System.out.println(">>>");
            String next = scanner.nextLine();
            String inputFile = "newPostAfter.txt";
            String outFile = "OUT.txt";
            lml.tokenizer(inputFile, outFile);

            // Phân nhãn một dữ liệu mới
            Library library = new Library();
            Document testDoc = library.readDocument(new File(outFile));
            for (String s : testDoc.getListWord().getListKeys()) {
                System.out.println(s + " : " + lml.xPos.get(s) + " - " + lml.xNeg.get(s));
            }
            int predict = lml.predictLabel(testDoc);
            if (predict == 1) {
                System.out.println("Dữ liệu này có mang ý định mua bán");
            } else {
                System.out.println("Dữ liệu này không mang ý định mua bán");
            }
        }
    }
}
