package com.nhnacademy.dashboard;

import com.nhnacademy.dashboard.api.GrafanaApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DashboardApplicationTests {

	@MockitoBean
	GrafanaApi grafanaApi;

	@Test
	void contextLoads() {
	}

}
