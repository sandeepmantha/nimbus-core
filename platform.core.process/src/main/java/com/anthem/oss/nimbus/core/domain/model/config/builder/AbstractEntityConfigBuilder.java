/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.model.config.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.Constraint;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.util.ClassUtils;

import com.anthem.oss.nimbus.core.UnsupportedScenarioException;
import com.anthem.oss.nimbus.core.domain.config.builder.AnnotationConfigHandler;
import com.anthem.oss.nimbus.core.domain.definition.ConfigLoadException;
import com.anthem.oss.nimbus.core.domain.definition.Constants;
import com.anthem.oss.nimbus.core.domain.definition.Converters;
import com.anthem.oss.nimbus.core.domain.definition.Converters.ParamConverter;
import com.anthem.oss.nimbus.core.domain.definition.Domain;
import com.anthem.oss.nimbus.core.domain.definition.Execution;
import com.anthem.oss.nimbus.core.domain.definition.InvalidConfigException;
import com.anthem.oss.nimbus.core.domain.definition.MapsTo;
import com.anthem.oss.nimbus.core.domain.definition.MapsTo.State;
import com.anthem.oss.nimbus.core.domain.definition.Model;
import com.anthem.oss.nimbus.core.domain.definition.Repo;
import com.anthem.oss.nimbus.core.domain.definition.ViewConfig.ViewParamBehavior;
import com.anthem.oss.nimbus.core.domain.definition.ViewConfig.ViewStyle;
import com.anthem.oss.nimbus.core.domain.model.config.AnnotationConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ModelConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ParamConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ParamConfig.MappedParamConfig;
import com.anthem.oss.nimbus.core.domain.model.config.ParamType;
import com.anthem.oss.nimbus.core.domain.model.config.internal.DefaultModelConfig;
import com.anthem.oss.nimbus.core.domain.model.config.internal.DefaultParamConfig;
import com.anthem.oss.nimbus.core.domain.model.config.internal.MappedDefaultModelConfig;
import com.anthem.oss.nimbus.core.domain.model.config.internal.MappedDefaultParamConfig;
import com.anthem.oss.nimbus.core.domain.model.state.internal.StateContextEntity;
import com.anthem.oss.nimbus.core.rules.RulesEngineFactoryProducer;
import com.anthem.oss.nimbus.core.util.GenericUtils;
import com.anthem.oss.nimbus.core.util.JustLogit;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Soham Chakravarti
 *
 */
abstract public class AbstractEntityConfigBuilder {

	protected JustLogit logit = new JustLogit(getClass());
	
	@Autowired RulesEngineFactoryProducer rulesEngineFactoryProducer;
	
	@Autowired
	ApplicationContext ctx;

	
	abstract public <T> ModelConfig<T> buildModel(Class<T> clazz, EntityConfigVistor visitedModels);
	
	abstract public <T> ParamConfig<?> buildParam(ModelConfig<T> mConfig, Field f, EntityConfigVistor visitedModels);
	
	abstract protected <T, P> ParamType buildParamType(ModelConfig<T> mConfig, ParamConfig<P> pConfig, Field f, EntityConfigVistor visitedModels);
	abstract protected <T, P> ParamType buildParamType(ModelConfig<T> mConfig, ParamConfig<P> pConfig, ParamType.CollectionType colType, Class<?> pDirectOrColElemType, EntityConfigVistor visitedModels); 
	
	public boolean isPrimitive(Class<?> determinedType) {
		return ClassUtils.isPrimitiveOrWrapper(determinedType) || String.class==determinedType;
	}
	
	public <T> DefaultModelConfig<T> createModel(Class<T> referredClass, EntityConfigVistor visitedModels) {
		MapsTo.Type mapsToType = AnnotationUtils.findAnnotation(referredClass, MapsTo.Type.class);
		
		final DefaultModelConfig<T> created;
		if(mapsToType!=null) {
			
			ModelConfig<?> mapsTo = visitedModels.get(mapsToType.value());
			if(mapsTo==null)
				throw new InvalidConfigException(MapsTo.Type.class.getSimpleName()+" not found: "+mapsToType + " in mapped: " + referredClass);
			
			created = new MappedDefaultModelConfig<>(mapsTo, referredClass);
			
		} else {
			created = new DefaultModelConfig<>(referredClass);
		}
		
		// handle repo
		Repo rep = AnnotationUtils.findAnnotation(referredClass, Repo.class);
		created.setRepo(rep);
		
		// set domain or model
		assignDomainAndModel(created);
				
		// rules
		Optional.ofNullable(created.getAlias())
			.map(d->rulesEngineFactoryProducer.getFactory(referredClass))
			.map(f->f.createConfig(created.getAlias()))
				.ifPresent(c->created.setRulesConfig(c));
		return created; 
	}
	
