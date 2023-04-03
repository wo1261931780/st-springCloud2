package wo1261931780.publisher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@SpringBootTest
class PublisherApplicationTests {
	
	@Test
	void contextLoads() throws IOException, TimeoutException {
		// 跟mq建立连接
		ConnectionFactory factory = new ConnectionFactory();// 创建连接工厂
		factory.setHost("192.3.128.13");// 设置连接主机
		factory.setPort(5672);// 设置连接端口，管理控制台15672
		factory.setVirtualHost("/");// 设置虚拟主机
		factory.setUsername("itcast");
		factory.setPassword("123321");
		Connection connection = factory.newConnection();// 创建连接
		Channel connectionChannel = connection.createChannel();// 创建通道
		String demoQueue = "demoQueue";
		connectionChannel.queueDeclare(demoQueue, true, false, false, null);// 声明队列
		
		String message = "junw project";
		connectionChannel.basicPublish("", demoQueue, null, message.getBytes());// 发送消息
		log.info("消息发送成功");
		connectionChannel.close();// 关闭通道
		connection.close();// 关闭连接
	}
	
}
