package com.kerz.mvc.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public class JsonExceptionResolver extends AbstractHandlerExceptionResolver implements InitializingBean
{
	protected static final Logger log = LoggerFactory.getLogger(JsonExceptionResolver.class);

	@Autowired
	MappingJacksonJsonView jsonView;

	/**
	 * Sets the {@linkplain #setOrder(int) order} to {@link #LOWEST_PRECEDENCE}.
	 */
	public JsonExceptionResolver()
	{
		setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
	{
		try
		{
			if (ex instanceof MethodArgumentNotValidException)
			{
				return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex, request, response, handler);
			}
		}
		catch (Exception handlerException)
		{
			logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
		}
		return null;
	}

	/**
	 * Handle the case where an argument annotated with {@code @Valid} such as an
	 * {@link RequestBody} or {@link RequestPart} argument fails validation. An
	 * HTTP 400 error is sent back to the client.
	 * 
	 * @param request
	 *          current HTTP request
	 * @param response
	 *          current HTTP response
	 * @param handler
	 *          the executed handler
	 * @return an empty ModelAndView indicating the exception was handled
	 * @throws IOException
	 *           potentially thrown from response.sendError()
	 */
	protected ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request,
			HttpServletResponse response, Object handler) throws IOException
	{
		BindingResult br = ex.getBindingResult();
		log.debug("encountered manve, br={}, from handler={}", br, handler);
		//response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return new ModelAndView(jsonView).addObject(br);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Assert.notNull(jsonView, "json-view required");
	}
}
