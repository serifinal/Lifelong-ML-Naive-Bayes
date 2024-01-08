import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class KFold {
    //Hàm chia dữ liệu thành các phần bằng nhau
    public static List<List<String>> divideList(List<String> inputList, int numberOfParts) {
        int listSize = inputList.size();
        int partSize = listSize / numberOfParts;

        List<List<String>> dividedLists = new ArrayList<>();

        int startIndex = 0, endIndex = 0;
        for (int i = 0; i < numberOfParts; i++) {
            if (i == numberOfParts - 1) {
                endIndex = startIndex + partSize + (listSize % numberOfParts);
                dividedLists.add(inputList.subList(startIndex, endIndex));
            }
            else{
                endIndex = startIndex + partSize;
                dividedLists.add(inputList.subList(startIndex, endIndex));
                startIndex = endIndex;
            }
        }

        return dividedLists;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("dataset");
        File out = new File("testing");
        out.mkdirs();

        int k=0;

        // data->avforums,avsforum,camera,howard
        for (File f1 : file.listFiles()) {
            List<String> l_0 = new ArrayList<>();
            List<String> l_1 = new ArrayList<>();
            for (File f2 : f1.listFiles()) {
                if (f2.getName().equals("0")) {
                    // txt file
                    for (File f3 : f2.listFiles()) {
                        l_0.add(f3.getPath());
                    }
                } else {
                    for (File f3 : f2.listFiles()) {
                        l_1.add(f3.getPath());
                    }
                }
            }

            //Chia dữ liệu làm 10 phần
            List<List<String>> dividedLists_0 = divideList(l_0,10);
            List<List<String>> dividedLists_1 = divideList(l_1,10);

            for (int i = 0; i < 10; i++) {
                int j = i+1;
                //Phần tương ứng
                List<String> temp_0 = dividedLists_0.get(i);
                List<String> temp_1 = dividedLists_1.get(i);

//                // Tiếp tục chia nhỏ 10 phần: 9 cho train, 1 cho test
//                List<String> testSubset0 = temp_0.subList(0, splitIndex0);
//                List<String> trainSubset0 = temp_0.subList(splitIndex0, temp_0.size());
//
//                List<String> testSubset1 = temp_1.subList(0, splitIndex1);
//                List<String> trainSubset1 = temp_1.subList(splitIndex1, temp_1.size());

                int index = j+k;
                // Write paths to files
                File fTest = new File(out.getPath() + "//" + index + "_Test.txt");
                File fTrain = new File(out.getPath() + "//" + index + "_Train.txt");

                PrintWriter pwTest = new PrintWriter(fTest);
                PrintWriter pwTrain = new PrintWriter(fTrain);

                //Ghi dữ liệu vào file Test
                for (String path : temp_1) {
                    pwTest.println(path);
                }
                for (String path : temp_0) {
                    pwTest.println(path);
                }

                // Ghi dữ liệu vào file Train
                for (List<String> trainSubset : dividedLists_1.subList(0, i)) {
                    for (String path : trainSubset) {
                        pwTrain.println(path);
                    }
                }

                for (List<String> trainSubset : dividedLists_1.subList(i + 1, 10)) {
                    for (String path : trainSubset) {
                        pwTrain.println(path);
                    }
                }

                for (List<String> trainSubset : dividedLists_0.subList(0, i)) {
                    for (String path : trainSubset) {
                        pwTrain.println(path);
                    }
                }

                for (List<String> trainSubset : dividedLists_0.subList(i + 1, 10)) {
                    for (String path : trainSubset) {
                        pwTrain.println(path);
                    }
                }


                pwTest.close();
                pwTrain.close();
            }
            k+=10;
        }
    }
}