	protected void assignDomainAndModel(DefaultModelConfig<?> created) {
		// prefer @Domain or @Model declared on current class
		Domain domain = AnnotationUtils.findAnnotation(created.getReferredClass(), Domain.class);
		
		// set model if domain is absent
		Model model = AnnotationUtils.findAnnotation(created.getReferredClass(), Model.class);
		
		if(domain==null && model!=null)
			created.setModel(model);
		else
			if(domain!=null && model==null)
				created.setDomain(domain);
		else
			if(domain!=null && model!=null // both present with different alias entries
					&& StringUtils.trimToNull(domain.value())!=null && StringUtils.trimToNull(model.value())!=null 
					&& !StringUtils.equals(domain.value(), model.value())) {
				
				// prefer annotation declared directly on class
				if(AnnotationUtils.isAnnotationInherited(Domain.class, created.getReferredClass()) 
						&& !AnnotationUtils.isAnnotationInherited(Model.class, created.getReferredClass()))
					created.setModel(model);
				else 
					if(!AnnotationUtils.isAnnotationInherited(Domain.class, created.getReferredClass()) 
							&& AnnotationUtils.isAnnotationInherited(Model.class, created.getReferredClass()))	
						created.setDomain(domain);
				else
					throw new InvalidConfigException("A model can have alias defined in either @Domain or @Model. "
							+ "Found in both with different values for class: "+created.getReferredClass()
							+" with @Domain: "+domain+" and @Model: "+model);
			}
		else {
			created.setDomain(domain);
			created.setModel(model);
		}
	}
	
	public <T> DefaultModelConfig<List<T>> createCollectionModel(Class<List<T>> referredClass, ParamConfig<?> associatedParamConfig) {
		//mapsTo is null when the model itself is a java List implementation (ArrayList, etc)
		
		DefaultModelConfig<List<T>> coreConfig = new DefaultModelConfig<>(referredClass);
		
		if(associatedParamConfig.isMapped()) {
			return new MappedDefaultModelConfig<>(coreConfig, referredClass);
		} else {
			return coreConfig;
		}
	}
	
	public DefaultParamConfig<?> createParam(ModelConfig<?> mConfig, Field f, EntityConfigVistor visitedModels) {
		MapsTo.Path mapsToPath = AnnotationUtils.findAnnotation(f, MapsTo.Path.class);
		
		// no path specified
		if(mapsToPath==null)
			return decorateParam(mConfig, f, DefaultParamConfig.instantiate(mConfig, f.getName()), visitedModels); 
			
		// check if field is mapped with linked=true: which would require parent model to also be mapped
		if(mapsToPath.linked())
			return createMappedParamAttached(mConfig, f, visitedModels, mapsToPath);
		else 
			return createMappedParamDetached(mConfig, f, visitedModels, mapsToPath);
	}
	
	private DefaultParamConfig<?> createMappedParamAttached(ModelConfig<?> mConfig, Field f, EntityConfigVistor visitedModels, MapsTo.Path mapsToPath) {
		// param field is linked: enclosing model must be mapped
		if(!mConfig.isMapped())
			throw new InvalidConfigException("Mapped param field: "+f.getName()+" is mapped with linked=true. Enclosing model: "+mConfig.getReferredClass()+" must be mapped, but was not.");

		// param field is linked: enclosing mapped model's config must have been loaded
		Class<?> mapsToModelClass = mConfig.findIfMapped().getMapsTo().getReferredClass();
		ModelConfig<?> mapsToModel = visitedModels.get(mapsToModelClass);
		if(mapsToModel==null)
			throw new ConfigLoadException("Mapped param field: "+f.getName()+" is mapped with linked=true. Enclosing model: "+mConfig.getReferredClass()
				+" mapsToModelClass: "+mapsToModelClass+" which must have been loaded prior, but wasn't.");
		
		// find mapsTo param from mapsTo model
		ParamConfig<?> mapsToParam = findMappedParam(mapsToModel, f.getName(), mapsToPath);
		if(mapsToParam==null)
			throw new InvalidConfigException("No mapsTo param found for mapped param field: "+f.getName()+" in enclosing model:"+mConfig.getReferredClass()+" with mapsToPath: "+mapsToPath);
		
		return decorateParam(mConfig, f, new MappedDefaultParamConfig<>(f.getName(), mapsToModel, mapsToParam, mapsToPath), visitedModels);
	}
	
