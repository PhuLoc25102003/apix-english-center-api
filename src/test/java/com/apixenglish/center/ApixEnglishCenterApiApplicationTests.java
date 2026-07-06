package com.apixenglish.center;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ApixEnglishCenterApiApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, email, status, deleted_at FROM users");
		System.out.println("=== USERS IN DATABASE ===");
		for (Map<String, Object> user : users) {
			System.out.println(user);
		}
		System.out.println("=========================");
	}

}





