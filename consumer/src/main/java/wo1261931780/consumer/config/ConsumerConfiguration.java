package wo1261931780.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.consumer.config
 *
 * @author liujiajun_junw
 * @Date 2023-04-03-33  星期二
 * @description
 */
@Configuration
public class ConsumerConfiguration {
	// 这里我们是通过配置类的方式来声明队列、交换机、绑定关系
	// 声明完毕以后，所有的绑定关系都会自动的在RabbitMQ中创建
	
	
	/**
	 * 声明一个交换机
	 *
	 * @return 交换机
	 */
	@Bean
	public FanoutExchange fanoutExchange() {
		return new FanoutExchange("demoExchange.fanout");
	}
	
	/**
	 * 声明两个队列
	 *
	 * @return 队列
	 */
	@Bean
	public Queue queue1() {
		return new Queue("fanout.queue1");
	}
	
	@Bean
	public Queue queue2() {
		return new Queue("fanout.queue2");
	}
	
	/**
	 * 将两个队列绑定到一个交换机上
	 *
	 * @param queue1         队列1
	 * @param fanoutExchange 交换机
	 * @return 绑定
	 */
	@Bean
	public Binding fanoutBinding1(Queue queue1, FanoutExchange fanoutExchange) {
		// 将两个队列绑定到一个交换机上
		return BindingBuilder.bind(queue1()).to(fanoutExchange());
	}
	
	@Bean
	public Binding fanoutBinding2(Queue queue2, FanoutExchange fanoutExchange) {
		// 将两个队列绑定到一个交换机上
		return BindingBuilder.bind(queue2()).to(fanoutExchange());
	}
	
	/**
	 * 下面是消息转换器
	 * @return 消息转换器
	 */
	@Bean
	public Queue object2Queue() {
		return new Queue("object2Queue.queue");
	}
	
	
}
