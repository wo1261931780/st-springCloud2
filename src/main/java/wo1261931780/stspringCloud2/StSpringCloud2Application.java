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
				HttpHost.create("http://192.168.0.1:9200")
		));
	}
}
