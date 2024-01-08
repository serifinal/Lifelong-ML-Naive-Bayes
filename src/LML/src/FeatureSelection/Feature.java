package FeatureSelection;

public class Feature implements Comparable<Feature> {

    private String str;
    private double IGValue;

    public Feature(String str, double IGValue) {
        this.str = str;
        this.IGValue = IGValue;
    }

    public String getStr() {
        return str;
    }

    public double getIGValue() {
        return IGValue;
    }

    //So sánh IG của 2 feature
    @Override
    public int compareTo(Feature o) {
        return -Double.compare(IGValue, o.getIGValue());
    }

}
