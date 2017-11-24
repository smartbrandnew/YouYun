package uyun.bat.monitor.api.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollectionUtil {

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static void main(String[] args) {
        String[] arr1=new String[]{"1","2"};
        String[] arr2=new String[]{};
        String[] results=concat(arr1,arr2);
        for(String str:results){
            System.out.println(str);
        }
    }


    public static  <T>List<T> getListByPage(List<T> list, int page,int pageSize) {
        if (null==list||list.size()<1){
            return null;
        }
        int total=list.size();
        int totalPage=total%pageSize==0? (total/pageSize ): (total/pageSize+1);
        if (page>totalPage||page<1){
            return null;
        }
        int first=(page-1)*pageSize;
        int end=first+pageSize;

        List<T> results=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            if(i>=first&&i<end){
                results.add(list.get(i));
            }
        }
        return results;
    }


}
