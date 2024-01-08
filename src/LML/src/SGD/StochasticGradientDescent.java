package SGD;

import DataStructure.MyHashMap;
import Model.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StochasticGradientDescent {

    private MyHashMap xPos;
    private MyHashMap xNeg;
    private MyHashMap dictionary;
    private double sumPos;
    private double sumNeg;
    private List<Document> trainData;
    private double dicSize;

    public StochasticGradientDescent(List<Document> trainData, MyHashMap nPos, MyHashMap nNeg) {
        this.trainData = trainData;

        xPos = new MyHashMap();
        xNeg = new MyHashMap();
        dictionary = new MyHashMap();

        xPos.combine(nPos);
        xNeg.combine(nNeg);
        dictionary.combine(xPos);
        dictionary.combine(xNeg);
        sumPos = xPos.getSumAllValues();
        sumNeg = xNeg.getSumAllValues();
        for (Document doc : trainData) {
            doc.setNeed(doc.getNeed());
        }
        dicSize = dictionary.size();
    }

    public void run() {
        double learningRate = 0.01;
        for (int loop = 1; loop <= 10; loop++) {
            // Shuffle data:
            Collections.shuffle(trainData);
            // Update:
            for (Document document : trainData) {
                if (document.getDocumentLabel() == 1) {
                    updateWeight(document, learningRate);
                }
                else {
                    updateWeight(document, learningRate);
                }
            }
        }
    }

    //Cập nhật weight
    private void updateWeight(Document di, double learningRate) {
        double dh, ne;
        for (String word : di.getListWord().getListKeys()) {
            if (dictionary.get(word) > 0) {
                if (di.getDocumentLabel() == 1) {
                    // Positive:

                    dh = dh_f_plus_i_x_plus_u(word, di);
                    //Xích ma trừ đi giá trị X
                    sumPos = sumPos - xPos.get(word);

                    //Cập nhật X theo SGD
                    ne = xPos.get(word) - learningRate * dh;
                    ne = Math.max(ne, 0);

                    //Cập nhật X
                    xPos.putChange(word, ne);

                    //Cập nhật xích ma
                    sumPos = sumPos + ne;

                    dh = dh_f_plus_i_x_minus_u(word, di);
                    sumNeg = sumNeg - xNeg.get(word);
                    ne = xNeg.get(word) - learningRate * dh;
                    ne = Math.max(ne, 0);
                    xNeg.putChange(word, ne);
                    sumNeg = sumNeg + ne;

                } else {
                    // Negative:

                    dh = dh_f_minus_i_x_plus_u(word, di);
                    sumPos = sumPos - xPos.get(word);
                    ne = xPos.get(word) - learningRate * dh;
                    ne = Math.max(ne, 0);
                    xPos.putChange(word, ne);
                    sumPos = sumPos + ne;

                    dh = dh_f_minus_i_x_minus_u(word, di);
                    sumNeg = sumNeg - xNeg.get(word);
                    ne = xNeg.get(word) - learningRate * dh;
                    ne = Math.max(ne, 0);
                    xNeg.putChange(word, ne);
                    sumNeg = sumNeg + ne;

                }
            }
        }
    }

    //Đạo hàm f(+,x) theo x(+,u)
    private double dh_f_plus_i_x_plus_u(String word, Document di) {
        if (di.getListWord().get(word) > 0) {
            double p1 = -di.getListWord().get(word) / (1 + xPos.get(word));
            double p2 = di.getListWord().get(word) / (dicSize + sumPos);
            double p3 = di.need();
            p3 = p3 - di.getListWord().get(word);
            p3 = p3 / (dicSize + sumPos);
            return p1 + p2 + p3;
        }
        return 0.00;
    }

    //Đạo hàm f(+,x) theo x(-,u)
    private double dh_f_plus_i_x_minus_u(String word, Document di) {
        if (di.getListWord().get(word) > 0) {
            double p1 = di.getListWord().get(word) / (1 + xNeg.get(word));
            double p2 = -di.getListWord().get(word) / (dicSize + sumNeg);
            double p3 = -di.need();
            p3 = p3 + di.getListWord().get(word);
            p3 = p3 / (dicSize + sumNeg);
            return p1 + p2 + p3;
        }
        return 0.00;
    }

    //Đạo hàm f(-,x) theo x(+,u)
    private double dh_f_minus_i_x_plus_u(String word, Document di) {
        return -dh_f_plus_i_x_plus_u(word, di);
    }

    //Đạo hàm f(-,x) theo x(-,u)
    private double dh_f_minus_i_x_minus_u(String word, Document di) {
        return -dh_f_plus_i_x_minus_u(word, di);
    }

    public MyHashMap getxPos() {
        for (String str : xPos.getListKeys()) {
            if (xPos.get(str) <= 0) {
                xPos.remove(str);
            }
        }
        return xPos;
    }

    public MyHashMap getxNeg() {
        for (String str : xNeg.getListKeys()) {
            if (xNeg.get(str) <= 0) {
                xNeg.remove(str);
            }
        }
        return xNeg;
    }

}