	private DefaultParamConfig<?> createMappedParamDetached(ModelConfig<?> mConfig, Field mappedField, EntityConfigVistor visitedModels, MapsTo.Path mapsToPath) {
		if(!isCollection(mappedField.getType())) {
			return createMappedParamDetachedNested(mConfig, mappedField, visitedModels, mapsToPath);
		} 
		
		// detached collection
		return createMappedParamDetachedCollection(mConfig, mappedField, visitedModels, mapsToPath);
	}
	
	/**
	 * For detached mode, simulate mapsToParam. 
	 * This would require creating an enclosing ModelConfig which encloses the mapsTo Param.
	 * 
	 * Enclosing ModelConfig's type (referredClass) is top level holder class.
	 * 
	 * MapsTo ParaConfig's type is same as MapsTo.Type on mappedParam's referredClass. 
	 * If mappedParam's referredClass doesn't have MapsTo.Type defined, then mappedParam's referredClass would be used for self-detached mode simulation.
	 */
	private DefaultParamConfig<?> createMappedParamDetachedNested(ModelConfig<?> mConfig, Field mappedField, EntityConfigVistor visitedModels, MapsTo.Path mapsToPath) {
		// create mapsToParam's enclosing ModelConfig
		DefaultModelConfig<?> simulatedEnclosingModel = new DefaultModelConfig<>(SimulatedNestedParamEnclosingEntity.class);
		
		// create mapsToParam and attach to enclosing model
		DefaultParamConfig<?> simulatedMapsToParam = decorateParam(simulatedEnclosingModel, DefaultParamConfig.instantiate(simulatedEnclosingModel, MapsTo.DETACHED_SIMULATED_FIELD_NAME), visitedModels);
		simulatedEnclosingModel.templateParams().add(simulatedMapsToParam);
		
		// build mapsToParam's type
		MapsTo.Type mappedParamRefClassMapsToType = AnnotationUtils.findAnnotation(mappedField.getType(), MapsTo.Type.class);
		Class<?> mappedParamRefClassMapsToEntityClass = mappedParamRefClassMapsToType!=null ? mappedParamRefClassMapsToType.value() : mappedField.getType();
		
		ParamType pType = buildParamType(simulatedEnclosingModel, simulatedMapsToParam, null, mappedParamRefClassMapsToEntityClass, visitedModels);
		simulatedMapsToParam.setType(pType);
		
		DefaultParamConfig<?> mappedParam = decorateParam(mConfig, mappedField, new MappedDefaultParamConfig<>(mappedField.getName(), simulatedEnclosingModel, simulatedMapsToParam, mapsToPath), visitedModels);
		return mappedParam;
	}
	
	public DefaultParamConfig<?> createMappedParamDetachedCollection(ModelConfig<?> mConfigOfMappedParam, Field mappedField, EntityConfigVistor visitedModels, MapsTo.Path mapsToPath) {
		// create mapsToParam's enclosing ModelConfig
		DefaultModelConfig<?> simulatedEnclosingModel = new DefaultModelConfig<>(SimulatedCollectionParamEnclosingEntity.class);
		
		// create mapsToParam and attach to enclosing model
		DefaultParamConfig<?> simulatedMapsToParam = decorateParam(simulatedEnclosingModel, DefaultParamConfig.instantiate(simulatedEnclosingModel, MapsTo.DETACHED_SIMULATED_FIELD_NAME), visitedModels);
		simulatedEnclosingModel.templateParams().add(simulatedMapsToParam);
		
		// determine collection param generic element type
		final ParamType.CollectionType colType = determineCollectionType(mappedField.getType());
		final Class<?> determinedMappedColElemType = GenericUtils.resolveGeneric(mConfigOfMappedParam.getReferredClass(), mappedField);
		
		MapsTo.Type mappedParamRefClassMapsToType = AnnotationUtils.findAnnotation(determinedMappedColElemType, MapsTo.Type.class);
		Class<?> mappedParamRefClassMapsToEntityClass = mappedParamRefClassMapsToType!=null ? mappedParamRefClassMapsToType.value() : determinedMappedColElemType;
		
		ParamType pType = buildParamType(simulatedEnclosingModel, simulatedMapsToParam, colType, mappedParamRefClassMapsToEntityClass, visitedModels);
		simulatedMapsToParam.setType(pType);
		
		DefaultParamConfig<?> mappedParam = decorateParam(mConfigOfMappedParam, mappedField, new MappedDefaultParamConfig<>(mappedField.getName(), simulatedEnclosingModel, simulatedMapsToParam, mapsToPath), visitedModels);
		return mappedParam;
	}

