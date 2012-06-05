package com.kerz.mvc.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

public class BindingResultMappingJsonJacksonView extends MappingJacksonJsonView
{
	private Logger log = LoggerFactory.getLogger(BindingResultMappingJsonJacksonView.class);

	private boolean extractValueFromSingleKeyModel = false;

	public void setExtractValueFromSingleKeyModel(boolean extractValueFromSingleKeyModel)
	{
		this.extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
	}

	@Override
	protected Object filterModel(Map<String, Object> model)
	{
		Map<String, Object> result = new HashMap<String, Object>(model.size());
		Set<String> modelKeys = getModelKeys();
		Set<String> renderedAttributes = (!CollectionUtils.isEmpty(modelKeys) ? modelKeys : model.keySet());
		for (Map.Entry<String, Object> entry : model.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			log.debug("key={}, value={}", key, value);
			// if (!(entry.getValue() instanceof BindingResult) &&
			// renderedAttributes.contains(entry.getKey())) {
			if (renderedAttributes.contains(key))
			{
				log.debug("rendered-attributes contains={}", key);
				if (value instanceof BindingResult)
				{
					// by default currently looks like: 'beanPropertyBindingResult'
					key = "binding-result";
				}
				result.put(key, value);
			}
		}
		return (this.extractValueFromSingleKeyModel && result.size() == 1 ? result.values().iterator().next() : result);

	}

}
