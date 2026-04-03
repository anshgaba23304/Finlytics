package com.example.finlytics.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccessControlIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void viewerCannotListRecords() throws Exception {
		String token = login("viewer", "Viewer123!");
		mockMvc.perform(get("/api/records").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void analystCanListRecords() throws Exception {
		String token = login("analyst", "Analyst123!");
		mockMvc.perform(get("/api/records").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void viewerCanReadDashboardSummary() throws Exception {
		String token = login("viewer", "Viewer123!");
		mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void viewerCannotAccessRecentActivity() throws Exception {
		String token = login("viewer", "Viewer123!");
		mockMvc.perform(get("/api/dashboard/recent").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	private String login(String username, String password) throws Exception {
		String body = """
				{"username":"%s","password":"%s"}
				""".formatted(username, password);
		String json = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		int idx = json.indexOf("\"accessToken\":\"");
		if (idx < 0) {
			throw new IllegalStateException("No token in response: " + json);
		}
		int start = idx + 15;
		int end = json.indexOf('"', start);
		return json.substring(start, end);
	}
}
