package wo1261931780.stspringCloud2;


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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.pojo.HotelDoc;
import wo1261931780.stspringCloud2.service.IHotelService;

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
	// 要想在java中操作dsl
	// 首先要创建对应的索引库，然后在这里完成初始化的过程

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

	@AfterEach
	void tearDown() throws IOException {
		restHighLevelClient.close();
		// 完成后主动销毁
	}
	// 所有的单元测试，都会优先执行初始化，所以这里可以直接使用

	@Autowired
	private IHotelService hotelService;

	/**
	 * 创建索引（实际就是创建一个数据库）
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testAddDocument() throws IOException {
		Hotel byId = hotelService.getById(61083L);// 查询数据库
		HotelDoc hotelDoc = new HotelDoc(byId);// 数据库的实体类对象，转换成索引库文档对象

		// 1. 创建文档对象
		IndexRequest request = new IndexRequest("hotel").id(byId.getId().toString());
		// 准备json文档
		// 需要将原始的对象，序列化为json的格式
		request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);

		// 2. 调用方法，添加文档,indcies是索引操作，这里是index，直接操作文档
		restHighLevelClient.index(request, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}

	/**
	 * 获取文档，实际就是查询
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testGetDocument() throws IOException {
		GetRequest getRequest = new GetRequest("hotel", "61083");// 1. 索引库名，文档id
		// 上面就是查询条件
		// 因为文档中的数据就是Source中的数据，所以需要一步骤来获取
		GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);// 2. 调用方法，获取文档
		log.info("文档内容：{}", documentFields.getSourceAsString());// 3. 获取文档内容
		// 文档中的内容是json格式的
		String sourceAsString = documentFields.getSourceAsString();// 4. 将文档内容转换成实体类对象
		// 因为是json格式的
		HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);// 5. 反序列化
		log.info("反序列化后的对象：{}", hotelDoc);
	}

	/**
	 * 更新文档
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testUpdateDocument() throws IOException {
		// 1. 创建文档对象
		UpdateRequest hotel = new UpdateRequest("hotel", "61083");
		// 相当于这里创建了一个查询语句来获取对象
		// 然后再进行更新
		hotel.doc(
				"name", "北京饭店",
				"starName", "四钻"
				// 这里其实要注意，可变参数，中间是逗号
		);// 准备请求的参数
		// 前面是字段，后面是值
		// 2. 调用方法，添加文档
		restHighLevelClient.update(hotel, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}

	/**
	 * 删除文档
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testDeleteDocument() throws IOException {
		// 1. 创建文档对象
		DeleteRequest hotel = new DeleteRequest("hotel", "61083");
		// 2. 调用方法，添加文档
		restHighLevelClient.delete(hotel, RequestOptions.DEFAULT);
		// 3. 关闭客户端
	}

	/**
	 * 批量新增
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testBulkRequest() throws IOException {
		List<Hotel> hotelList = hotelService.list();// 获得所有的酒店数据
		BulkRequest bulkRequest = new BulkRequest();// 1. 创建批量请求对象
		// 在括号当中，可以批量新增/修改/删除
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
	// @Test
	// void testMatchAll() throws IOException {
	// 	// 1.准备request
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	//
	// 	// 2.使用dsl
	// 	searchRequest.source().query(QueryBuilders.matchAllQuery());// 这里本质上就是在写dsl
	// 	// 3.发送请求
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	//
	// 	log.info(searchResponse + "");
	// 	// 这里得到的是一个大的json字符串
	// 	// 所以我们是需要进行逐层解析的
	// 	SearchHits hits = searchResponse.getHits();
	// 	long value = hits.getTotalHits().value;// 总数据量
	// 	log.info(value + "");
	// 	// 根据结构来逐层解析出东西
	// 	SearchHit[] searchHits = hits.getHits();
	// 	for (SearchHit hit : searchHits) {
	// 		String asString = hit.getSourceAsString();
	// 		HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
	// 		log.info(hotelDoc + "");
	// 	}
	// }
	//
	// @Test
	// void testMatchAll2() throws IOException {
	// 	// 1.准备request
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	//
	// 	// 2.使用dsl
	// 	searchRequest.source().query(QueryBuilders.matchQuery("hotel", "华住会"));// 一个是字段名，一个是查询条件
	// 	// 3.发送请求
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	//
	// 	log.info(searchResponse + "");
	// 	// 这里得到的是一个大的json字符串
	// 	// 所以我们是需要进行逐层解析的
	// 	SearchHits hits = searchResponse.getHits();
	// 	long value = hits.getTotalHits().value;// 总数据量
	// 	log.info(value + "");
	// 	// 根据结构来逐层解析出东西
	// 	SearchHit[] searchHits = hits.getHits();
	// 	for (SearchHit hit : searchHits) {
	// 		String asString = hit.getSourceAsString();
	// 		HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
	// 		log.info(hotelDoc + "");
	// 	}
	// }
	//
	//
	// @Test
	// void testBoolean() throws IOException {
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	//
	// 	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
	// 	boolQueryBuilder.must(QueryBuilders.termQuery("city", "厦门"));
	// 	boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(200));
	//
	// 	searchRequest.source().query(boolQueryBuilder);
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// 	// ……
	// 	// 后面就和上面是一样的操作，直接提取出一个方法就可以
	// 	// ……
	// }
	//
	// @Test
	// void testPageAndSort() throws IOException {
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	// 	searchRequest.source().query(QueryBuilders.matchAllQuery());
	//
	// 	searchRequest.source().sort("price", SortOrder.ASC);
	// 	// searchRequest.source().from((page-1)*size).size(5);// 前后端联动的结果
	// 	searchRequest.source().from(0).size(5);// 设置页码和分页
	//
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// 	// ……
	// 	// 后面就和上面是一样的操作，直接提取出一个方法就可以
	// 	// ……
	// }
	//
	// @Test
	// void testHighLight() {
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	// 	searchRequest.source().query(QueryBuilders.matchQuery("all", "华住会"));
	// 	searchRequest.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
	// }
	//
	//
	// // @Test
	// // void testAggregation() throws IOException {
	// // 	// new SearchRequest("hotel").source().aggregation(AggregationBuilders.terms("city").field("city"));
	// // 	// 其实上面是可以链式编程，
	// // 	SearchRequest searchRequest = new SearchRequest("hotel");
	// // 	searchRequest.source().size(0);// 不需要返回数据，只需要聚合结果
	// // 	searchRequest.source().aggregation(AggregationBuilders
	// // 			.terms("city")// 聚合名称
	// // 			.field("city")// 聚合字段
	// // 			.size(10)
	// // 	);
	// // 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// // 	Aggregations aggregations = searchResponse.getAggregations();// 聚合结果
	// // 	Terms city = aggregations.get("city");// 根据聚合名称获取聚合结果
	// // 	List<? extends Terms.Bucket> cityBuckets = city.getBuckets();
	// // 	for (Bucket cityBucket : cityBuckets) {
	// // 		String keyAsString = cityBucket.getKeyAsString();
	// // 		log.info(keyAsString + " : " + cityBucket.getDocCount());// 最终结果
	// // 	}
	// // 	// city.getBuckets().forEach(bucket -> {
	// // 	// 	log.info(bucket.getKeyAsString() + " : " + bucket.getDocCount());
	// // 	// });
	// // }
	//
	// // @Test
	// // void testSuggestion() throws IOException {
	// // 	SearchRequest searchRequest = new SearchRequest("hotel");
	// // 	searchRequest.source()
	// // 			.suggest(new SuggestBuilder()
	// // 					.addSuggestion("suggestions",
	// // 							SuggestBuilders.completionSuggestion("suggestion")
	// // 									.prefix("华住会")
	// // 									.skipDuplicates(true)
	// // 									.size(10)
	// // 					));
	// // 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// // 	Suggest searchResponseSuggest = searchResponse.getSuggest();// 获取建议结果
	// // 	CompletionSuggestion suggestions = searchResponseSuggest.getSuggestion("suggestions");  // 根据建议名称获取建议结果
	// // 	List<? extends Option> options = suggestions.getOptions();// 获取建议选项
	// // 	for (Option option : options) {// 遍历建议选项
	// // 		log.info(option.getText().toString());// 获取建议选项的文本，这里是我们想要获取的对象
	// // 	}
	// // }
	//
	//

}
