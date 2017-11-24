package uyun.bat.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.whale.common.util.text.Unit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lilm on 17-6-29.
 * 单位换算工具类
 */
public class UnitUtil {

    private static Logger log = LoggerFactory.getLogger(UnitUtil.class);

    // 需要进行单位换算的单位
    private static Unit[][] units = {{Unit.B, Unit.KB, Unit.MB, Unit.GB, Unit.TB},
                                     {Unit.B_s, Unit.KB_s, Unit.MB_s, Unit.GB_s, Unit.TB_s},
                                     {Unit.B_ms, Unit.KB_ms, Unit.MB_ms, Unit.GB_ms, Unit.TB_ms},
                                     {Unit.ms, Unit.second, Unit.minute, Unit.hour}};

    public static String transformUnit(double val, String unit) {
        if (unit == null) {
            return val + "";
        }
        Unit finalUnit;
        Unit matchUnit = Unit.SETS[0][0].getUnitByCode(unit);
        if (val > 1) {
            int unitI = 0;
            int unitJ = 0;
            boolean isExist = false;
            for (int i = 0; i < units.length; i++) {
                Unit[] unitArr = units[i];
                for (int j = 0; j < unitArr.length; j++) {
                    unitJ = j;
                    Unit one = units[i][j];
                    if (one.equals(matchUnit)) {
                        isExist = true;
                        break;
                    }
                }
                if (isExist) {
                    unitI = i;
                    break;
                }
            }
            // 不需要进位转化的单位
            if (!isExist) {
                return val + unit;
            }
            double d = val;
            double tmp;
            while (true) {
                tmp = d;
                // unitI == 3 为时间单位
                if (unitI == 3) {
                    if (unitJ == 0) {
                        d = d / 1000;
                    } else if (unitJ == 3) {
                        break;
                    } else {
                        d = d / 60;
                    }
                } else {
                    if (unitJ == 4) {
                        break;
                    }
                    d = d / 1024;
                }
                if (d < 1) {
                    d = tmp;
                    break;
                }
                unitJ++;
            }
            val = d;
            finalUnit = units[unitI][unitJ];
        } else {
            finalUnit = matchUnit;
        }
        BigDecimal bd = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
        if (finalUnit == null) {
            return bd.doubleValue() + "";
        }
        return bd.doubleValue() + finalUnit.getCode();
    }

    /**
     * 对指标序列数据最大值进行转换单位
     * @param val 二维数组字符串 [[1498579200000,1.2828621063E9]]
     * @param unit
     * @return
     */
    public static String transformMaxValUnit(String val, String unit) {
        if (val != null) {
            if (val.contains("[[") && val.contains("]]")) {
                try {
                    // 去除收尾[]符
                    val = val.substring(1, val.length() - 1);
                    List<String> valList = new ArrayList<String>();
                    while (val.length() > 1) {
                        if (val.contains("[")) {
                            val = val.substring(val.indexOf("[") + 1);
                        }
                        String one = "";
                        if (val.contains("]")) {
                            one = val.substring(0, val.indexOf("]"));
                            val = val.substring(val.indexOf("]"));
                        }
                        if ("".equals(one)) {
                            break;
                        }
                        valList.add(one);
                    }
                    double[][] doubleArr = new double[valList.size()][2];
                    for (int i = 0; i < valList.size(); i++) {
                        String[] oneArr = valList.get(i).split(",");
                        double time = Double.parseDouble(oneArr[0]);
                        double metricVal = Double.parseDouble(oneArr[1]);
                        doubleArr[i][0] = time;
                        doubleArr[i][1] = metricVal;
                    }
                    return transformMaxValUnit(doubleArr, unit);
                } catch (Exception e) {
                    log.error("data format error:", e);
                }
            }
        }
        return null;
    }

    public static String transformMaxValUnit(double[][] val, String unit) {
        if (val != null) {
            return transformUnit(getMaxVal(val), unit);
        }
        return null;
    }

    private static double getMaxVal(double[][] val) {
        double maxVal = 0;
        if (val != null) {
            for (double[] doubles : val) {
                if (doubles[1] > maxVal) {
                    maxVal = doubles[1];
                }
            }
        }
        return maxVal;
    }

    public static void main(String[] args) {
        System.out.println(transformUnit(0, null));
        //0.0s
        System.out.println(transformUnit(0, "s"));
        //50.0ms
        System.out.println(transformUnit(50, "ms"));
        //5.0s
        System.out.println(transformUnit(5000, "ms"));
        //8.33m
        System.out.println(transformUnit(500, "s"));
        //500.0min
        System.out.println(transformUnit(500, "min"));
        //8.33h
        System.out.println(transformUnit(500, "m"));
        //500.0h
        System.out.println(transformUnit(500, "h"));
        //900.0B
        System.out.println(transformUnit(900, "B"));
        //1.0KB
        System.out.println(transformUnit(1024, "B"));
        //1.0KB/s
        System.out.println(transformUnit(1024, "B/s"));
        //1.0KB/ms
        System.out.println(transformUnit(1024, "B/ms"));
        //1.01MB
        System.out.println(transformUnit(1059061.76, "B"));
        //1.11GB
        System.out.println(transformUnit(1159256, "KB"));
        //1.0GB
        System.out.println(transformUnit(1024, "MB"));
        //1.0TB
        System.out.println(transformUnit(1024, "GB"));
        // 1.0TB
        System.out.println(transformMaxValUnit("[[1498579200000,1.2828621063E9]]", "B"));
    }

}
