package wo1261931780.stspringCloud2;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.pojo.HotelDoc;
import wo1261931780.stspringCloud2.service.IHotelService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
		restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 搜索文档
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testMatchAll() throws IOException {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		searchRequest.source().query(QueryBuilders.matchAllQuery());// 这里本质上就是在写dsl
		// 3.发送请求
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		// 这里会将我们es返回的结果，直接封装为一个对象
		// 而这个对象，就是searchResponse
		log.info("{}", searchResponse);
		// 这里得到的是一个大的json字符串
		// 所以我们是需要进行逐层解析的
		SearchHits hits = searchResponse.getHits();
		long value = hits.getTotalHits().value;// 总数据一共多少条
		// 所有的数据结果都在hits中间
		log.info("{}", value);
		// 根据结构来逐层解析出东西
		SearchHit[] searchHits = hits.getHits(); // 得到所有的结果
		for (SearchHit hit : searchHits) {
			String asString = hit.getSourceAsString();  // 得到每条数据的json字符串
			HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);// 反序列化
			log.info("{}", hotelDoc);
		}
	}

	/**
	 * 搜索文档，匹配字段
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testMatchAll2() throws IOException {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		// searchRequest.source().query(QueryBuilders.matchQuery("hotel", "华住会"));// 一个是字段名，一个是查询条件
		searchRequest.source().query(QueryBuilders.matchQuery("all", "华住会"));// 一个是字段名，一个是查询条件
		// 3.发送请求
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		log.info("{}", searchResponse);
		// 这里得到的是一个大的json字符串
		// 所以我们是需要进行逐层解析的
		SearchHits hits = searchResponse.getHits();
		long value = hits.getTotalHits().value;// 总数据量
		log.info("{}", value);
		// 根据结构来逐层解析出东西
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			String asString = hit.getSourceAsString();
			HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
			log.info("{}", hotelDoc);
		}
	}

	/**
	 * 将展示结果的方法抽取出来
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testMatchAll3() throws IOException {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		// searchRequest.source().query(QueryBuilders.matchQuery("hotel", "华住会"));// 一个是字段名，一个是查询条件
		searchRequest.source().query(QueryBuilders.matchQuery("all", "华住会"));// 一个是字段名，一个是查询条件
		// 3.发送请求
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		log.info("{}", searchResponse);
		// 这里得到的是一个大的json字符串
		// 所以我们是需要进行逐层解析的
		SearchHits hits = searchResponse.getHits();
		long value = hits.getTotalHits().value;// 总数据量
		log.info("{}", value);
		// 根据结构来逐层解析出东西
		handleResponse(hits);
	}

	/**
	 * 处理搜索结果的方法
	 * 将方法抽取以后，代码更加简洁
	 *
	 * @param hits 搜索结果
	 */
	private static void handleResponse(SearchHits hits) {
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			String asString = hit.getSourceAsString();
			HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
			log.info("{}", hotelDoc);
		}
	}

	/**
	 * 组合查询
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testBoolean() throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel"); // 索引库名
		// 这里是组合查询，先匹配city，再过滤price
		// 这里的boolQuery是布尔查询，可以组合多个查询条件
		// must表示必须匹配，filter表示过滤，可以有多个filter
		// 这里的termQuery是精确匹配，这里的city是字段名，厦门是查询条件

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery(); // 创建布尔查询对象
		boolQueryBuilder.must(QueryBuilders.termQuery("city", "厦门")); // 必须匹配
		boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(200)); // 过滤，只要价格小于等于200
		// 这是我们手动写查询的方式，实际很麻烦，每次查询都要一个独立的dsl语句
		// 是否存在自动配置/生成/判断的方式来完成？
		searchRequest.source().query(boolQueryBuilder); // 设置查询条件
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		// ……
		// 后面就和上面是一样的操作，直接提取出一个方法就可以
		// ……
	}

	/**
	 * 分页和排序
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testPageAndSort() throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel"); // 索引库名
		searchRequest.source().query(QueryBuilders.matchAllQuery()); // 匹配所有

		searchRequest.source().sort("price", SortOrder.ASC); // 排序，按照价格升序
		// searchRequest.source().from((page-1)*size).size(5);// 前后端联动的结果
		searchRequest.source().from(0).size(5);// 设置页码和分页

		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		// ……
		// 后面就和上面是一样的操作，直接提取出一个方法就可以
		// ……
	}

	/**
	 * 高亮显示
	 */
	@Test
	void testHighLight() throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel");
		searchRequest.source().query(QueryBuilders.matchQuery("all", "华住会"));
		searchRequest.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
		// es的特点之一，全部支持链式编程
		// 其实有个问题，我们的高亮字段不是在原始位置自动替换出来的
		// ，要手动从hits中获取
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits(); // 这里我们得到了所有数据的数组，但是需要对数据完成一次加工操作
		// if (CollectionUtils.isEmpty(searchHits)) {
		// 	log.info("没有数据");
		// 	return;
		// }
		for (SearchHit hit : searchHits) {
			String sourceAsString = hit.getSourceAsString();// 得到原始数据
			HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);// 使用指定的对象对结果进行序列化操作，变成我们使用的对象
			log.info("我是单个对象：{}", hotelDoc);// 打印原始数据
			// 这里会发现我们的结果没有进行高亮标签展示，所以有了下面这一步：
			Map<String, HighlightField> highlightFields = hit.getHighlightFields();
			if (CollectionUtils.isEmpty(highlightFields)) {
				log.info("没有高亮展示");
				continue;
			}
			HighlightField name = highlightFields.get("name");
			hotelDoc.setName(String.valueOf(name));
			log.info("我是高亮展示的对象：{}", hotelDoc);// 打印高亮以后的数据
		}
	}


	// @Test
	// void testAggregation() throws IOException {
	// 	// new SearchRequest("hotel").source().aggregation(AggregationBuilders.terms("city").field("city"));
	// 	// 其实上面是可以链式编程，
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	// 	searchRequest.source().size(0);// 不需要返回数据，只需要聚合结果
	// 	searchRequest.source().aggregation(AggregationBuilders
	// 			.terms("city")// 聚合名称
	// 			.field("city")// 聚合字段
	// 			.size(10)
	// 	);
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// 	Aggregations aggregations = searchResponse.getAggregations();// 聚合结果
	// 	Terms city = aggregations.get("city");// 根据聚合名称获取聚合结果
	// 	List<? extends Terms.Bucket> cityBuckets = city.getBuckets();
	// 	for (Bucket cityBucket : cityBuckets) {
	// 		String keyAsString = cityBucket.getKeyAsString();
	// 		log.info(keyAsString + " : " + cityBucket.getDocCount());// 最终结果
	// 	}
	// 	// city.getBuckets().forEach(bucket -> {
	// 	// 	log.info(bucket.getKeyAsString() + " : " + bucket.getDocCount());
	// 	// });
	// }

	// @Test
	// void testSuggestion() throws IOException {
	// 	SearchRequest searchRequest = new SearchRequest("hotel");
	// 	searchRequest.source()
	// 			.suggest(new SuggestBuilder()
	// 					.addSuggestion("suggestions",
	// 							SuggestBuilders.completionSuggestion("suggestion")
	// 									.prefix("华住会")
	// 									.skipDuplicates(true)
	// 									.size(10)
	// 					));
	// 	SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
	// 	Suggest searchResponseSuggest = searchResponse.getSuggest();// 获取建议结果
	// 	CompletionSuggestion suggestions = searchResponseSuggest.getSuggestion("suggestions");  // 根据建议名称获取建议结果
	// 	List<? extends Option> options = suggestions.getOptions();// 获取建议选项
	// 	for (Option option : options) {// 遍历建议选项
	// 		log.info(option.getText().toString());// 获取建议选项的文本，这里是我们想要获取的对象
	// 	}
	// }
	@Test
	void testGetDocumentById() throws IOException {
		// 1.准备Request      // GET /hotel/_doc/{id}
		GetRequest request = new GetRequest("hotel", "61083");
		// 2.发送请求
		GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
		// 3.解析响应结果
		String json = response.getSourceAsString();

		HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
		System.out.println("hotelDoc = " + hotelDoc);
	}

	@Test
	void testDeleteDocumentById() throws IOException {
		// 1.准备Request      // DELETE /hotel/_doc/{id}
		DeleteRequest request = new DeleteRequest("hotel", "61083");
		// 2.发送请求
		restHighLevelClient.delete(request, RequestOptions.DEFAULT);
	}

	@Test
	void testUpdateById() throws IOException {
		// 1.准备Request
		UpdateRequest request = new UpdateRequest("hotel", "61083");
		// 2.准备参数
		request.doc(
				"price", "870"
		);
		// 3.发送请求
		restHighLevelClient.update(request, RequestOptions.DEFAULT);
	}


}