	@Getter @Setter @Domain("_simulatedDetachedNested")
	public static class SimulatedNestedParamEnclosingEntity<E> {
		private E detachedParam; 
	}
	
	
	@Getter @Setter @Domain("_simulatedDetachedCollection")
	public static class SimulatedCollectionParamEnclosingEntity<E> {
		private List<E> detachedParam;
	}

	private <T, P> ParamConfig<P> createParamCollectionElemMappedAttached(ModelConfig<T> mConfig, MappedParamConfig<P, ?> pConfig, ModelConfig<List<P>> colModelConfig, EntityConfigVistor visitedModels, Class<?> colElemClass, MapsTo.Path mapsToColParamPath) {
		// colParam is mapped as Attached, but parent enclosing Model is un-mapped :- throw Ex
		if(!mConfig.isMapped()) { 	
			throw new InvalidConfigException("Param: "+pConfig.getCode()+" has @MapsTo.Path "+mapsToColParamPath+" with resolved mode: "+MapsTo.Mode.MappedAttached
						+" Attached Mapped Param must have Model that is mapped, but found with no @MapsTo.Model mappings for: "+mConfig.getReferredClass());
		}
		
		//ModelConfig<?> mapsToEnclosingModel = visitedModels.get(mConfig.findIfMapped().getMapsTo().getReferredClass());
		//logit.debug(()->"[create.pColElem] [colParam is mapped] [elemClass same] [Attached] Found parent mapsToEnclosingModel: "+mapsToEnclosingModel+" from visitedMmodels using: "+mConfig.findIfMapped().getMapsTo().getReferredClass());
		
		return createParamCollectionElemMapped(/*mapsToEnclosingModel, */pConfig, colModelConfig, visitedModels, colElemClass, mapsToColParamPath);
	}
	
	private <T, P> ParamConfig<P> createParamCollectionElemMappedDetached(ModelConfig<T> mConfig, MappedParamConfig<P, ?> pConfig, ModelConfig<List<P>> colModelConfig, EntityConfigVistor visitedModels, Class<?> colElemClass, MapsTo.Path mapsToColParamPath) {
		return createParamCollectionElemMapped(pConfig, colModelConfig, visitedModels, colElemClass, mapsToColParamPath);
	}
	
	
	private <T, P> ParamConfig<P> createParamCollectionElemMapped(MappedParamConfig<P, ?> pConfig, ModelConfig<List<P>> colModelConfig, EntityConfigVistor visitedModels, Class<?> colElemClass, MapsTo.Path mapsToColParamPath) {
		//ParamConfig<?> mapsToColParamConfig = findMappedParam(mapsToEnclosingModel, pConfig.getCode(), mapsToColParamPath);
		ParamConfig<?> mapsToColParamConfig = pConfig.getMapsTo();
		logit.debug(()->"[create.pColElem] [colParam is mapped] [elemClass same] [Attached] Found mapsToColParamConfig for "+pConfig.getCode()+" with mapsToPath of colParam: "+mapsToColParamPath+" -> "+mapsToColParamConfig);
		
		@SuppressWarnings("unchecked")
		ParamConfig<P> mapsToColElemParamConfig = (ParamConfig<P>)mapsToColParamConfig.getType().findIfCollection().getElementConfig();

		
		// colParam is mapped: colElemModel is NOT explicitly mapped BUT colElemClass is NOT SAME as mappedElemClass :- throw Ex
		if(colElemClass!=mapsToColElemParamConfig.getReferredClass()) {
			
			// handle {index} scenario in MapsTo.Path
			//if(StringUtils.contains(mapsToColParamPath.value(), Constants.MARKER_COLLECTION_ELEM_INDEX.code)) {
			if(MapsTo.hasCollectionPath(mapsToColParamPath)) {
				String colElemPathAfterIndexMarker = mapsToColParamPath.colElemPath();//StringUtils.substringAfter(mapsToColParamPath.value(), Constants.MARKER_COLLECTION_ELEM_INDEX.code);
				ParamConfig<P> mapsToNestedColElemParamConfig = mapsToColElemParamConfig.findParamByPath(colElemPathAfterIndexMarker);
				
				return createParamCollectionElementInternal(colModelConfig, mapsToNestedColElemParamConfig, mapsToColParamPath, visitedModels, pConfig.getCode());
				
			} else {
			
				MapsTo.Type mapsToElemModel = AnnotationUtils.findAnnotation(colElemClass, MapsTo.Type.class);
				
				if(mapsToElemModel==null)
					throw new InvalidConfigException("Mapped Elem Class is not same as MapsTo Elem Class. Must be same or an explicit MapsTo.Model mapping is required. "
							//+ " For EnclosingModel: "+mapsToEnclosingModel.getReferredClass()
							+" param: "+pConfig.getCode()
							+ " Expected elemClass: "+colElemClass+" but found mapsToElemClass: "+mapsToColElemParamConfig.getReferredClass());
			}
		}
		
		return createParamCollectionElementInternal(colModelConfig, mapsToColElemParamConfig, mapsToColParamPath, visitedModels, pConfig.getCode());
	}
	
