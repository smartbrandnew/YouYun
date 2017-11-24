package uyun.bat.gateway.api.mq;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.common.config.Config;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抓取amq管理页面获取mq的状态信息
 */
public class MqCrawler {
    private static final String JMS_URLS = Config.getInstance().get("mq.brokerURL", "failover:(tcp://localhost:61616)");
    private static final Logger LOG = LoggerFactory.getLogger(MqCrawler.class);
    private static final double BROKER_THRESHOLD = Config.getInstance().get("jms.storage.limit", 90);
    private static final String QUEUE_NAME = Config.getInstance().get("jms.metric.queue", "");

    private String mqAdminUrl;
    private String login;
    // queue xml: http://10.1.60.251:8161/admin/xml/queues.jsp
    private Document queueDoc;
    // admin xml: http://10.1.60.251:8161/admin/index.jsp
    private Document adminDoc;

    public MqCrawler() {
        this.login = basicAuth();
        this.mqAdminUrl = getUrlFromCluster();
        this.queueDoc = queueDoc();
        this.adminDoc = adminDoc();
    }

    private Document adminDoc() {
        try {
            return Jsoup.connect(String.format("http://%s/admin/index.jsp", this.mqAdminUrl)).header("Authorization", login).get();
        } catch (IOException e) {
            LOG.error("无法连接到mq管理界面:{}, 连接地址:{}", e.getMessage(), mqAdminUrl);
        }
        return null;
    }

    public boolean isHealthy() {
        return getStorageUsage() < BROKER_THRESHOLD;
    }	

    public String getInfo() {
        return String.format("MQ state error，Enqueued:%s, Dequeued:%s, StorageUsage:%s", getEnQueueSize(QUEUE_NAME), getDeQueueSize(QUEUE_NAME),
                getStorageUsage());
    }

    private Document queueDoc() {
        try {
            return Jsoup.connect(String.format("http://%s/admin/xml/queues.jsp", this.mqAdminUrl)).header("Authorization", login).get();
        } catch (IOException e) {
            LOG.error("Cann't connect to mq managment interface:{}, Connection URL:{}", e.getMessage(), mqAdminUrl);
        }
        return null;
    }

    private int getConsumerCount(String queueName) {
        Element queueState = this.queueDoc.select(String.format("[name=%s]", queueName)).first().getElementsByTag("stats").first();
        return (int) Long.parseLong(queueState.attr("consumerCount"));
    }

    private long getQueueSize(String queueName) {
        Element queueState = this.queueDoc.select(String.format("[name=%s]", queueName)).first().getElementsByTag("stats").first();
        return Long.parseLong(queueState.attr("size"));
    }

    /**
     * 获取队列出队数
     *
     * @return
     */
    private long getDeQueueSize(String queueName) {
        Element queueState = this.queueDoc.select(String.format("[name=%s]", queueName)).first().getElementsByTag("stats").first();
        return Long.parseLong(queueState.attr("dequeueCount"));
    }


    /**
     * 获取队列入队数
     *
     * @return
     */
    private long getEnQueueSize(String queueName) {
        Element queueState = this.queueDoc.select(String.format("[name=%s]", queueName)).first().getElementsByTag("stats").first();
        return Long.parseLong(queueState.attr("enqueueCount"));
    }




    // 目前只有一个broker，这里可能会有坑。
    public int getStorageUsage() {
        Elements table = this.adminDoc.select("h2 + table");
        Element store = table.select("td:contains(Store percent used)").first();
        Element value = store.siblingElements().first();
        return Integer.parseInt(value.text());
    }


    // Basic auth 登录信息
    // 默认登录 admin/admin
    private String basicAuth() {
        String username = "admin";
        String password = "admin";
        String login = username + ":" + password;
        String base64 = new String(Base64.getEncoder().encode(login.getBytes()));
        return "Basic " + base64;
    }

    private String getUrlFromCluster() {
        String[] urlsFromMq = getUrlsFromMq(JMS_URLS);
        // 简单轮询
        for (String url : urlsFromMq) {
            try {
                Jsoup.connect(String.format("http://%s/admin/index.jsp", url)).header("Authorization", login).get();
                return url;
            } catch (IOException e) {
                // 忽略无法连接的备用机
            }
        }
        // 所有url都无法连接时抛出异常
        throw new RuntimeException("Cann't gain mq state，can not attach!");
    }

    private String[] getUrlsFromMq(String jmsUrl) {
        String pattern = "failover:\\((.+)\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(jmsUrl);
        if (m.find()) {
            // tcp://10.1.60.251:61616,tcp://10.1.60.252:61616,tcp://10.1.60.251:61616
            String urlStr = m.group(1);
            String[] urls = urlStr.split(",");
            String[] result = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                String adminHost = urls[i].replace("61616", "8161").substring(6);
                result[i] = adminHost;
            }
            return result;
        } else {
            throw new IllegalArgumentException("mq.brokerURL config format error，cann't be resolved: " + jmsUrl);
        }
    }

    public static void main(String[] args) throws IOException {
        MqCrawler mqCrawler = new MqCrawler();
        int storePercentUsed = mqCrawler.getStorageUsage();
        mqCrawler.getQueueSize(QUEUE_NAME);
        System.out.println(storePercentUsed);
        System.out.println(mqCrawler.getDeQueueSize(QUEUE_NAME));
        System.out.println(mqCrawler.getEnQueueSize(QUEUE_NAME));
        System.out.println(mqCrawler.getQueueSize(QUEUE_NAME));
        System.out.println(mqCrawler.getInfo());
    }
}
