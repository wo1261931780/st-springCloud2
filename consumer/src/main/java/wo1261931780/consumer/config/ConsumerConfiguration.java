package wo1261931780.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
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
	@Bean
	public FanoutExchange fanoutExchange() {
		return new FanoutExchange("demoExchange.fanout");
	}
	@Bean
	public Queue queue1() {
		return new Queue("fanout.queue1");
	}
	@Bean
	public Queue queue2() {
		return new Queue("fanout.queue2");
	}
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

	
}
