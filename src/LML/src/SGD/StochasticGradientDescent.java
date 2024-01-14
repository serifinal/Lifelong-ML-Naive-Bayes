package SGD;

import DataStructure.MyHashMap;
import Model.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StochasticGradientDescent {

    private MyHashMap xPos; //Các từ w và số lần xuất hiện trong nhãn (+) ở thời điểm hiện tại
    private MyHashMap xNeg;  //Các từ w và số lần xuất hiện trong nhãn (-) ở thời điểm hiện tại
    private MyHashMap dictionary; //Từ diển
    private double sumPos; //Tổng số từ xuất hiện trong các văn bản nhãn (+)
    private double sumNeg; //Tổng số từ xuất hiện trong các văn bản nhãn (-)
    private List<Document> trainData;
    private double dicSize; // |V| số lượng các từ khaác nhau có trong từ điển

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
            // Trộn dữ liệu
            Collections.shuffle(trainData);
            // Cập nhật theo SGD
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

    //Cập nhật weight X
    private void updateWeight(Document di, double learningRate) {
        double dh, xNew;
        for (String word : di.getListWord().getListKeys()) {
            //Nếu từ u xuất hiện trong từ điển
            if (dictionary.get(word) > 0) {
                // Văn bản nhãn dương (+)
                if (di.getDocumentLabel() == 1) {
                    //Đạo hàm f(+,i) theo X(+,u)
                    dh = dh_f_plus_i_x_plus_u(word, di);
                    //Tổng xích ma trừ đi giá trị X(+,u) cũ
                    sumPos = sumPos - xPos.get(word);

                    //Tìm X(+,u) mới theo SGD
                    xNew = xPos.get(word) - learningRate * dh;
                    xNew = Math.max(xNew, 0);

                    //Cập nhật X(+,u) mới
                    xPos.putChange(word, xNew);
                    //Cập nhật tổng xích ma
                    sumPos = sumPos + xNew;

                    //Đạo hàm f(+,i) theo X(-,u)
                    dh = dh_f_plus_i_x_minus_u(word, di);
                    //Tổng xích ma trừ đi giá trị X(-,u) cũ
                    sumNeg = sumNeg - xNeg.get(word);

                    //Tìm X(-,u) mới theo SGD
                    xNew = xNeg.get(word) - learningRate * dh;
                    xNew = Math.max(xNew, 0);

                    //Cập nhật X(-,u) mới
                    xNeg.putChange(word, xNew);
                    //Cập nhật tổng xích ma
                    sumNeg = sumNeg + xNew;

                }
                // Văn bản nhãn âm (-)
                else {
                    //Đạo hàm f(-,i) theo X(+,u)
                    dh = dh_f_minus_i_x_plus_u(word, di);
                    //Tổng xích ma trừ đi giá trị X(+,u) cũ
                    sumPos = sumPos - xPos.get(word);

                    //Tìm X(+,u) mới theo SGD
                    xNew = xPos.get(word) - learningRate * dh;
                    xNew = Math.max(xNew, 0);

                    //Cập nhật X(+,u) mới
                    xPos.putChange(word, xNew);
                    //Cập nhật tổng xích ma
                    sumPos = sumPos + xNew;

                    //Đạo hàm f(-,i) theo X(-,u)
                    dh = dh_f_minus_i_x_minus_u(word, di);
                    //Tổng xích ma trừ đi giá trị X(-,u) cũ
                    sumNeg = sumNeg - xNeg.get(word);

                    //Tìm X(-,u) mới theo SGD
                    xNew = xNeg.get(word) - learningRate * dh;
                    xNew = Math.max(xNew, 0);

                    //Cập nhật X(-,u) mới
                    xNeg.putChange(word, xNew);
                    //Cập nhật tổng xích ma
                    sumNeg = sumNeg + xNew;

                }
            }
        }
    }

    //Đạo hàm f(+,i) theo x(+,u)
    private double dh_f_plus_i_x_plus_u(String word, Document di) {
        //Nếu word xuất hiện trong văn bản di
        if (di.getListWord().get(word) > 0) {
            // -n(u,d_i) / (1 + X(+,u))
            double p1 = -di.getListWord().get(word) / (1 + xPos.get(word));
            // n(u,d_i) / (|V| + sum(X(+,v)))
            double p2 = di.getListWord().get(word) / (dicSize + sumPos);

            //n(all)
            double p3 = di.need();
            //n(w,d_i) = n(all) - n(w,d_u)    w!=u
            p3 = p3 - di.getListWord().get(word);
            // n(w,d_i) / (|V| + sum(X(+,v)))
            p3 = p3 / (dicSize + sumPos);
            return p1 + p2 + p3;
        }
        return 0.00;
    }

    //Đạo hàm f(+,i) theo x(-,u)
    private double dh_f_plus_i_x_minus_u(String word, Document di) {
        //Nếu word xuất hiện trong văn bản di
        if (di.getListWord().get(word) > 0) {
            // n(u,d_i) / (1 + X(-,u))
            double p1 = di.getListWord().get(word) / (1 + xNeg.get(word));
            // -n(u,d_i) / (|V| + sum(X(-,v)))
            double p2 = -di.getListWord().get(word) / (dicSize + sumNeg);

            double p3 = -di.need();
            //n(w,d_i) = n(all) + n(w,d_u)    w!=u
            p3 = p3 + di.getListWord().get(word);
            // n(w,d_i) / (|V| + sum(X(-,v)))
            p3 = p3 / (dicSize + sumNeg);
            return p1 + p2 + p3;
        }
        return 0.00;
    }

    //Đạo hàm f(-,i) theo X(+,u)   = -f(+,i) theo X(+,u)
    private double dh_f_minus_i_x_plus_u(String word, Document di) {
        return -dh_f_plus_i_x_plus_u(word, di);
    }

    //Đạo hàm f(-,i) theo X(-,u)  = -f(+,i) theo X(-,u)
    private double dh_f_minus_i_x_minus_u(String word, Document di) {
        return -dh_f_plus_i_x_minus_u(word, di);
    }

    //Lấy ra X(+,u)
    public MyHashMap getxPos() {
        for (String str : xPos.getListKeys()) {
            if (xPos.get(str) <= 0) {
                xPos.remove(str);
            }
        }
        return xPos;
    }

    //Lấy ra X(-,u)
    public MyHashMap getxNeg() {
        for (String str : xNeg.getListKeys()) {
            if (xNeg.get(str) <= 0) {
                xNeg.remove(str);
            }
        }
        return xNeg;
    }

}
