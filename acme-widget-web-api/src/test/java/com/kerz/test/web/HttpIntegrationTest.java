package com.kerz.test.web;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.acme.widget.domain.Widget;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class HttpIntegrationTest
{
	@Autowired
	@Qualifier("widgetPath")
	private String widgetPath;

	@Test
	public void test() throws IOException
	{
		// rest assured: given/expect/when
		//

		Widget w = new Widget();
		w.setName("widget-" + System.currentTimeMillis());

		// post (create)
		given().log().all().contentType("application/json").body(w).expect().log().all().statusCode(HttpStatus.OK.value()).when()
				.post(widgetPath);

		// get all
		// given().log().all().expect().log().all().statusCode(HttpStatus.OK.value()).and().body("",
		// hasSize(1)).when().get(widgetPath);
		// String json = get(widgetPath).asString();
		// List<Widget>

		// get all
		Widget[] widgetArray = get(widgetPath).as(Widget[].class);

		System.out.println("widget-array: " + widgetArray);
		List<Widget> widgets = Arrays.asList(widgetArray);
		for (Widget widget : widgets)
		{
			if (widget.getName().equals(w.getName()))
			{
				w = widget;
				break;
			}
		}

		System.out.println("widget-2: " + w);
		// String json = get(widgetPath).asString();
		// List<Map<String, Object>> result =
		// with(json).get("findAll { it.name == '" + w.getName() + "'}");

		// assertTrue(widgetArray.length == 1);
		// Widget w2 = widgetArray[0];

		// assertTrue(widgets.size() == 1);
		// List<Map<String, Object>> widgets = with(json).get("");
		// System.out.println("widgets: " + widgets);

		// get one
		// given().log().all().expect().log().all().statusCode(HttpStatus.OK.value()).and().body("",
		// hasSize(1)).when().get(widgetPath + "/" + );

		w.setName(w.getName() + "-2");

		// put (update)
		given().log().all().contentType("application/json").body(w).expect().log().all().statusCode(HttpStatus.OK.value()).when()
				.put(widgetPath + "/" + w.getId());

		// get single
		given().log().all().expect().log().all().statusCode(HttpStatus.OK.value()).body("name", equalTo(w.getName())).when()
				.get(widgetPath + "/" + w.getId());

		// delete
		given().log().all().expect().log().all().statusCode(HttpStatus.OK.value()).when().delete(widgetPath + "/" + w.getId());
	}
}
