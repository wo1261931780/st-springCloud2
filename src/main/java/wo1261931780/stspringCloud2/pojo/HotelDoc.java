package wo1261931780.stspringCloud2.pojo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
	private Long id;
	private String name;
	private String address;
	private Integer price;
	private Integer score;
	private String brand;
	private String city;
	private String starName;
	private String business;
	private String pic;
	private Object distance;
	private Boolean isAD;
	private List<String> suggestion;
	// 这里是因为索引中添加了部分字段，那么我们的实体类也需要进行对应的变化
	private String location;
	// 大部分的字段都是一样的，经纬度不一样
	// 这里后面有很多修改，包括对构造方法的修改，以及对location字段的格式的修改。

	public HotelDoc(Hotel hotel) {
		this.id = hotel.getId();
		this.name = hotel.getName();
		this.address = hotel.getAddress();
		this.price = hotel.getPrice();
		this.score = hotel.getScore();
		this.brand = hotel.getBrand();
		this.city = hotel.getCity();
		this.starName = hotel.getStarName();
		this.business = hotel.getBusiness();
		this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
		this.pic = hotel.getPic();
		// 自动补全字段的处理
		// this.suggestion = Arrays.asList(this.brand,this.business);
		// 在没有斜杠的情况下，我们不需要切分，所以这里用的就是Collections.addAll()方法
		this.suggestion = new ArrayList<>();
		// 添加品牌、城市
		this.suggestion.add(this.brand);
		this.suggestion.add(this.city);
		// 判断商圈是否包含/
		if (this.business.contains("/")) {
			// 需要切割
			String[] arr = this.business.split("/");
			Collections.addAll(this.suggestion, arr);
		} else {
			this.suggestion.add(this.business);
		}
		// 这里是为了匹配location字段的格式
		// 其实是可以直接使用BeanUtil来完成这个操作，
		// 主要的目的是将经纬度放到一起。实现数据库数据到索引库数据的转换
	}
}
