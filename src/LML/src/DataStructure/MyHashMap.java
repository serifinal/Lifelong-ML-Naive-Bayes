package DataStructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MyHashMap {

    private HashMap<String, Double> hashMap;

    public MyHashMap() {
        hashMap = new HashMap<>();
    }

    //Thêm một từ mới
    public void putChange(String key, Double value) {
        hashMap.put(key, value);
    }

    //Thêm số lần xuất hiện
    public void putAdd(String key, Double value) {
        putChange(key, value + get(key));
    }

    //Lấy ra số lần xuất hiện của một từ
    public double get(String key) {
        Double value = hashMap.get(key);
        if (value == null) {
            return 0.00;
        }
        return value;
    }

    //Lấy tổng số lượng từ đang có trong từ điển
    public double getSumAllValues() {
        double sum = 0.00;
        for (String key : hashMap.keySet()) {
            sum += get(key);
        }
        return sum;
    }

    //Lấy ra danh sách key
    public Set<String> getListKeys() {
        Set<String> keySet = new HashSet<>();
        for (String key : hashMap.keySet()) {
            keySet.add(key);
        }
        return keySet;
    }

    //Tổng hợp
    public void combine(MyHashMap hashMap2) {
        Set<String> keySet2 = hashMap2.getListKeys();
        for (String key : keySet2) {
            this.putAdd(key, hashMap2.get(key));
        }
    }

    public void clear() {
        hashMap.clear();
    }

    public long size() {
        return hashMap.size();
    }

    public void remove(String key) {
        hashMap.remove(key);
    }

}
