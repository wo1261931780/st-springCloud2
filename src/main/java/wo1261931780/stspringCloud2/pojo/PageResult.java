package wo1261931780.stspringCloud2.pojo;

import lombok.Data;

import java.util.List;

/**
 * Created by Intellij IDEA.
 * Project:st-springCloud2
 * Package:wo1261931780.stspringCloud2.pojo
 *
 * @author liujiajun_junw
 * @Date 2024-04-17-29  星期三
 * @Description
 */
@Data
public class PageResult {
	private long total;
	private List<HotelDoc> hotels;

	public PageResult() {
	}

	public PageResult(long total, List<HotelDoc> hotels) {
		this.total = total;
		this.hotels = hotels;
	}
}
