import conflux.web3j.Cfx;
import conflux.web3j.request.Epoch;
import conflux.web3j.request.LogFilter;
import conflux.web3j.response.Log;
import conflux.web3j.types.Address;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class track extends Thread {

    Cfx cfx = Cfx.create("https://test.confluxrpc.org/v2", 3, 1000);


    public void getLog(long _fromEpoch, long _toEpoch) {
        LogFilter lf = new LogFilter();
        Epoch fromEpoch = Epoch.numberOf(_fromEpoch);
        lf.setFromEpoch(fromEpoch);
        Epoch toEpoch = Epoch.numberOf(_toEpoch);
        lf.setToEpoch(toEpoch);
        // System.out.printf("from %s\n", _fromEpoch);
        List<Address> addresses = new ArrayList<Address>();
        addresses.add(new Address("cfxtest:achm7rp1p42rvxh908up7c6a29r6nrt5f67xp4jm1g"));
        lf.setAddress(addresses);
        List<List<String>> _topics = new ArrayList<List<String>>();
        List<String> topic = new ArrayList<>();
        topic.add("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");
        _topics.add(topic);
        lf.setTopics(_topics);
        try {

            Log.Response logs = cfx.getLogs(lf).send();
            List<Log> log = logs.getResult();
            if (log.size()>0) {
                System.out.println(log.size());
                for (int i=0; i<log.size(); i++) {
                    String txHash = log.get(i).getTransactionHash().get();
                    System.out.println(txHash);
                    // ... 可以对比下是否和数据库中的txHash一致
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long startEpoch= 42995460;

    public void run() {
        while(true) {
            // 多次对比下最新的确定块是否比目前的业务Epoch高；
            // 如果等待三秒后还是小于业务Epoch，则说明节点数据不同步，退出进程。
            for (int i = 0; i<3; i++) {
                BigInteger currentEpoch = cfx.getEpochNumber().sendAndGet();
                BigInteger confrimedEpoch = currentEpoch.subtract(BigInteger.valueOf(5));
                if (confrimedEpoch.compareTo(BigInteger.valueOf(startEpoch)) < 0) {
                    if (i==2) {
                        System.out.println("节点账本不同步");
                        System.exit(1);
                    }
                    try {
                        sleep(1000);
                        System.out.printf("wait for confirmed epoch growing, current confirmed epoch is %s\n", confrimedEpoch);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
            getLog(startEpoch,startEpoch);
            startEpoch++;
        }
    }

    public static void main(String args[]) {
        track ta = new track();
        ta.run();
    }

}
