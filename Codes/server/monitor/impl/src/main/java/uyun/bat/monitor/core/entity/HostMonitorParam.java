package uyun.bat.monitor.core.entity;

import uyun.bat.common.config.Config;
import uyun.bat.monitor.api.entity.Options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostMonitorParam implements MonitorParam {

	final static boolean isZH = Config.getInstance().isChinese();
	
    private String status;

    private String period;

    private List<TagEntry> tags;

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

    @Override
    public Map<String, String> getParamMap() {
        Map<String, String> map = new HashMap<>();
        if(isZH)
        	map.put(MonitorParam.DURATION, period.replace("m", "分钟").replace("h", "小时"));
        else
        	map.put(MonitorParam.DURATION, period.replace("m", "minutes").replace("h", "hours"));
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
}
