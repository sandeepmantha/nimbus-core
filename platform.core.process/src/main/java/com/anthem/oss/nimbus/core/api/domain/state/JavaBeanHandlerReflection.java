/**
 * 
 */
package com.anthem.oss.nimbus.core.api.domain.state;

import java.lang.reflect.Method;

import org.springframework.stereotype.Component;

import com.anthem.nimbus.platform.spec.model.exception.InvalidConfigException;
import com.anthem.nimbus.platform.spec.model.exception.PlatformRuntimeException;

/**
 * @author Soham Chakravarti
 *
 */
@Component("default.java.bean.handler")
public class JavaBeanHandlerReflection implements JavaBeanHandler {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Method readMethod, Object target) {
		try {
			return (target == null) ? null : (T)readMethod.invoke(target);
		}
		catch (Exception ex) {
			throw new PlatformRuntimeException("Failed to execute read on : "+readMethod, ex);
		}
	}
	
	@Override
	public <T> void setValue(Method writeMethod, Object target, T value) {
		try {
			writeMethod.invoke(target, value);
		} catch (Exception ex) {
			throw new PlatformRuntimeException("Failed to execute write on : "+writeMethod+" with value: "+value, ex);
		}
	}
	
	@Override
	public <T> T instantiate(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} 
		catch (Exception ex) {
			throw new InvalidConfigException("Class could not be instantiated with blank constructor: " + clazz, ex);
		}
	}
}