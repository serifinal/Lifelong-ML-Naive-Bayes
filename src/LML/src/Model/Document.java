package Model;

import DataStructure.MyHashMap;

public class Document {

    private MyHashMap listWord;

    //Nhãn dữ liệu
    private int documentLabel;
    private long size;
    private String filePath;
    private double need;

    public Document() {
        listWord = new MyHashMap();
        documentLabel = -1;
        size = 0;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public MyHashMap getListWord() {
        return listWord;
    }

    public void setListWord(MyHashMap listWord) {
        this.listWord = listWord;
    }

    public int getDocumentLabel() {
        return documentLabel;
    }

    public void setDocumentLabel(int documentLabel) {
        this.documentLabel = documentLabel;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setNeed(double val) {
        need = val;
    }

    public double need() {
        return need;
    }

    public double getNeed() {
        double ans = 0;
        for (String str : listWord.getListKeys()) {
            ans += listWord.get(str);
        }
        return ans;
    }

}
