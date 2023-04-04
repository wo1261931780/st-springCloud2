package wo1261931780.publisher;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class SpringAmqpTests {
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Test
	void contextLoads() {
		String queueName = "demoQueue.queue";
		String message = "junw demo project";
		rabbitTemplate.convertAndSend(queueName, message);// 发送消息
	}
	
	@Test
	void testFanout() throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			String queueName = "demoQueue.queue";
			String message = "junw demo project--" + i;
			rabbitTemplate.convertAndSend(queueName, message);// 发送消息
			Thread.sleep(20);
		}
		log.info("testFanout，循环调用50次: " + 50);
	}
	
	/**
	 * 测试发送到交换机
	 */
	@Test
	public void testSendEE() {
		String exchangeName = "demoExchange.fanout";
		String message = "junw demo project";
		rabbitTemplate.convertAndSend(exchangeName, "", message);// 发送消息
	}
	
	@Test
	public void testSendDirectMsg() {
		String exchangeName = "demoExchange.fanout";
		String message = "junw demo project";
		rabbitTemplate.convertAndSend(exchangeName, "demo3", message);// 发送指定key的消息
	}
	
	@Test
	public void testSendTopicMsg() {
		String exchangeName = "demoTopic.topic";// 话题类型的交换机
		String message = "junw demo project";
		rabbitTemplate.convertAndSend(exchangeName, "china.weather", message);// 发送指定key的消息
	}
	
	@Test
	public void testSendObject(){
		HashMap<String, Object> objectObjectHashMap = new HashMap<>();
		objectObjectHashMap.put("name","junw");
		objectObjectHashMap.put("age",18);
		// 发送对象
		// 我们的rabbitTemplate默认使用的是JDK的序列化机制，所以我们发送的对象必须实现Serializable接口
		// 否则在里面就会展示出字节的数据
		rabbitTemplate.convertAndSend("demoQueue.queue",objectObjectHashMap);
		
	}
}
