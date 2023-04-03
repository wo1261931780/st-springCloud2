package wo1261931780.consumer.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
	@RabbitListener(queues = "fanout.queue1")
	public void listenerFanoutQueue1(String msg) throws InterruptedException {
		log.info("listenerFanoutQueue1: " + msg + "  " + LocalDateTime.now());
	}
	@RabbitListener(queues = "fanout.queue2")
	public void listenerFanoutQueue2(String msg) throws InterruptedException {
		log.info("listenerFanoutQueue2: " + msg + "  " + LocalDateTime.now());
	}
}
