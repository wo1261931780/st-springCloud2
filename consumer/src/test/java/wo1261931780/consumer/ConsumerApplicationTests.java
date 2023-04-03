package wo1261931780.consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@SpringBootTest
class ConsumerApplicationTests {
	
	@Test
	void contextLoads() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.3.128.13");
		factory.setPort(5672);
		factory.setVirtualHost("/");
		factory.setUsername("itcast");
		factory.setPassword("123321");
		
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String demoQueue = "demoQueue";
		channel.queueDeclare(demoQueue, true, false, false, null);// 声明队列
		// channel.basicConsume(demoQueue, true, (consumerTag, message) -> {
		// 	log.info("接收到的消息是：" + new String(message.getBody()));
		// }, consumerTag -> {
		// });
		
		// 4.消费消息，也叫接收消息
		channel.basicConsume(demoQueue, true, new DefaultConsumer(channel) {
			// DefaultConsumer，默认消费者
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
			                           AMQP.BasicProperties properties, byte[] body) throws IOException {
				// handleDelivery，处理消息的代码
				// 5.处理消息
				String message = new String(body);// 消息体用byte[]接收，需要转换成字符串
				log.info("接收到消息：【" + message + "】");
			}
		});// 这里只是完成了绑定，消息还没有接收到
		log.info("等待接收消息。。。。");// 这里的代码会先执行，因为消息接收是异步的
		// 接收到消息以后，消息会立即被删除
		// 实际上，如果每次接收都用这种类似springMVC的结构，会非常麻烦
		// 所以我们使用了spring-amqp，它是spring对rabbitmq的封装
		// spring-amqp也是基于rabbitmq的原生api实现的
	}
	
}
