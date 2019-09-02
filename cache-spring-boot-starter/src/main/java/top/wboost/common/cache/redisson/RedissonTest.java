package top.wboost.common.cache.redisson;

/**
 * @Auther: jwsun
 * @Date: 2019/2/25 18:04
 */
public class RedissonTest {

    static int a = 0;

    public static void main(String[] args) throws InterruptedException {
        for(int i = 0;i < 10;i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(a++);
                /*Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.52.105:6379").setPassword("chinaoly").setTimeout(50000);
                RedissonClient redissonClient = Redisson.create(config);
                RLock lock = redissonClient.getLock("init-security2");
                lock.lock(2, TimeUnit.MINUTES);
                System.out.println(a++);
                lock.unlock();
                redissonClient.shutdown();*/
            }).start();
        }
        Thread.sleep(3000);
        System.out.println("------" + a);
    }

}
