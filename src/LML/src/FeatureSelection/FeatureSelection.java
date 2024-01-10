package FeatureSelection;

import DataStructure.MyHashMap;
import Model.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureSelection {

    private List<String> listFeatureSave;
    private List<Feature> listFeature;
    private int numPositiveDocument;

    public FeatureSelection(List<Document> trainData, int numberOfFeatureSelection) {
        //Danh sách đặc trưng cần
        listFeatureSave = new ArrayList<>();
        //Danh sách tất cả đặc trưng
        listFeature = new ArrayList<>();

        //Mảng chứa từ điển của mỗi document trong train data
        MyHashMap[] lhm = new MyHashMap[trainData.size()];

        //Từ điển của toàn bộ train data -> tính D(f) và D(+,f)
        MyHashMap dictionary = new MyHashMap();
        int index = 0;
        numPositiveDocument = 0;
        for (Document document : trainData) {
            lhm[index] = new MyHashMap();
            if (document.getDocumentLabel() == 1) {
                ++numPositiveDocument;
            }
            for (String word : document.getListWord().getListKeys()) {
                dictionary.putAdd(word, document.getListWord().get(word));
                lhm[index].putAdd(word, document.getListWord().get(word));
            }
            ++index;
        }

        for (String str : dictionary.getListKeys()) {
            //D(f),  D(+,f)
            int count_feature = 0, count_feature_pos = 0;
            for (int i = 0; i < trainData.size(); i++) {
                if (lhm[i].get(str) > 0) {
                    ++count_feature;
                    if (trainData.get(i).getDocumentLabel() == 1) {
                        ++count_feature_pos;
                    }
                }
            }
            //Thêm vào list đặc trưng
            listFeature.add(new Feature(str, IG(trainData, count_feature, count_feature_pos)));
        }
        //Sắp xếp lại danh sách feature
        Collections.sort(listFeature);
        //Lâý các đặc trưng cần thiết rồi lưu vào list save
        for (int i = 0; i < numberOfFeatureSelection; i++) {
            listFeatureSave.add(listFeature.get(i).getStr());
        }
    }

    public double H(double d) {
        if (d == 0) {
            return 0.00;
        }
        return d * Math.log(d);
    }

    //Information Gain
    public double IG(List<Document> trainData, int count_feature, int count_feature_pos) {
        int count_not_feature_pos = numPositiveDocument - count_feature_pos;

        //P(f) = D(f)/D
        double p_f = (double) count_feature / trainData.size();
        //P(n_f) = 1 - P(f)
        double p_not_f = 1 - p_f;
        //P(+|f) = D(+,f) / D(f)
        double p_pos_f = (double) count_feature_pos / count_feature;
        //P(-|f) = 1 - P(+|f)
        double p_neg_f = 1 - p_pos_f;
        //P(+|n_f) = D(+,n_f) / (D - D(f))
        double p_pos_not_f = (double) (count_not_feature_pos) / (trainData.size() - count_feature);
        //P(-|n_f) = 1 - P(+|n_f)
        double p_neg_not_f = 1 - p_pos_not_f;

        double IGVal = p_f * H(p_pos_f) + p_f * H(p_neg_f) + p_not_f * H(p_pos_not_f) + p_not_f * H(p_neg_not_f);
        return IGVal;

    }

    public List<String> getListFeatureSave() {
        return listFeatureSave;
    }

}
