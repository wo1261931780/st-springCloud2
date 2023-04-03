package wo1261931780.publisher;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
	
	@Test
	public void testSendEE() {
		String exchangeName = "demoExchange.fanout";
		String message = "junw demo project";
		rabbitTemplate.convertAndSend(exchangeName, "", message);// 发送消息
	}
}
