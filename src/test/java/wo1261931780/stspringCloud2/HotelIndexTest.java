package wo1261931780.stspringCloud2;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static wo1261931780.stspringCloud2.constants.HotelConstants.MAPPING_TEMPLATE;

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
		// 因为是成员变量，所以一开始在这里初始化
		restHighLevelClient = new RestHighLevelClient(
				RestClient.builder(
						new HttpHost("localhost", 9200, "http")
						// 如果是集群，就是多个HttpHost
						// ,new HttpHost("localhost", 9201, "http")
				));
	}

	@AfterEach
	void tearDown() throws IOException {
		// 关闭客户端,使用完成以后自动销毁资源
		restHighLevelClient.close();
	}

	@Test
	void testInitIndex() {
		log.info("初始化索引");
		// 测试一下初始化能否顺利完成
		log.info("{}", restHighLevelClient);
	}

	/**
	 * 创建索引
	 * @throws IOException 异常
	 */
	@Test
	public void testCreateIndex() throws IOException {
		// 创建索引
		CreateIndexRequest request = new CreateIndexRequest("hotel");
		// 创建索引请求，将dsl语句放入请求
		request.source(MAPPING_TEMPLATE, XContentType.JSON);
		// 客户端执行请求 IndicesClient,请求后获得响应
		restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
		// 所有增删改查的操作请求，实际都是在restHighLevelClient.indices()中间的
		// 所以这里我们操作一次即可
		// 响应的状态
	}

	/**
	 * 删除索引
	 *
	 * @throws IOException 异常
	 */
	@Test
	public void testDeleteIndex() throws IOException {
		DeleteIndexRequest indexRequest = new DeleteIndexRequest("hotel");
		// 直接发送请求即可
		// indexRequest.source(MAPPING_TEMPLATE, XContentType.JSON);
		// 删除索引的请求
		restHighLevelClient.indices().delete(indexRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 判断索引是否存在
	 *
	 * @throws IOException 异常
	 */
	@Test
	public void testExistIndex() throws IOException {
		// 判断索引是否存在
		// GetIndexRequest request = new GetIndexRequest("hotel");
		GetIndexRequest request = new GetIndexRequest();
		// 因为判断是否存在，其实是有返回值的，所以要进行查询
		boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
		log.info(String.valueOf(exists));
	}


}
