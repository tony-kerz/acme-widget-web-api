package com.acme.widget.web.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.acme.widget.dao.jpa.WidgetDao;
import com.acme.widget.domain.Widget;
import com.kerz.beans.PropertyCopier;

@Controller
public class WidgetController implements InitializingBean
{
	private static final Logger log = LoggerFactory.getLogger(WidgetController.class);
	public static final String WIDGETS = "/widgets";
	private static final String WIDGET_ID = "widgetId";
	public static final String WIDGET = WIDGETS + "/{" + WIDGET_ID + "}";

	@Autowired
	private WidgetDao widgetDao;

	@Autowired
	PropertyCopier propertyCopier;

	@RequestMapping(value = WIDGETS, method = RequestMethod.GET)
	public @ResponseBody
	List<Widget> index()
	{
		List<Widget> widgets = widgetDao.findAll(new Sort("name"));
		log.debug("widgets={}", widgets);
		return widgets;
	}

	@RequestMapping(value = WIDGET, method = RequestMethod.GET)
	public ResponseEntity<Widget> get(@PathVariable Long widgetId)
	{
		Widget widget = widgetDao.findOne(widgetId);
		log.debug("id={}, widget={}", widgetId, widget);
		return new ResponseEntity<Widget>(widget, widget != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
	}

	@RequestMapping(value = WIDGETS, method = RequestMethod.POST)
	public ResponseEntity<Widget> post(@Valid @RequestBody Widget widget) throws URISyntaxException
	{
		log.debug("widget={}", widget);
		widgetDao.save(widget);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI(WIDGETS + '/' + widget.getId()));
		return new ResponseEntity<Widget>(widget, headers, HttpStatus.OK);
	}

	// @Transactional
	@RequestMapping(value = WIDGET, method = RequestMethod.PUT)
	public ResponseEntity<Widget> put(@PathVariable Long widgetId, @Valid @RequestBody Widget widget) throws URISyntaxException
	{
		log.debug("id={}, widget={}", widgetId, widget);

		Widget putWidget = widgetDao.findOne(widgetId);
		Assert.notNull(putWidget, "existing resource required");

		propertyCopier.copyProperties(widget, putWidget);

		putWidget = widgetDao.save(putWidget);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI(WIDGETS + '/' + widgetId));
		return new ResponseEntity<Widget>(putWidget, headers, HttpStatus.OK);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Assert.notNull(widgetDao, "widget-dao required");
		Assert.notNull(propertyCopier, "property-copier required");
	}
}
