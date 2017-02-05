import java.io.Serializable;

/**
 * Created by hadoop on 17-1-17.
 * FeatFireData
 */
public class FeatFireData implements Serializable {
    String barcode;
    String channel;
    int m1;
    int m2;
    int m3;
    int m4;
    int m5;
    int m6;
    int label;
    int predict;

    @Override
    public String toString() {
        return "barcode:" + barcode + "channel:" + channel  + "m1:" + m1 +
                "m2:" + m2 + "m3:" + m3 + "m4:" + m4 + "m5:" + m5 +
                "m6:" + m6 + "label:" + label;
    }

    public String toStr() {
        return barcode + "," + m1 + "," + m2 + "," + m3 + "," + m4 + "," + m5 +
                "," + m6 + "," + label;
    }
}
