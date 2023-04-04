package wo1261931780.consumer.component;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.consumer.component
 *
 * @author liujiajun_junw
 * @Date 2023-04-03-02  星期二
 * @description
 */
@Component
@Slf4j
public class SpringListener {
	
	@RabbitListener(queues = "demoQueue.queue")
	public void listenerQueue(String msg) {
		log.info("SpringListener: " + msg);
		// 只要监听到消息，就会自动确认
	}
	
	@RabbitListener(queues = "demoQueue.queue")
	public void listenerQueue50(String msg) throws InterruptedException {
		log.info("SpringListener50: " + msg + "  " + LocalDateTime.now());
		// 只要监听到消息，就会自动确认
		Thread.sleep(200);
	}
	
	@RabbitListener(queues = "demoQueue.queue")
	public void listener2Queue50(String msg) throws InterruptedException {
		log.info("Spring2Listener50: " + msg + "  " + LocalDateTime.now());
		// 只要监听到消息，就会自动确认
		Thread.sleep(20);
	}
	
	/**
	 * fanout模式，通过绑定的交换机，将消息发送到所有绑定的队列
	 *
	 * @param msg 消息
	 * @throws InterruptedException 中断异常
	 */
	@RabbitListener(queues = "fanout.queue1")
	public void listenerFanoutQueue1(String msg) throws InterruptedException {
		log.info("listenerFanoutQueue1: " + msg + "  " + LocalDateTime.now());
	}
	
	@RabbitListener(queues = "fanout.queue2")
	public void listenerFanoutQueue2(String msg) throws InterruptedException {
		log.info("listenerFanoutQueue2: " + msg + "  " + LocalDateTime.now());
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(name = "fanout.queue3"),
			exchange = @Exchange(name = "fanout.direct", type = ExchangeTypes.DIRECT),
			key = {"demo1", "demo2"}
	))
	// fanout.direct就是一个交换机，只不过这个交换机的类型是direct，所以可以通过key来进行绑定
	// 然后我们通过key来进行发送消息，就可以发送到指定的队列
	
	public void listenerDirectQueue1(String msg) throws InterruptedException {
		log.info("listenerDirectQueue1: " + msg + "  " + LocalDateTime.now());
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(name = "fanout.queue3"),
			exchange = @Exchange(name = "fanout.direct", type = ExchangeTypes.DIRECT),
			key = {"demo1", "demo3"}
	))
	public void listenerDirectQueue2(String msg) throws InterruptedException {
		log.info("listenerDirectQueue2: " + msg + "  " + LocalDateTime.now());
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(name = "topic.queue1"),
			exchange = @Exchange(name = "demoTopic.topic", type = ExchangeTypes.TOPIC),
			key = "china.#"
	))
	public void listenerTopicQueue1(String msg) throws InterruptedException {
		log.info("listenerTopicQueue1: " + msg + "  " + LocalDateTime.now());
	}
	
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(name = "topic.queue2"),
			exchange = @Exchange(name = "demoTopic.topic", type = ExchangeTypes.TOPIC),
			key = "#.news"
	))
	public void listenerTopicQueue2(String msg) throws InterruptedException {
		log.info("listenerTopicQueue2: " + msg + "  " + LocalDateTime.now());
	}
	@RabbitListener(queues = "object.queue")
	public void listenObjectQueue(Map<String, Object> map) {
	 		log.info("listenObjectQueue: " + map);
	}
}
