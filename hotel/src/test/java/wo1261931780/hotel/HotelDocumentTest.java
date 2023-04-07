package wo1261931780.hotel;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wo1261931780.hotel.pojo.Hotel;
import wo1261931780.hotel.pojo.HotelDoc;
import wo1261931780.hotel.service.IHotelService;

import java.io.IOException;
import java.util.List;

/**
 * Created by Intellij IDEA.
 * Project:hotel-demo
 * Package:cn.itcast.hotel
 *
 * @author liujiajun_junw
 * @Date 2023-04-09-06  星期五
 * @description
 */
@SpringBootTest
@Slf4j
public class HotelDocumentTest {
	private RestHighLevelClient restHighLevelClient;
	
	@Autowired
	private IHotelService hotelService;
	
	
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
	void testAddDocument() throws IOException {
		Hotel byId = hotelService.getById(61083L);// 查询数据库
		HotelDoc hotelDoc = new HotelDoc(byId);// 数据库的实体类对象，转换成索引库文档对象
		
		// 1. 创建文档对象
		IndexRequest request = new IndexRequest("hotel").id(byId.getId().toString());
		request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
		
		// 2. 调用方法，添加文档
		restHighLevelClient.index(request, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}
	
	@Test
	void testGetDocument() throws IOException {
		GetRequest getRequest = new GetRequest("hotel", "61083");// 1. 索引库名，文档id
		GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);// 2. 调用方法，获取文档
		log.info("文档内容：{}", documentFields.getSourceAsString());// 3. 获取文档内容
		String sourceAsString = documentFields.getSourceAsString();// 4. 将文档内容转换成实体类对象
		HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);// 5. 反序列化
		log.info("反序列化后的对象：{}", hotelDoc);
	}
	
	@Test
	void testUpdateDocument() throws IOException {
		// 1. 创建文档对象
		UpdateRequest hotel = new UpdateRequest("hotel", "61083");
		hotel.doc("name", "北京饭店");// 准备请求的参数
		
		// 2. 调用方法，添加文档
		restHighLevelClient.update(hotel, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}
	
	@Test
	void testDeleteDocument() throws IOException {
		// 1. 创建文档对象
		DeleteRequest hotel = new DeleteRequest("hotel", "61083");
		
		// 2. 调用方法，添加文档
		restHighLevelClient.delete(hotel, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}
	
	@Test
	void testBulkRequest() throws IOException {
		List<Hotel> hotelList = hotelService.list();// 获得所有的酒店数据
		BulkRequest bulkRequest = new BulkRequest();// 1. 创建批量请求对象
		for (Hotel hotel : hotelList) {
			HotelDoc hotelDoc = new HotelDoc(hotel);
			// 2. 准备请求，添加多个新增的Request
			bulkRequest.add(new IndexRequest("hotel")
					                .id(hotel.getId().toString())
					                .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
			// IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
			// request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
			// restHighLevelClient.index(request, RequestOptions.DEFAULT);
		}
		// 2. 准备请求，添加多个新增的Request
		// bulkRequest.add(new IndexRequest("hotel").id("1").source(XContentType.JSON, "name", "北京饭店"));
		// 新增的好几个，就是多个IndexRequest
		// bulkRequest.add(new IndexRequest("hotel").id("2").source(XContentType.JSON, "name", "北京饭店"));
		// bulkRequest.add(new IndexRequest("hotel").id("3").source(XContentType.JSON, "name", "北京饭店"));
		// 因为有了一个转化为文档对象的循环过程
		// 所以，这里就不需要再写多个IndexRequest了
		restHighLevelClient.bulk(null, RequestOptions.DEFAULT);
	}
}
