package LmlComponents;

import DataStructure.MyHashMap;
import Model.Document;
import Model.DomainData;
import java.util.ArrayList;
import java.util.List;
import FeatureSelection.FeatureSelection;

public class PastInformationStore {

    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại N(t^)
    private MyHashMap xNeg; //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại N(t^)
    private long numPositiveDocument; //Số lượng dữ liệu nhãn dương
    private long numNegativeDocument; //Số lượng dữ liệu nhãn âm
    private MyHashMap dictionary; //Từ điển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    // Các dữ liệu huấn luyện đến thời điểm hiện tại
    private List<Document> trainData;

    public PastInformationStore() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        trainData = new ArrayList<>();
    }

    //Thêm thông tin cần thiết vào domain
    public void addDomainData(DomainData domainData) {
        // Thêm văn bản nhãn dương
        for (Document document : domainData.getListPositiveDocument()) {
            trainData.add(document);
            ++numPositiveDocument;
            for (String word : document.getListWord().getListKeys()) {
                xPos.putAdd(word, document.getListWord().get(word));
            }
        }
        // Thêm văn bản nhãn âm
        for (Document document : domainData.getListNegativeDocument()) {
            trainData.add(document);
            ++numNegativeDocument;
            for (String word : document.getListWord().getListKeys()) {
                xNeg.putAdd(word, document.getListWord().get(word));
            }
        }

        //Trích chọn đặc trưng
        FeatureSelection fs = new FeatureSelection(trainData, 1500);
        List<String> listFeatureSave = fs.getListFeatureSave();

        //Bỏ đi những đặc trưng trong văn bản nhãn dương không có trong list save
        for (String word : xPos.getListKeys()) {
            if (!listFeatureSave.contains(word)) {
                xPos.remove(word);
            }
        }

        //Bỏ đi những đặc trưng trong văn bản nhãn âm không có trong list save
        for (String word : xNeg.getListKeys()) {
            if (!listFeatureSave.contains(word)) {
                xNeg.remove(word);
            }
        }

        // Find dictionary:
        dictionary.combine(xPos);
        dictionary.combine(xNeg);

        // Find sumPos and sumNeg:
        sumPos = xPos.getSumAllValues();
        sumNeg = xNeg.getSumAllValues();
    }

    public List<Document> getTrainData() {
        return trainData;
    }

    public MyHashMap getxPos() {
        return xPos;
    }

    public MyHashMap getxNeg() {
        return xNeg;
    }

    public long getNumPositiveDocument() {
        return numPositiveDocument;
    }

    public long getNumNegativeDocument() {
        return numNegativeDocument;
    }

}
