package wo1261931780.stspringCloud2.pojo;

import lombok.Data;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.stspringCloud2.pojo
 *
 * @author liujiajun_junw
 * @Date 2024-04-17-23  星期三
 * @Description
 */
@Data
public class RequestParams {
	private String key;
	private Integer page;
	private Integer size;
	private String sortBy;
	private String brand;
	private String city;
	private String starName;
	private Integer minPrice;
	private Integer maxPrice;
	private String location;
}
