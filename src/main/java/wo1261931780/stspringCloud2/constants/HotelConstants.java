package wo1261931780.stspringCloud2.constants;

/**
 * Created by Intellij IDEA.
 * Project:hotel-demo
 * Package:cn.itcast.hotel.constants
 *
 * @author liujiajun_junw
 * date 2023-04-21-29  星期四
 * Description 123
 */
public class HotelConstants {
	// public static final String MAPPING_TEMPLATE = "hotel";
	// 上面这样是错误的，应该是一个完整的dsl语句
	public static final String MAPPING_TEMPLATE = "{\n" +
			"  \"mappings\": {\n" +
			"    \"properties\": {\n" +
			"      \"id\": {\n" +
			"        \"type\": \"keyword\"\n" +
			"      },\n" +
			"      \"name\": {\n" +
			"        \"type\": \"text\",\n" +
			"        \"analyzer\": \"ik_max_word\",\n" +
			"        \"copy_to\": \"all\"\n" +
			"      },\n" +
			"      \"address\": {\n" +
			"        \"type\": \"keyword\",\n" +
			"        \"index\": false\n" +
			"      },\n" +
			"      \"price\": {\n" +
			"        \"type\": \"integer\",\n" +
			"        \"copy_to\": \"all\"\n" +
			"      },\n" +
			"      \"score\": {\n" +
			"        \"type\": \"integer\"\n" +
			"      },\n" +
			"      \"brand\": {\n" +
			"        \"type\": \"keyword\",\n" +
			"        \"copy_to\": \"all\"\n" +
			"      },\n" +
			"      \"city\": {\n" +
			"        \"type\": \"keyword\",\n" +
			"        \"copy_to\": \"all\"\n" +
			"      },\n" +
			"      \"stars\": {\n" +
			"        \"type\": \"keyword\"\n" +
			"      },\n" +
			"      \"localtion\": {\n" +
			"        \"type\": \"geo_point\"\n" +
			"      },\n" +
			"      \"pic\": {\n" +
			"        \"type\": \"keyword\",\n" +
			"        \"index\": false\n" +
			"      },\n" +
			"      \"all\": {\n" +
			"        \"type\": \"text\",\n" +
			"        \"analyzer\": \"ik_max_word\"\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}";
}
