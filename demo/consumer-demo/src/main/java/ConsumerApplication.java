import com.my.rpc.ReferenceConfig;
import com.my.rpc.RpcBootstrap;
import com.my.rpc.SayHelloRpc;
import com.my.rpc.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务消费方
 *
 * @Author : Williams
 * Date : 2023/12/4 18:48
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        log.debug("consumer start....");

        ReferenceConfig<SayHelloRpc> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(SayHelloRpc.class);

        RpcBootstrap bootstrap = RpcBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper", "127.0.0.1:2181"))
                .reference(reference);


        /*
         * 获取代理对象
         * 代理做了些什么，
         * 1. 连接注册中心
         * 2. 拉取服务列表
         * 3. 选择一个服务并建立连接
         * 4. 发送请求, 携带一些信息(接口名, 参数列表, 方法名字),  获得结果
         */
        SayHelloRpc helLoYrpc = reference.get();
        String hello = helLoYrpc.sayHi("你好");
        log.debug("hello ==={}", hello);
    }
}
