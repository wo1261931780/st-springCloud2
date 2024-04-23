package wo1261931780.stspringCloud2.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wo1261931780.stspringCloud2.mapper.HotelMapper;
import wo1261931780.stspringCloud2.pojo.Hotel;
import wo1261931780.stspringCloud2.pojo.HotelDoc;
import wo1261931780.stspringCloud2.pojo.PageResult;
import wo1261931780.stspringCloud2.pojo.RequestParams;
import wo1261931780.stspringCloud2.service.IHotelService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author junw
 */
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * 根据条件搜索酒店信息
	 * 这里其实先放到抽象类，然后重写了一次。我觉得没必要
	 *
	 * @param requestParams 条件参数
	 * @return PageResult<Hotel>
	 */
	@Override
	public PageResult searchHotel(RequestParams requestParams) throws IOException {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		// 需要结合前端
		String requestParamsKey = requestParams.getKey();
		// 健壮性分析
		if (requestParamsKey.isEmpty()) {
			searchRequest.source().query(QueryBuilders.matchAllQuery());
		} else {
			searchRequest.source().query(QueryBuilders.matchQuery("all", requestParamsKey));
		}
		Integer size = requestParams.getSize();
		searchRequest.source().from((requestParams.getPage() - 1) * size).size(size);// 分页

		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		log.debug(String.valueOf(searchResponse));
		// 这里得到的是一个大的json字符串
		// 所以我们是需要进行逐层解析的
		SearchHits hits = searchResponse.getHits();
		long value = hits.getTotalHits().value;
		// 总数据量
		log.debug("总数据量:" + value);
		// 根据结构来逐层解析出东西
		return handleResponse(hits);
		// 2024年4月17日18:15:10，这里碰到个es没在容器中启动的问题，回家等修好了再试试项目
	}

	@Override
	public PageResult searchBooleanHotel(RequestParams requestParams) {
		SearchRequest searchRequest = new SearchRequest("hotel");
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		String requestParamsKey = requestParams.getKey();
		if (requestParamsKey.isEmpty()) {
			boolQueryBuilder.must(QueryBuilders.matchAllQuery());
		} else {
			boolQueryBuilder.must(QueryBuilders.matchQuery("all", requestParamsKey));
		}
		// 然后分别根据城市，品牌，星级来判断一遍
		if (!requestParams.getCity().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
		}
		if (!requestParams.getBrand().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
		}

		if (requestParams.getStarName().isEmpty()) {
			boolQueryBuilder.filter(QueryBuilders.termQuery("star", requestParams.getStarName()));
		}

		if (requestParams.getMinPrice() > 0) {
			boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(requestParams.getMinPrice()).gte(requestParams.getMaxPrice()));
		}

		return null;
	}

	@Override
	public PageResult searchSortedHotel(RequestParams requestParams) {
		// 1.准备request
		SearchRequest searchRequest = new SearchRequest("hotel");

		// 2.使用dsl
		// 需要结合前端
		String requestParamsKey = requestParams.getKey();
		// 健壮性分析
		if (requestParamsKey.isEmpty()) {
			searchRequest.source().query(QueryBuilders.matchAllQuery());
		} else {
			searchRequest.source().query(QueryBuilders.matchQuery("all", requestParamsKey));
		}
		Integer size = requestParams.getSize();
		searchRequest.source().from((requestParams.getPage() - 1) * size).size(size);// 分页
		// 现在开始使用sorted的方式来完成查询，其他部分都是类似的
		String location = requestParams.getLocation();
		if (location != null && !location.isEmpty()) {
			// searchRequest.source().sort("location", requestParams.getLocation());
			searchRequest.source().sort(SortBuilders
					.geoDistanceSort("localtion", new GeoPoint(location)) // 按照距离排序
					.order(SortOrder.ASC)// 升序排序
					.unit(DistanceUnit.KILOMETERS));// 单位是公里
		}
		// 我们要想看到距离的数据，还需要到结果中类似hits的形式去拿到sort的结果，然后再进行解析
		return null;
	}

	/**
	 * 这里面就是发请求，根据请求解析结果
	 *
	 * @return Map<String, List < String>>
	 */
	@Override
	public Map<String, List<String>> filters(RequestParams requestParams) throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel"); // 索引名称
		// 这里因为要进行一个过滤，所以这部分代码是新增进来的
		// buidBasicQuery方法是新增的，用来构造查询条件，todo 导入课程代码
		buildBasicQuery(requestParams, searchRequest);
		searchRequest.source().size(0);
		// 2.3.聚合
		buildAggregations(searchRequest);
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		// 有结果以后，需要对结果进行解析，才能进入我们的Java项目中
		Map<String, List<String>> resultMap = new HashMap<>(3);
		Aggregations aggregations = searchResponse.getAggregations(); // 获取聚合结果
		List<String> brandList = getStringList(aggregations, "brandName");
		resultMap.put("brand", brandList); // 品牌聚合结果存入map
		List<String> cityList = getStringList(aggregations, "cityName");
		resultMap.put("city", cityList);
		List<String> starList = getStringList(aggregations, "starName");
		resultMap.put("star", starList);
		return resultMap;
	}

	@Override
	public List<String> getSuggestion(String key) throws IOException {
		SearchRequest searchRequest = new SearchRequest("hotel");
		searchRequest.source().suggest(
				new SuggestBuilder()
						.addSuggestion("suggestions",
								SuggestBuilders.completionSuggestion("suggestion")
										.prefix(key)
										.skipDuplicates(true)
										.size(10))
		);
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		log.debug(searchResponse.toString());
		Suggest suggest = searchResponse.getSuggest();
		// Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>>
		// 泛型很长，实际上就是底下这个
		CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
		List<CompletionSuggestion.Entry.Option> options = suggestions.getOptions();
		List<String> stringList = new ArrayList<>(options.size());
		// 这里的空间大小是很有讲究的
		for (CompletionSuggestion.Entry.Option option : options) {
			log.warn(option.toString());
			log.warn(option.getText().toString());
			stringList.add(option.getText().toString());
		}
		log.error(stringList.toString());
		return stringList;
	}

	@Override
	public void deleteById(Long hotelId) {

	}

	@Override
	public void saveById(Long hotelId) {

	}

	private void buildBasicQuery(RequestParams params, SearchRequest request) {
		// 1.准备Boolean查询
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// 1.1.关键字搜索，match查询，放到must中
		String key = params.getKey();
		if (key.isEmpty()) {
			// 不为空，根据关键字查询
			boolQuery.must(QueryBuilders.matchQuery("all", key));
		} else {
			// 为空，查询所有
			boolQuery.must(QueryBuilders.matchAllQuery());
		}

		// 1.2.品牌
		String brand = params.getBrand();
		if (brand.isEmpty()) {
			boolQuery.filter(QueryBuilders.termQuery("brand", brand));
		}
		// 1.3.城市
		String city = params.getCity();
		if (StringUtils.isNotBlank(city)) {
			boolQuery.filter(QueryBuilders.termQuery("city", city));
		}
		// 1.4.星级
		String starName = params.getStarName();
		if (StringUtils.isNotBlank(starName)) {
			boolQuery.filter(QueryBuilders.termQuery("starName", starName));
		}
		// 1.5.价格范围
		Integer minPrice = params.getMinPrice();
		Integer maxPrice = params.getMaxPrice();
		if (minPrice != null && maxPrice != null) {
			maxPrice = maxPrice == 0 ? Integer.MAX_VALUE : maxPrice;
			boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
		}

		// 2.算分函数查询
		FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
				boolQuery, // 原始查询，boolQuery
				new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{ // function数组
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(
								QueryBuilders.termQuery("isAD", true), // 过滤条件
								ScoreFunctionBuilders.weightFactorFunction(10) // 算分函数
						)
				}
		);

		// 3.设置查询条件
		request.source().query(functionScoreQuery);
	}

	private void buildAggregations(SearchRequest request) {
		request.source().aggregation(
				AggregationBuilders.terms("brandAgg").field("brand").size(100));
		request.source().aggregation(
				AggregationBuilders.terms("cityAgg").field("city").size(100));
		request.source().aggregation(
				AggregationBuilders.terms("starAgg").field("starName").size(100));
	}

	/**
	 * 获取聚合结果的方法
	 *
	 * @param aggregations 聚合结果
	 * @param todoName     聚合结果的名称
	 * @return List<String>
	 */
	private static List<String> getStringList(Aggregations aggregations, String todoName) {
		Terms brandTerms = aggregations.get("brandAgg"); // 获取品牌聚合结果
		List<? extends Terms.Bucket> buckets = brandTerms.getBuckets(); // 获取品牌聚合结果的桶
		List<String> brandList = new ArrayList<>();
		buckets.forEach(bucket -> { // 遍历桶，打印品牌和数量
			String key = bucket.getKeyAsString();
			brandList.add(key);
		});
		return brandList;
	}


	/**
	 * 处理搜索结果的方法
	 * 将方法抽取以后，代码更加简洁
	 *
	 * @param hits 搜索结果
	 */
	private PageResult handleResponse(SearchHits hits) {
		// 没有按照课堂代码来写：
		//         SearchHits searchHits = response.getHits();
		//         // 4.1.总条数
		//         long total = searchHits.getTotalHits().value;
		//         // 4.2.获取文档数组
		//         SearchHit[] hits = searchHits.getHits();
		//         // 4.3.遍历
		//         List<HotelDoc> hotels = new ArrayList<>(hits.length);
		//         for (SearchHit hit : hits) {
		//             // 4.4.获取source
		//             String json = hit.getSourceAsString();
		//             // 4.5.反序列化，非高亮的
		//             HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
		//             // 4.6.处理高亮结果
		//             // 1)获取高亮map
		//             Map<String, HighlightField> map = hit.getHighlightFields();
		//             if (map != null && !map.isEmpty()) {
		//                 // 2）根据字段名，获取高亮结果
		//                 HighlightField highlightField = map.get("name");
		//                 if (highlightField != null) {
		//                     // 3）获取高亮结果字符串数组中的第1个元素
		//                     String hName = highlightField.getFragments()[0].toString();
		//                     // 4）把高亮结果放到HotelDoc中
		//                     hotelDoc.setName(hName);
		//                 }
		//             }
		//             // 4.8.排序信息
		//             Object[] sortValues = hit.getSortValues();
		//             if (sortValues.length > 0) {
		//                 hotelDoc.setDistance(sortValues[0]);
		//             }
		//
		//             // 4.9.放入集合
		//             hotels.add(hotelDoc);
		//         }
		//         return new PageResult(total, hotels);
		// 为了符合前后端交互的逻辑，这里使用统一的返回格式包装一遍
		PageResult pageResult = new PageResult();
		List<HotelDoc> hotels = pageResult.getHotels();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit : searchHits) {
			String asString = hit.getSourceAsString();
			HotelDoc hotelDoc = JSON.parseObject(asString, HotelDoc.class);
			log.debug("结果为：" + hotelDoc);
			hotels.add(hotelDoc);
		}
		pageResult.setTotal(hotels.size());
		return pageResult;
	}
}
