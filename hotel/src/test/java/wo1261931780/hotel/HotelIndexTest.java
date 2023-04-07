package wo1261931780.hotel;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static wo1261931780.hotel.constants.HotelConstants.MAPPING_TEMPLATE;


/**
 * Created by Intellij IDEA.
 * Project:hotel-demo
 * Package:cn.itcast.hotel
 *
 * @author liujiajun_junw
 * @Date 2023-04-21-06  星期四
 * @description
 */
@SpringBootTest
@Slf4j
public class HotelIndexTest {
	private RestHighLevelClient restHighLevelClient;
	
	@BeforeEach
	void setUp() {
		// 因为是成员变量，所以在这里初始化
		restHighLevelClient = new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("localhost", 9200, "http")
						// 如果是集群，就是多个HttpHost
						// ,new HttpHost("localhost", 9201, "http")
				));
	}
	
	@Test
	void testInitIndex() {
		log.info("初始化索引");
		log.info("{}", restHighLevelClient);
	}
	@Test
	public void testCreateIndex() throws IOException {
		// 创建索引
		CreateIndexRequest request = new CreateIndexRequest("hotel");
		// 创建索引请求，将dsl语句放入请求
		request.source(MAPPING_TEMPLATE, XContentType.JSON);
		// 客户端执行请求 IndicesClient,请求后获得响应
		restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
		// 响应的状态
	}
	
	@Test
	public void testDeleteIndex() throws IOException {
		DeleteIndexRequest indexRequest = new DeleteIndexRequest("hotel");
		// 直接发送请求即可
		// indexRequest.source(MAPPING_TEMPLATE, XContentType.JSON);
		// 删除索引的请求
		restHighLevelClient.indices().delete(indexRequest, RequestOptions.DEFAULT);
	}
	@Test
	public void testExistIndex() throws IOException {
		// 判断索引是否存在
		GetIndexRequest request = new GetIndexRequest("hotel");
		boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
		log.info(String.valueOf(exists));
	}
	
	
}
