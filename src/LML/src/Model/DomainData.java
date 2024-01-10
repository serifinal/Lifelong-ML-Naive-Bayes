package Model;

import DataStructure.Pair;
import java.util.ArrayList;
import java.util.List;

public class DomainData {

    //Danh sách văn bản mang nhãn dương
    private List<Document> listPositiveDocument;
    //Danh sách văn bản mang nhãn âm
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

        //Duyệt qua những văn bản mang nhãn dương
        for (Document document : listPositiveDocument) {
            //Nếu trong list path file train có chứa thì thêm vào train data
            if (lfTrain.contains(document.getFilePath())) {
                //Thêm văn bản vào dữ liệu huấn luện
                trainData.add(document);
            }
            //Nếu trong list path file test có chứa thì thêm vào test data
            if (lfTest.contains(document.getFilePath())) {
                //Thêm văn bản vào dữ liệu kiểm thử
                testData.add(document);
            }
        }

        //Duyệt qua những văn bản mang nhãn âm
        for (Document document : listNegativeDocument) {
            //Nếu trong list path file train có chứa thì thêm vào train data
            if (lfTrain.contains(document.getFilePath())) {
                //Thêm văn bản vào dữ liệu huấn luện
                trainData.add(document);
            }
            //Nếu trong list path file train có chứa thì thêm vào test data
            if (lfTest.contains(document.getFilePath())) {
                //Thêm văn bản vào dữ liệu kiểm thử
                testData.add(document);
            }
        }
        ans.setFirst(testData);
        ans.setSecond(trainData);
        return ans;
    }

}