	public <T, P> ParamConfig<P> createParamCollectionElement(ModelConfig<T> mConfig, ParamConfig<P> pConfig, ModelConfig<List<P>> colModelConfig, EntityConfigVistor visitedModels, Class<?> colElemClass) {
		logit.trace(()->"[create.pColElem] starting to process colElemClass: "+colElemClass+" with pConfig :"+pConfig.getCode());
		
		MapsTo.Path mapsToColParamPath = pConfig.isMapped() ? pConfig.findIfMapped().getPath() : null;
		MapsTo.Mode mapsToColParamMode = MapsTo.getMode(mapsToColParamPath);
		
		logit.debug(()->"[create.pColElem] mapsToColParam: "+mapsToColParamPath);
		logit.debug(()->"[create.pColElem] mapsToModeColParam: "+mapsToColParamMode);
		logit.debug(()->"[create.pColElem] colParamCode: "+pConfig.getCode());
		
		if(mapsToColParamMode==MapsTo.Mode.UnMapped) { 
			ParamConfig<P> pCoreElemConfig = createParamCollectionElementInternal(colModelConfig, null, null, visitedModels, pConfig.getCode());
		
			logit.trace(()->"[create.pColElem] [colParam is UnMapped] returning core pColElem Config as colElem is UnMapped.");
			return pCoreElemConfig;
			
		} else if(mapsToColParamMode==MapsTo.Mode.MappedAttached) {
			ParamConfig<P> pMappedAttachedElemConfig = createParamCollectionElemMappedAttached(mConfig, pConfig.findIfMapped(), colModelConfig, visitedModels, colElemClass, mapsToColParamPath);
			return pMappedAttachedElemConfig;
			
		} else if(mapsToColParamMode==MapsTo.Mode.MappedDetached) {
			ParamConfig<P> pMappedDetachedElemConfig = createParamCollectionElemMappedDetached(mConfig, pConfig.findIfMapped(), colModelConfig, visitedModels, colElemClass, mapsToColParamPath);
			return pMappedDetachedElemConfig;
			
		} else {
			throw new UnsupportedScenarioException("Param: "+pConfig.getCode()+" has @MapsTo.Path "+mapsToColParamPath+" with unknown mode: "+mapsToColParamMode
					+" in enclosing model: "+mConfig.getReferredClass());
		}
	}
	
	private <P> ParamConfig<P> createParamCollectionElementInternal(ModelConfig<List<P>> colModelConfig, ParamConfig<P> mapsToColElemParamConfig, MapsTo.Path mapsToColParamPath, EntityConfigVistor visitedModels, String colParamCode) {
		final String collectionElemPath = createCollectionElementPath(colParamCode);
		
		final ParamConfig<P> created;
		if(colModelConfig.isMapped()) {
			final MapsTo.Path mapsToColElemParamPathAnnotation = mapsToColParamPath==null ? null : createNewImplicitMapping(collectionElemPath, mapsToColParamPath.linked(), mapsToColParamPath.state());
			
			created = new MappedDefaultParamConfig<>(collectionElemPath, colModelConfig, mapsToColElemParamConfig, mapsToColElemParamPathAnnotation);

		} else if(mapsToColElemParamConfig==null) {
			created = decorateParam(colModelConfig, DefaultParamConfig.instantiate(colModelConfig, collectionElemPath), visitedModels);
			
		} else {
			created = mapsToColElemParamConfig;
			
		}
		return created;
	}
	
