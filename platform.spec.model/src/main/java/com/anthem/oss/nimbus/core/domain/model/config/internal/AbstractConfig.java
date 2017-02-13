/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.model.config.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import com.anthem.oss.nimbus.core.domain.model.config.Config;
import com.anthem.oss.nimbus.core.util.JustLogit;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Soham Chakravarti
 *
 */
@Getter @Setter
abstract public class AbstractConfig<T> implements Config<T> {

	@JsonIgnore final protected JustLogit logit = new JustLogit(getClass());

	@JsonIgnore private List<Annotation> annotations;

}