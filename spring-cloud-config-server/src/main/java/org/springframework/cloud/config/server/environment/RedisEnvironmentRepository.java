/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.config.server.environment;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * @author Piotr Mińkowski
 * @author Thomas Matlak
 */
public class RedisEnvironmentRepository implements EnvironmentRepository {

	private final StringRedisTemplate redis;

	private final RedisEnvironmentProperties properties;

	private final ConfigServerProperties serverProperties;

	public RedisEnvironmentRepository(StringRedisTemplate redis,
			RedisEnvironmentProperties properties,
			ConfigServerProperties configServerProperties) {
		this.redis = redis;
		this.properties = properties;
		this.serverProperties = configServerProperties;
	}

	@Override
	public Environment findOne(String application, String profile, String label) {
		application = StringUtils.isEmpty(application)
				? serverProperties.getDefaultApplicationName() : application;
		profile = StringUtils.isEmpty(profile) ? serverProperties.getDefaultProfile()
				: profile;
		label = StringUtils.isEmpty(label) ? serverProperties.getDefaultLabel() : label;

		String[] profiles = StringUtils.commaDelimitedListToStringArray(profile);
		Environment environment = new Environment(application, profiles, label, null,
				null);

		for (String prof : profiles) {
			environment.add(generatePropertySource(application, prof, label));
		}

		return environment;
	}

	private PropertySource generatePropertySource(String application, String profile,
			String label) {
		String key = application;

		if (!StringUtils.isEmpty(profile)) {
			key += "-" + profile;
		}

		if (!StringUtils.isEmpty(label)) {
			key += "-" + label;
		}

		return new PropertySource("redis:" + key, redis.opsForHash().entries(key));
	}

}
