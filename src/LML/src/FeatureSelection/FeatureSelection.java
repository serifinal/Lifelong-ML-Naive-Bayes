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

        MyHashMap[] lhm = new MyHashMap[trainData.size()];

        //Từ điển
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
            int count_feature = 0, count_feature_pos = 0;
            for (int i = 0; i < trainData.size(); i++) {
                if (lhm[i].get(str) > 0) {
                    ++count_feature;
                    if (trainData.get(i).getDocumentLabel() == 1) {
                        ++count_feature_pos;
                    }
                }
            }
            //
            listFeature.add(new Feature(str, IG(trainData, count_feature, count_feature_pos)));
        }
        //Sắp xếp lại danh sách feature
        Collections.sort(listFeature);
        //Lấy
        for (int i = 0; i < numberOfFeatureSelection; i++) {
            listFeatureSave.add(listFeature.get(i).getStr());
        }
    }

    private double H(double d) {
        if (d == 0) {
            return 0.00;
        }
        return d * Math.log(d);
    }

    //Information Gain
    private double IG(List<Document> trainData, int count_feature, int count_feature_pos) {
        int count_not_feature_pos = numPositiveDocument - count_feature_pos;

        double p_f = (double) count_feature / trainData.size();
        double p_not_f = 1 - p_f;
        double p_pos_f = (double) count_feature_pos / count_feature;
        double p_neg_f = 1 - p_pos_f;
        double p_pos_not_f = (double) (count_not_feature_pos) / (trainData.size() - count_feature);
        double p_neg_not_f = 1 - p_pos_not_f;

        double IGVal = p_f * H(p_pos_f) + p_f * H(p_neg_f) + p_not_f * H(p_pos_not_f) + p_not_f * H(p_neg_not_f);
        return IGVal;

    }

    public List<String> getListFeatureSave() {
        return listFeatureSave;
    }

}
