package wo1261931780.stspringCloud2;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author junw
 */
@SpringBootApplication
@MapperScan("wo1261931780.stspringCloud2.mapper")
public class StSpringCloud2Application {

	public static void main(String[] args) {
		SpringApplication.run(StSpringCloud2Application.class, args);
	}

	/**
	 * 手动注入一个es客户端，方便我们调取服务
	 * @return es客户端
	 */
	@Bean
	public RestHighLevelClient restHighLevelClient() {
		return new RestHighLevelClient(RestClient.builder(
				// HttpHost.create("http://0.0.0.0:9200")
				// HttpHost.create("http://172.18.0.2:9200")
				HttpHost.create("http://127.0.0.1:9200")
				// 这里使用了gpt帮我进行日志分析，上面第一个不是ip地址，所以错误
				// 第二个，因为是docker中的发布地址和通信地址，也不对
				// 一开始使用localhost，后来改成了127.0.0.1，因为我本地是可以访问的，所以就用了这个地址
		));
	}
}
