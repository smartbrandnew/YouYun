package uyun.bat.monitor.core.entity;

import uyun.bat.common.config.Config;
import uyun.bat.monitor.api.entity.Options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMonitorParam implements MonitorParam {

	final static boolean isZH = Config.getInstance().isChinese();
	
    private String status;

    private String period;

    private List<TagEntry> tags;

    private String state;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<TagEntry> getTags() {
        return tags;
    }

    public void setTags(List<TagEntry> tags) {
        this.tags = tags;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public Map<String, String> getParamMap() {
        Map<String, String> map = new HashMap<>();
        if(isZH)
        	map.put(MonitorParam.DURATION, period.replace("m", "分钟").replace("h", "小时"));
        else
        	map.put(MonitorParam.DURATION, period.replace("m", "minutes").replace("h", "hours"));
        map.put(MonitorParam.APP,getState().split("\\.")[0]);
        return map;
    }

    public void setOptions(Options options) {
        if (options != null && options.getThresholds() != null) {
            Map<String, String> thresholds=options.getThresholds();
            for(Map.Entry<String,String> entry:thresholds.entrySet()){
                if (Options.ALERT.equals(entry.getKey())){
                    status= Options.ALERT;
                    return;
                }
                entry.getKey();
            }
            status= Options.WARNING;
        }
    }

    public boolean match(StateMetricData data) {
        if (!data.getName().equals(state))
            return false;
        // 匹配tag

        // 监测器没有tag，则只要指标名匹配都符合
        if (tags == null || tags.size() == 0)
            return true;
        // 监测器有tag但是指标数据没tag
        if (data.getTags() == null || data.getTags().size() == 0)
            return false;
        // 指标数据的tag是否包含监测器的tag
        for (TagEntry monitorTag : tags) {
            boolean isMatach = false;
            for (TagEntry metricTag : data.getTags()) {
                if (matchTagEntry(monitorTag, metricTag)) {
                    isMatach = true;
                    break;
                }
            }
            if (!isMatach)
                return false;
        }
        return true;
    }

    /**
     * 匹配指标tag
     */
    private boolean matchTagEntry(TagEntry monitorTag, TagEntry metricTag) {
        if (!monitorTag.getKey().equals(metricTag.getKey()))
            return false;
        if (monitorTag.getValue() == null || monitorTag.getValue().length() == 0) {
            if (metricTag.getValue() == null || metricTag.getValue().length() == 0)
                return true;
            return false;
        } else {
            if (metricTag.getValue() == null || metricTag.getValue().length() == 0) {
                return false;
            } else {
                // 由于同tagk的tagv会用逗号相加,故判断下里面是否包含本tag
                if (metricTag.getValue().indexOf(',') != -1) {
                    String[] metricTagvs = metricTag.getValue().split(",");
                    String[] monitorTagvs = monitorTag.getValue().split(",");
                    if (metricTagvs.length < monitorTagvs.length)
                        return false;
                    for (String monitorTagv : monitorTagvs) {
                        boolean isContain = false;
                        for (String metricTagv : metricTagvs) {
                            if (metricTagv.equals(monitorTagv)) {
                                isContain = true;
                                break;
                            }
                        }
                        if (!isContain)
                            return false;
                    }
                    return true;
                } else {
                    return monitorTag.getValue().equals(metricTag.getValue());
                }
            }
        }
    }
}
