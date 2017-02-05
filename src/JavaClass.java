import java.util.*;

/**
 * Created by hadoop on 17-1-6.
 */
public class JavaClass {
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }


    public List<Map> getList() {
        List<Map> mList = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("Hello", "Hello Scala");
        mList.add(map);

        Map<String, String> map2 = new HashMap<>();
        map2.put("Haha", "hhhhhhhhhhhhhh");
        mList.add(map2);

        return mList;
    }

    public List<String> getStrList() {
        List<String> mList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mList.add("HelloScala_" + i);
        }
        return mList;
    }

    public List<Integer> getIntList() {
        List<Integer> mIntList = new ArrayList<>();
        for(Integer i = 1; i <= 10; i++) {
            mIntList.add(i);
        }
        return mIntList;
    }

    public Integer[] getIntArray() {
        Integer[] mArray = new Integer[10];
        for(Integer i = 1; i <= 10; i++) {
            mArray[i-1] = i;
        }
        return mArray;
    }


    public static ArrayList<FeatFireData> getFeatFireData() {
        ArrayList<FeatFireData> mFeatList = new ArrayList<>();
        Random r = new Random();

        for (int i = 0; i < 10; ++i) {
            FeatFireData mData = new FeatFireData();
            mData.barcode = "2310000" + i;
            mData.channel = i + "";
            mData.m1 = r.nextInt(2);
            mData.m2 = r.nextInt(2);
            mData.m3 = r.nextInt(2);
            mData.m4 = r.nextInt(2);
            mData.m5 = r.nextInt(2);
            mData.m6 = r.nextInt(2);
            mData.predict = r.nextInt(2);
            System.out.println(mData);
            mFeatList.add(mData);
        }

        return mFeatList;
    }

    public ArrayList<String> getFeatFireStr() {
        ArrayList<String> mArr = new ArrayList<>();
        Random r = new Random();

        for (int i = 0; i < 10; ++i) {
            FeatFireData mData = new FeatFireData();
            mData.barcode = "2310000" + i;
            mData.channel = i + "";
            mData.m1 = r.nextInt(2);
            mData.m2 = r.nextInt(2);
            mData.m3 = r.nextInt(2);
            mData.m4 = r.nextInt(2);
            mData.m5 = r.nextInt(2);
            mData.m6 = r.nextInt(2);
            mData.label = r.nextInt(2);
            mData.predict = r.nextInt(2);

            mArr.add(mData.toStr());
        }

        return mArr;
    }
}
