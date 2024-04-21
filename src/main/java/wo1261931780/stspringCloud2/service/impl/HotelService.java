package wo1261931780.stspringCloud2.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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
		searchRequest.source().size(0);
		searchRequest.source().aggregation(AggregationBuilders.terms("brandAgg")
				.field("brand.keyword")
				.size(10));
		searchRequest.source().aggregation(AggregationBuilders.terms("cityAgg")
				.field("city.keyword")
				.size(10));
		searchRequest.source().aggregation(AggregationBuilders.terms("starAgg")
				.field("starName.keyword")
				.size(10));
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		// 有结果以后，需要对结果进行解析，才能进入我们的Java项目中
		Map<String, List<String>> resultMap = new HashMap<>();
		Aggregations aggregations = searchResponse.getAggregations(); // 获取聚合结果
		List<String> brandList = getStringList(aggregations, "brandName");
		resultMap.put("brand", brandList); // 品牌聚合结果存入map
		List<String> cityList = getStringList(aggregations, "cityName");
		resultMap.put("city", cityList);
		List<String> starList = getStringList(aggregations, "starName");
		resultMap.put("star", starList);
		return resultMap;
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
