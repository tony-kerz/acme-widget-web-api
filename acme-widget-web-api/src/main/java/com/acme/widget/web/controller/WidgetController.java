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
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
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
	public ResponseEntity<?> post(@Valid @RequestBody Widget widget, Errors errors) throws URISyntaxException
	// public ResponseEntity<?> post(@Valid @RequestBody Widget widget) throws URISyntaxException
	{
		log.debug("widget={}, errors={}", widget, errors);

		if (errors.hasErrors())
		{
			return new ResponseEntity<Errors>(errors, HttpStatus.BAD_REQUEST);
		}

		// current setup allows post json to set 'version' prop. if this is an
		// issue, can use propertyCopier configured to filter version prop.

		widgetDao.save(widget);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI(WIDGETS + '/' + widget.getId()));
		return new ResponseEntity<Widget>(widget, headers, HttpStatus.OK);
	}

	@RequestMapping(value = WIDGET, method = RequestMethod.PUT)
	public ResponseEntity<?> put(@PathVariable Long widgetId, @Valid @RequestBody Widget widget, Errors errors)
			throws URISyntaxException
	{
		log.debug("id={}, widget={}, errors={}", widgetId, widget, errors);

		if (widget.getVersion() == null)
		{
			errors.rejectValue("version", "field-required");
		}

		if (errors.hasErrors())
		{
			return new ResponseEntity<Errors>(errors, HttpStatus.BAD_REQUEST);
		}

		Widget putWidget = widgetDao.findOne(widgetId);

		HttpStatus status = null;
		if (putWidget == null)
		{
			status = HttpStatus.NOT_FOUND;
		}
		else
		{
			propertyCopier.copyProperties(widget, putWidget);
			putWidget = widgetDao.save(putWidget);
			status = HttpStatus.OK;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI(WIDGETS + '/' + widgetId));
		return new ResponseEntity<Widget>(putWidget, headers, status);
	}

	@RequestMapping(value = WIDGET, method = RequestMethod.DELETE)
	public ResponseEntity<Widget> delete(@PathVariable Long widgetId) throws URISyntaxException
	{
		log.debug("id={}", widgetId);

		Widget widget = widgetDao.findOne(widgetId);

		HttpStatus status = null;
		if (widget == null)
		{
			status = HttpStatus.NOT_FOUND;
		}
		else
		{
			widgetDao.delete(widgetId);
			status = HttpStatus.OK;
		}

		return new ResponseEntity<Widget>(status);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Assert.notNull(widgetDao, "widget-dao required");
		Assert.notNull(propertyCopier, "property-copier required");
	}
}