	public static String createCollectionElementPath(String collectionPath) {
		return new StringBuilder()
				//.append(collectionPath)
				//.append(Constants.SEPARATOR_URI.code)
				.append(Constants.MARKER_COLLECTION_ELEM_INDEX.code)
				.toString();
	}
	
	private <P> DefaultParamConfig<P> decorateParam(ModelConfig<?> mConfig, Field f, DefaultParamConfig<P> created, EntityConfigVistor visitedModels) {
		created.setUiNatures(AnnotationConfigHandler.handle(f, ViewParamBehavior.class));
		created.setUiStyles(AnnotationConfigHandler.handleSingle(f, ViewStyle.class));
		created.setExecutionConfigs(new ArrayList<>(AnnotatedElementUtils.findMergedRepeatableAnnotations(f, Execution.Config.class)));
		
		
		if(AnnotatedElementUtils.isAnnotated(f, Converters.class)) {
			Converters convertersAnnotation = AnnotationUtils.getAnnotation(f, Converters.class);
			List<ParamConverter> converters = new ArrayList<>();
			
			Arrays.asList(convertersAnnotation.converters())
						.forEach((converterClass)->converters.add(ctx.getBean(converterClass)));
			
			created.setConverters(converters);
		}

		if(AnnotatedElementUtils.isAnnotated(f, Model.Param.Values.class)) {
			Model.Param.Values aVal = AnnotationUtils.getAnnotation(f, Model.Param.Values.class);
			
			//Model.Param.Values.Source srcValues = ClassLoadUtils.newInstance(aVal.value());
			//List<ParamValue> values = srcValues.getValues(created.getCode());
			created.setValues(aVal);
			//created.setValues(values);
		}
		
		String value ="";
		if(AnnotatedElementUtils.isAnnotated(f, Model.Param.Text.class)) {
			Model.Param.Text aVal = AnnotationUtils.getAnnotation(f, Model.Param.Text.class);
			
			//Model.Param.String srcValues = ModelsTemplate.newInstance(aVal.value());
			value = aVal.label();
		}
		//not mapped Param
		ParamConfig.Desc desc = new ParamConfig.Desc();
		desc.setHelp(created.getCode());
		desc.setHint(created.getCode());
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(value)){
			desc.setLabel(value);
		}
		else{
			desc.setLabel(created.getCode());
		}
		
		created.setDesc(desc);

		List<AnnotationConfig> vConfig = AnnotationConfigHandler.handle(f, Constraint.class);
		created.setValidations(vConfig);
		
