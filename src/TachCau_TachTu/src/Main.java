import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;

public class Main {
    //Hàm đọc dữ liệu từ file và lưu sang file mới
    public static void readDataFromFile(File f, File out) throws FileNotFoundException {
        Scanner scanner = new Scanner(f);
        PrintWriter pw = new PrintWriter(out);
        //Biến lưu title và nội dung bài đăng
        String strDoc = "";
        //Đọc từng dòng trong file
        while (scanner.hasNext()) {
            String str = scanner.nextLine().trim();
            //Lấy ra nội dung trong thẻ Content
            if (str.contains("<Content>")) {
                str = str.replaceAll("<Content>", "");
                str = str.replaceAll("</Content>", "");
                strDoc = strDoc + " " + str;
            }
            //Lấy ra title của bài đăng
            else if (str.contains("<Title>")) {
                str = str.replaceAll("<Title>", "");
                str = str.replaceAll("</Title>", "");
                strDoc = strDoc + str + " . ";
            }
        }
        //Sử dụng thư viện Standford NLP tách nội dung trong strDoc thành các câu
        DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(strDoc));
        //Danh sách các từ trong các
        for (List<HasWord> sentences : dp) {
            //Chuyển list thành string
            String senStringString = SentenceUtils.listToString(sentences);
            //Chuyển thành chữ thường
            String ans = senStringString.toLowerCase();
            if (!ans.trim().equals("")) {
                pw.write(ans + "\n");
            }
        }
        scanner.close();
        pw.close();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        File file = new File("data");
        File dataset = new File("dataset");
        //data->avforums,avsforum,camera,howard
        for (File f1 : file.listFiles()) {
            //0,1
            for (File f2 : f1.listFiles()) {
                //txt file
                for (File f3 : f2.listFiles()) {
                    File out = new File(dataset + "//" +f1.getName()+ "//" +f2.getName());
                    //Tạo thư mục
                    out.mkdirs();
                    File out2 = new File(out + "//" + f3.getName());
                    //Tạo file txt
                    out2.createNewFile();
                    //Lưu file txt
                    readDataFromFile(f3, out2);
                }
            }
        }
//        File temp = new File("newPost.txt");
//        File tempOut = new File("newPostAfter.txt");
//        tempOut.createNewFile();
//        readDataFromFile(temp,tempOut);
    }

}
