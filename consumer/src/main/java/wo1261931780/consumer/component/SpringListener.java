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
	// 正向索引，做局部内容检索的时候效率比较差
	// 先找到文档，然后再找到文档中的内容是否包含关键字，
	// 倒排索引，做全局内容检索的时候效率比较高
	// 基于词条创建索引，然后通过词条找到文档，然后再找到文档中的内容是否包含关键字
	// 倒排索引更适合于基于内容的检索，
	// 正向索引更适合于基于属性的检索
	// 索引在MySQL中叫做库，但是在ElasticSearch中叫映射，mapping
	/*
	 docker run -d \
	 	--name es \
	     -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \ # 设置JVM内存
	     -e "discovery.type=single-node" \ # 单节点
	     -v es-data:/usr/share/elasticsearch/data \ # 挂载数据卷
	     -v es-plugins:/usr/share/elasticsearch/plugins \ # 挂载插件卷
	     --privileged \ # 允许容器内的程序使用特权模式
	     --network es-net \ # 指定网络，es容器加入网络
	     -p 9200:9200 \ # 暴露端口，9200是http端口，9300是tcp端口，tcp给节点互联
	     -p 9300:9300 \ # 暴露端口
	 elasticsearch:8.7.0
	 */
	
	// 我们使用的是ik分词器，需要安装ik分词器作为插件到docker
	// 然后，有分词类型，最小和最细
	// 最小分词，就是将一个词语分成一个一个的词汇，
	// 4个词到3个词到2个，以此类推
	// 如果4个有，那就不再继续分词
	// 程序员=程序员
	// 最细分词，就是将一个词语分成一个一个的词汇，然后再将这些词汇分成一个一个的字
	// 程序员=程序员，程序，员
	// ==============================
	// 其实分词器的底层，应该有字典，然后根据字典来进行文本分词
	// es中没有数组这个概念，只有集合（允许多个值存在）
	// ==============================
	
	
	
	
}
