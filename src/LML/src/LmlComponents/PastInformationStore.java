package LmlComponents;

import DataStructure.MyHashMap;
import Model.Document;
import Model.DomainData;
import java.util.ArrayList;
import java.util.List;
import FeatureSelection.FeatureSelection;

public class PastInformationStore {

    private MyHashMap xPos;
    private MyHashMap xNeg;
    private long numPositiveDocument;
    private long numNegativeDocument;
    private MyHashMap dictionary;
    private double sumPos;
    private double sumNeg;
    private List<Document> trainData;

    public PastInformationStore() {
        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();
        trainData = new ArrayList<>();
    }

    public void addDomainData(DomainData domainData) {
        // add positive document:
        for (Document document : domainData.getListPositiveDocument()) {
            trainData.add(document);
            ++numPositiveDocument;
            for (String word : document.getListWord().getListKeys()) {
                xPos.putAdd(word, document.getListWord().get(word));
            }
        }
        // add negative document:
        for (Document document : domainData.getListNegativeDocument()) {
            trainData.add(document);
            ++numNegativeDocument;
            for (String word : document.getListWord().getListKeys()) {
                xNeg.putAdd(word, document.getListWord().get(word));
            }
        }

        // feature selection:
        FeatureSelection fs = new FeatureSelection(trainData, 1500);
        List<String> listFeatureSave = fs.getListFeatureSave();

        // Clear:
        for (String word : xPos.getListKeys()) {
            if (!listFeatureSave.contains(word)) {
                xPos.remove(word);
            }
        }
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

    public double getpPos(String word) {
        return Math.log(1 + xPos.get(word)) - Math.log(dictionary.size() + sumPos);
    }

    public double getpNeg(String word) {
        return Math.log(1 + xNeg.get(word)) - Math.log(dictionary.size() + sumNeg);
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
