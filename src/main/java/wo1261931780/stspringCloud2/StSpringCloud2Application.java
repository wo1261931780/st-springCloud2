package wo1261931780.stspringCloud2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("wo1261931780.stspringCloud2.mapper")
public class StSpringCloud2Application {

	public static void main(String[] args) {
		SpringApplication.run(StSpringCloud2Application.class, args);
	}

}
