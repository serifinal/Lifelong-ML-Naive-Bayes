package Model;

import DataStructure.Pair;
import java.util.ArrayList;
import java.util.List;

public class DomainData {

    //Danh sách dữ liệu mang nhãn dương
    private List<Document> listPositiveDocument;
    //Danh sách dữ liệu mang nhãn âm
    private List<Document> listNegativeDocument;

    public DomainData() {
        listPositiveDocument = new ArrayList<>();
        listNegativeDocument = new ArrayList<>();
    }

    public List<Document> getListPositiveDocument() {
        return listPositiveDocument;
    }

    public List<Document> getListNegativeDocument() {
        return listNegativeDocument;
    }

    //Lấy dữ liệu sau khi chia fold : cặp (train,test)
    public Pair<List<Document>, List<Document>> getDataFoldDivide(List<String> lfTrain, List<String> lfTest) {
        Pair<List<Document>, List<Document>> ans = new Pair<>();
        List<Document> testData = new ArrayList<>();
        List<Document> trainData = new ArrayList<>();

        //Duyệt qua những dữ liệu mang nhãn dương
        for (Document document : listPositiveDocument) {
            if (lfTrain.contains(document.getFilePath())) {
                trainData.add(document);
            }
            if (lfTest.contains(document.getFilePath())) {
                testData.add(document);
            }
        }

        //Duyệt qua những dữ liệu mang nhãn âm
        for (Document document : listNegativeDocument) {
            if (lfTrain.contains(document.getFilePath())) {
                trainData.add(document);
            }
            if (lfTest.contains(document.getFilePath())) {
                testData.add(document);
            }
        }
        ans.setFirst(testData);
        ans.setSecond(trainData);
        return ans;
    }

}
