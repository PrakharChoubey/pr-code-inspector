package com.aicodereview;

import com.aicodereview.model.CodeAnalysisResult;
import com.prcodeinspector.service.OpenAIClientService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class BasicIntegrationTest {

	@Autowired
	private OpenAIClientService openAIClientService;

	@Test
	public void testCodeAnalysis() {
		// Test code with known issues
		String testCode = "public class TestClass {\n" + "    public void processData() {\n"
				+ "        String sql = \"SELECT * FROM users WHERE id = \" + userId;\n"
				+ "        Statement stmt = connection.createStatement();\n"
				+ "        ResultSet rs = stmt.executeQuery(sql);\n" + "    }\n" + "}";

	}
}