package wo1261931780.stspringCloud2;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import wo1261931780.stspringCloud2.pojo.HotelDoc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.stspringCloud2
 *
 * @author liujiajun_junw
 * @Date 2024-04-13-01  星期二
 * @Description
 */
@SpringBootTest
public class HotelSearchTest {


	private static final Logger log = LoggerFactory.getLogger(HotelSearchTest.class);
	private RestHighLevelClient client;
	@Qualifier("restHighLevelClient")
	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Test
	void testMatchAll() throws IOException {
		// 1.准备request
		SearchRequest request = new SearchRequest("hotel");
		// 2.准备请求参数
		request.source().query(QueryBuilders.matchAllQuery());
		// 3.发送请求，得到响应
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);
		// 4.结果解析
		handleResponse(response);
	}

	@Test
	void testMatch() throws IOException {
		// 1.准备request
		SearchRequest request = new SearchRequest("hotel");
		// 2.准备请求参数
		// request.source().query(QueryBuilders.matchQuery("all", "外滩如家"));
		request.source().query(QueryBuilders.multiMatchQuery("外滩如家", "name", "brand", "city"));
		// 3.发送请求，得到响应
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);
		// 4.结果解析
		handleResponse(response);
	}

	@Test
	void testBool() throws IOException {
		// 1.准备request
		SearchRequest request = new SearchRequest("hotel");
		// 2.准备请求参数
       /*
         BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 2.1.must
        boolQuery.must(QueryBuilders.termQuery("city", "杭州"));
        // 2.2.filter
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));
        */

		request.source().query(
				QueryBuilders.boolQuery()
						.must(QueryBuilders.termQuery("city", "杭州"))
						.filter(QueryBuilders.rangeQuery("price").lte(250))
		);
		// 3.发送请求，得到响应
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);
		// 4.结果解析
		handleResponse(response);
	}

	@Test
	void testSortAndPage() throws IOException {
		int page = 2, size = 5;

		// 1.准备request
		SearchRequest request = new SearchRequest("hotel");
		// 2.准备请求参数
		// 2.1.query
		request.source()
				.query(QueryBuilders.matchAllQuery());
		// 2.2.排序sort
		request.source().sort("price", SortOrder.ASC);
		// 2.3.分页 from\size
		request.source().from((page - 1) * size).size(size);

		// 3.发送请求，得到响应
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);
		// 4.结果解析
		handleResponse(response);
	}

	@Test
	void testHighlight() throws IOException {
		// 1.准备request
		SearchRequest request = new SearchRequest("hotel");
		// 2.准备请求参数
		// 2.1.query
		request.source().query(QueryBuilders.matchQuery("all", "外滩如家"));
		// 2.2.高亮
		request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
		// 3.发送请求，得到响应
		SearchResponse response = client.search(request, RequestOptions.DEFAULT);
		// 4.结果解析
		handleResponse(response);
	}

	@Test
	void testSuggest() throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel");
		searchRequest.source().suggest(
				new SuggestBuilder()
						.addSuggestion("suggestions",
								SuggestBuilders.completionSuggestion("suggestion")
										.prefix("s")
										.skipDuplicates(true)
										.size(10))
		);
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		log.info(searchResponse.toString());
		Suggest suggest = searchResponse.getSuggest();
		// Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>>
		// 泛型很长，实际上就是底下这个
		CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
		List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
		for (CompletionSuggestion.Entry.Option option : options) {
			log.info(option.toString());
			// {"took":1,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":0,"relation":"eq"},"max_score":null,"hits":[]},"suggest":{"suggestions":[{"text":"s","offset":0,"length":1,"options":[{"text":"三里屯","_index":"hotel","_type":"_doc","_id":"396189","_score":1.0,"_source":{"address":"三丰北里3号","brand":"皇冠假日","business":"三里屯/工体/东直门地区","city":"北京","id":396189,"location":"39.92129, 116.43847","name":"北京朝阳悠唐皇冠假日酒店","pic":"https://m.tuniucd
			// text:三里屯 score:1.0 context:[]
			// text:上地产业园 score:1.0 context:[]
			// text:上海火车站地区 score:1.0 context:[]
			// text:四川北路商业区 score:1.0 context:[]
			// text:松岗商业中心区 score:1.0 context:[]
			// text:水贝珠宝城 score:1.0 context:[]
			// text:沙头角 score:1.0 context:[]
			// text:蛇口 score:1.0 context:[]
			// text:首都机场 score:1.0 context:[]
		}
	}


	private void handleResponse(SearchResponse response) {
		SearchHits searchHits = response.getHits();
		// 4.1.总条数
		long total = searchHits.getTotalHits().value;
		System.out.println("总条数：" + total);
		// 4.2.获取文档数组
		SearchHit[] hits = searchHits.getHits();
		// 4.3.遍历
		for (SearchHit hit : hits) {
			// 4.4.获取source
			String json = hit.getSourceAsString();
			// 4.5.反序列化，非高亮的
			HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
			// 4.6.处理高亮结果
			// 1)获取高亮map
			Map<String, HighlightField> map = hit.getHighlightFields();
			// 2）根据字段名，获取高亮结果
			HighlightField highlightField = map.get("name");
			// 3）获取高亮结果字符串数组中的第1个元素
			String hName = highlightField.getFragments()[0].toString();
			// 4）把高亮结果放到HotelDoc中
			hotelDoc.setName(hName);
			// 4.7.打印
			System.out.println(hotelDoc);
		}
	}

	@BeforeEach
	void setUp() {
		client = new RestHighLevelClient(RestClient.builder(
				HttpHost.create("http://192.168.150.101:9200")
		));
	}

	@AfterEach
	void tearDown() throws IOException {
		client.close();
	}
}