		return decorateParam(mConfig, created, visitedModels);
	}
	
	private ParamConfig<StateContextEntity> cachedRuntimeEntityParamConfig;
	
	/**
	 * build and assign RuntimeEntity Config
	 */
	protected <P> DefaultParamConfig<P> decorateParam(ModelConfig<?> mConfig, DefaultParamConfig<P> created, EntityConfigVistor visitedModels) {
		// do not make nested runtimeConfig;
		if(StringUtils.equals(created.getCode(), Constants.SEPARATOR_CONFIG_ATTRIB.code))
			return created;
		
		if(cachedRuntimeEntityParamConfig==null) {
			DefaultParamConfig<StateContextEntity> pRuntimeConfig = DefaultParamConfig.instantiate(mConfig, Constants.SEPARATOR_CONFIG_ATTRIB.code);
			cachedRuntimeEntityParamConfig = pRuntimeConfig;
			
			ModelConfig<StateContextEntity> mRuntimeConfig = buildModel(StateContextEntity.class, visitedModels);
			
			ParamType.Nested<StateContextEntity> nestedType = new ParamType.Nested<>(StateContextEntity.class.getSimpleName(), StateContextEntity.class);
			nestedType.setModel(mRuntimeConfig);
			
			pRuntimeConfig.setType(nestedType);
		}
		
		created.setContextParam(cachedRuntimeEntityParamConfig);
		return created;
	}
	
	
	//TODO Implement mapped param lookup given via explicit path reference
	private <T> ParamConfig<?> findMappedParam(ModelConfig<T> mapsToModel, String fieldNm, MapsTo.Path mapsTo) {
		if(mapsTo!=null && !StringUtils.isEmpty(mapsTo.value())) { //: if @Path has an explicit value
			String path = mapsTo.value();
			ParamConfig<?> mapsToParam = mapsToModel.findParamByPath(path);
			return mapsToParam;
			//throw new UnsupportedOperationException("TODO: Explicit Mapped Path lookup is not yet implemented. Found: "+ mapsTo.value());
		}
		
		ParamConfig<?> mappedToParam = mapsToModel.templateParams().find(fieldNm);
		return mappedToParam;
	}
	
	public static MapsTo.Path createNewImplicitMapping(String mappedPath, boolean linked, State state) {
		return createNewImplicitMapping(mappedPath, linked, state, "");
	}
	
	public static MapsTo.Path createNewImplicitMapping(String mappedPath, boolean linked, State state, String colElemPath) {
		return new MapsTo.Path() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return MapsTo.Path.class;
			}
			
			@Override
			public String value() {
				return mappedPath;
			}
			
			@Override
			public State state() {
				return state;
			}
			
			@Override
			public boolean linked() {
				return linked;
			}
			
			@Override
			public String colElemPath() {
				return colElemPath;
			}
			
			@Override
			public String toString() {
				return new StringBuilder().append(MapsTo.Path.class)
							.append(" value: ").append(value())
							.append(" linked: ").append(linked())
							.toString();
			}
		};
	}


	protected <P> ParamType.NestedCollection<P> createNestedCollectionType(ParamType.CollectionType colType) {
		Class<?> referredClass = ArrayList.class;
		String name = ClassUtils.getShortName(referredClass);
		
		ParamType.NestedCollection<P> nestedColType = new ParamType.NestedCollection<>(name, referredClass, colType);
		return nestedColType;
	}
	
	protected <T, P> ParamType createParamType(boolean isArray, Class<P> determinedType, ModelConfig<?> mConfig, EntityConfigVistor visitedModels) {
		final ParamType pType;
		if(isPrimitive(determinedType)) {	//Primitives bare or with wrapper & String
			String name = ClassUtils.getShortNameAsProperty(ClassUtils.resolvePrimitiveIfNecessary(determinedType));
			pType = new ParamType.Field(isArray, name, determinedType);

		} else if(lookUpTypeClassMapping(determinedType)!=null) { //custom mapping overrides
			String name = lookUpTypeClassMapping(determinedType);
			pType = new ParamType.Field(isArray, name, determinedType);
			
		} else if(AnnotationUtils.findAnnotation(determinedType, Model.class)!=null) { 
			String name = ClassUtils.getShortName(determinedType);
			pType = createParamTypeNested(name, determinedType, mConfig, visitedModels);
			
		} else { //All others: Treat as field type instead of complex object that requires config traversal
			pType = new ParamType.Field(isArray, ClassUtils.getShortName(determinedType), determinedType);
			
		}
		return pType;
	}
	
	protected <P> ParamType.Nested<P> createParamTypeNested(String typeName, Class<P> determinedType, ModelConfig<?> mConfig, EntityConfigVistor visitedModels) {
		
		final ParamType.Nested<P> nestedParamType = new ParamType.Nested<>(typeName, determinedType);
		
		final ModelConfig<P> nmConfig; 
		if(mConfig.getReferredClass()==determinedType) { //nested reference to itself
			nmConfig = (ModelConfig<P>)mConfig;
			
		} else if(visitedModels.contains(determinedType)) { //any nested model in hierarchy pointing back to any of its parents
			nmConfig = (ModelConfig<P>)visitedModels.get(determinedType);
			
		} else {
			nmConfig = buildModel(determinedType, visitedModels);
		}
		
		nestedParamType.setModel(nmConfig);
		return nestedParamType;
	}
	
	protected boolean isCollection(Class<?> clazz) {
		return determineCollectionType(clazz) != null;
	}
	
	protected ParamType.CollectionType determineCollectionType(Class<?> clazz) {
		if(clazz.isArray())	//Array
			return ParamType.CollectionType.array;
		
		else if(Collection.class.isAssignableFrom(clazz))  //Collection
			return ParamType.CollectionType.list;
			
		else if(Page.class.isAssignableFrom(clazz))	//Page
			return ParamType.CollectionType.page;
		
		return null;
	}
	
	abstract protected String lookUpTypeClassMapping(Class<?> clazz);
}
