package wo1261931780.stspringCloud2;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wo1261931780.stspringCloud2.service.impl.HotelService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class HotelApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Autowired
	private HotelService hotelService;

	/**
	 * 测试es中的聚合查询
	 *
	 * @throws IOException 异常
	 */
	@Test
	void testAggregation() throws IOException {
		log.info("你好，我测试es中的聚合查询");
		SearchRequest searchRequest = new SearchRequest("hotel"); // 索引名称
		searchRequest.source().size(0); // 设置返回结果数为0，只返回聚合结果
		searchRequest.source().aggregation(AggregationBuilders.terms("brandAgg")
				.field("brand.keyword")
				.size(10)); // 聚合查询，根据品牌进行聚合，返回10个品牌
		// 这里也有个问题，因为需要使用keyword类型才可以进行聚合查询，这里就加了个后缀
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT); // 执行查询
		log.info("聚合查询结果1：{}", searchResponse);
		log.info("聚合查询结果2：{}", searchResponse.getAggregations().asMap());
		// 聚合查询结果：{brandAgg=org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms@3d59933}
		// 有结果以后，需要对结果进行解析，才能进入我们的Java项目中
		Aggregations aggregations = searchResponse.getAggregations(); // 获取聚合结果
		Terms brandTerms = aggregations.get("brandAgg"); // 获取品牌聚合结果
		List<? extends Terms.Bucket> buckets = brandTerms.getBuckets(); // 获取品牌聚合结果的桶
		buckets.forEach(bucket -> { // 遍历桶，打印品牌和数量
			String key = bucket.getKeyAsString();
			long docCount = bucket.getDocCount();
			log.info("品牌：{}，数量：{}", key, docCount);
		});
	}

	@Test
	void testFilter() throws IOException {
		log.info("你好，我测试es中的过滤查询");
		// Map<String, List<String>> filters = hotelService.filters();
		// log.info("过滤条件：{}", filters);
		// 过滤条件：{star=[皇冠假日], city=[皇冠假日], brand=[皇冠假日]}
		// 里面没有数据，所以不行
	}
}
