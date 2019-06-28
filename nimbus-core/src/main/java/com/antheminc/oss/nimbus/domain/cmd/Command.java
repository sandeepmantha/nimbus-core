/**
 *  Copyright 2016-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.domain.cmd;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.antheminc.oss.nimbus.FrameworkRuntimeException;
import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.cmd.CommandElement.Type;
import com.antheminc.oss.nimbus.domain.defn.Constants;
import com.antheminc.oss.nimbus.support.fi.util.SupplierUtils;
import com.antheminc.oss.nimbus.support.pojo.CollectionsTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Soham Chakravarti
 *
 */
@Data @ToString(of={"absoluteUri", "action", "behaviors", "clientUserId"}) 
public class Command implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String MISSING_COMMAND_ARGUMENTS_MSG = "Command with URI: %s cannot have null %s.";
	
	private final String absoluteUri;
	
	private String clientUserId;

	@Getter(value=AccessLevel.PROTECTED)
	private CommandElementLinked root;
	
	private Action action;
	
	private String event;
	
	private List<Behavior> behaviors;
	
	private Map<String, String[]> requestParams;
	
	@JsonIgnore
	private final Instant createdInstant = Instant.now();
	
	@JsonIgnore @Getter(value=AccessLevel.PRIVATE)
	private final transient CollectionsTemplate<List<Behavior>, Behavior> templateBehaviors = CollectionsTemplate.linked(()->getBehaviors(), s->setBehaviors(s));
	
	public Command(String absoluteUri) {
		this.absoluteUri = absoluteUri;
	}
	
	public Command(Command source) {
		this(source.getAbsoluteUri());
		setAction(source.getAction());
		setEvent(source.getEvent());
		setBehaviors(source.getBehaviors());
		setClientUserId(source.getClientUserId());
		
		CommandElementLinked clonedRoot = new CommandElementLinked(source.getRoot());
		setRoot(clonedRoot);
	}
	
	public CollectionsTemplate<List<Behavior>, Behavior> templateBehaviors() {
		return templateBehaviors;
	}

	/**
	 * <p>Validate this {@link Command} instance contains all required arguments.
	 * <p>The required arguments are: <ul></ul>
	 * @throws InvalidConfigException if this {@link Command} instance is missing a required argument
	 */
	public void validate() {
		Optional.ofNullable(getAction())
			.orElseThrow(()->new InvalidConfigException(String.format(MISSING_COMMAND_ARGUMENTS_MSG, getAbsoluteUri(), "Action")));
		
		if(CollectionUtils.isEmpty(getBehaviors()))
			throw new InvalidConfigException(String.format(MISSING_COMMAND_ARGUMENTS_MSG, getAbsoluteUri(), "Behavior"));
		
		validateCommandArgument(Type.ClientAlias);
		validateCommandArgument(Type.TENANT_ID);
		validateCommandArgument(Type.AppAlias);
		validateCommandArgument(Type.PlatformMarker);
		validateCommandArgument(Type.DomainAlias);
	}
	
	/**
	 * <p>Validates that {@code type} is present as one of the arguments within
	 * this {@link Command}.
	 * @param type the {@link Type} to validate
	 * @throws InvalidConfigException if {@code type} is not present
	 */
	private void validateCommandArgument(Type type) {
		if (!getElement(type).isPresent()) {
			throw new InvalidConfigException(getMissingArgumentErrorMsg(type));
		}
	}
	
	private String getMissingArgumentErrorMsg(Type type) {
		return String.format(MISSING_COMMAND_ARGUMENTS_MSG, getAbsoluteUri(), type.getDesc());
	}
	
	public boolean isRootDomainOnly() {
		return !root().findFirstMatch(Type.DomainAlias).hasNext();
	}

	public CommandElementLinked root() {
		return root;
	}
	
	public boolean isEvent() {
		return StringUtils.trimToNull(getEvent()) != null;
	}

	
	public Command createRootDomainCommand() {
		String cUri = buildUri(getRoot(), Type.DomainAlias);
		return CommandBuilder.withUri(cUri).getCommand();
	}

	public String getAliasUri(Type type) {
		return getElement(type).map(e -> e.getAliasUri()).orElse(null);
	}
	
	public Long getRefId(Type type) {
		return getElement(type).map(e -> e.getRefId()).orElse(null);
	}
	
	public String getAbsoluteUri(Type type) {
		return getElement(type).map(e -> e.getUri()).orElse(null);
	}

	public String getAlias(Type type) {
		return getElement(type).map(e -> e.getAlias()).orElse(null);
	}
	
	public Optional<CommandElementLinked> getElement(Type type) {
		return Optional.ofNullable(root().findFirstMatch(type));
	}

	public CommandElementLinked getElementSafely(Type type) {
		Optional<CommandElementLinked> commandElement = getElement(type);
		if (!commandElement.isPresent()) {
			throw new FrameworkRuntimeException(getMissingArgumentErrorMsg(type)); 
		}
		return commandElement.get();
	}
	
	public String getRelativeUri(String input) {
		// input doesn't have /p/ : prefix client/org/app/p/{domain-root} from incoming command 
		int iFirstQ = StringUtils.indexOf(input, "?");
		final String searchSeq = (iFirstQ != StringUtils.INDEX_NOT_FOUND) ? StringUtils.substring(input, 0, iFirstQ) : input;
		
		if(!StringUtils.contains(searchSeq, Constants.SEGMENT_PLATFORM_MARKER.code)) {
			String prefix = buildUri(Type.PlatformMarker) + getRootDomainUri();
			return prefix + input;
		}
		
		// input starts with /p/ : prefix client/org/app from incoming command
		if(StringUtils.startsWith(input, Constants.SEGMENT_PLATFORM_MARKER.code)) {
			String prefix = buildUri(Type.AppAlias);
			return prefix + input;
		}
		
		// input is complete: use as is
		return input;
	}

	
/* TODO Refactor -- START -- */ 
	public boolean isView() {
		String domainRoot = getRootDomainAlias();
		return StringUtils.startsWith(domainRoot, Constants.PREFIX_FLOW.code);
	}

	public String getAppAlias() {
		return getAlias(Type.AppAlias);
	}
	
	public Long getTenantId() {
		try {
			return Long.valueOf(getAlias(Type.TENANT_ID));
		} catch (NumberFormatException e) {
			throw new FrameworkRuntimeException("Tenant ID must be of type Long.");
		}
	}
	
	public String getRootClientAlias() {
		return getAlias(Type.ClientAlias);
	}
	
	public CommandElement getRootDomainElement() {
		Optional<CommandElementLinked> commandElement = getElement(Type.DomainAlias);
		if (commandElement.isPresent()) {
			return commandElement.get();
		}
		return null;
	}

	public String getRootDomainAlias() {
		return getRootDomainElement().getAlias();
	}

	public String getRootDomainUri() {
		return getRootDomainElement().getUri();
	}

	/**
	 * Returns the absolute domain alias of this command.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set</i> then getAbsoluteDomainAlias() returns <i>/domain</i></li>
	 * @return the absolute domain alias of this command.
	 */
	public String getAbsoluteDomainAlias() {
		String a = buildAlias(root().findFirstMatch(Type.DomainAlias));
		return a;
	}

	/**
	 * Returns the absolute domain URI of this command.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set</i> then getAbsoluteDomainAlias() returns <i>/domain/ef/gh</i></li>
	 * @return the absolute domain URI of this command.
	 */
	public String getAbsoluteDomainUri() {
		String u = buildUri(root().findFirstMatch(Type.DomainAlias));
		return u;
	}
	
	/**
	 * Returns the prefix of this command.
	 * 
	 * <p> <b>Examples:</b> <p>When
	 * {@code absoluteUri = "/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set"} then
	 * {@link #getPrefix()} returns {@code "/Acme/ab/cd"}.
	 * @return the absolute domain URI of this command.
	 */
	public String getPrefix() {
		return buildUri(Type.AppAlias);
	}

	
	public String getProcessAlias() {
		String a = buildAlias(root().findFirstMatch(Type.ProcessAlias));
		return a;
	}
	
	
	public String getProcessUri() {
		String u = buildUri(root().findFirstMatch(Type.ProcessAlias));
		return u;
	}

	/**
	 * Returns the absolute alias of this command.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set</i> then getAbsoluteAlias() returns <i>/Acme/ab/cd/domain/ef/gh</i></li>
	 * @return the absolute alias of this command.
	 */
	public String getAbsoluteAlias() {
		String a = buildAlias(root());
		return a;
	}
	
	/**
	 * Returns the absolute alias with only the action included of this command.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set</i> then getAbsoluteDomainAlias() returns <i>/domain/ef/gh/_process</i></li>
	 * @return the absolute alias with only the action included of this command.
	 */
	public String getAbsoluteAliasWithAction() {
		String a = buildAlias(root());
		
		return a + "/" + this.getAction();
	}

	/**
	 * Returns the absolute alias up to the root domain of this command.
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain/ef/gh/_process?fn=_set</i> then getAbsoluteAliasTillRootDomain() returns <i>/Acme/ab/cd/domain</i></li>
	 * @return the absolute alias up to the root domain of this command.
	 */
	public String getAbsoluteAliasUntilRootDomain(boolean includeRefId) {
		return buildAlias(root(), Type.DomainAlias, includeRefId);
	}
	
	/**
	 * Returns the absolute alias up to the root domain of this command with the ref id included
	 * 
	 * <p>
	 * <b>Examples:</b>
	 * <p>When <b>absoluteUri</b> = <i>/Acme/ab/cd/p/domain:42/ef/gh/_process?fn=_set</i> then getAbsoluteAliasTillRootDomain() returns <i>/Acme/ab/cd/domain:42</i></li>
	 * @return the absolute alias up to the root domain of this command.
	 */
	public String getAbsoluteAliasTillRootDomain() {
		return getAbsoluteAliasUntilRootDomain(false);
	}
	
/* TODO Refactor -- END -- */
	
	public String buildAlias(CommandElementLinked startElem) {
		return traverseElements(startElem, (cmdElem, sb) -> sb.append(cmdElem.getAliasUri()));
	}
	public String buildAlias(Type endWhentype) {
		return traverseElements(root(), endWhentype, (cmdElem, sb) -> sb.append(cmdElem.getAliasUri()));
	}
	public String buildAlias(CommandElementLinked startElem, Type endWhentype, boolean includeRefId) {
		BiConsumer<CommandElement, StringBuilder> withRefId = (cmdElem, sb) -> sb.append(cmdElem.getUri());
		BiConsumer<CommandElement, StringBuilder> withoutRefId = (cmdElem, sb) -> sb.append(cmdElem.getAliasUri());
		return traverseElements(startElem, endWhentype, includeRefId ? withRefId : withoutRefId);
	}
	public String buildAlias(CommandElementLinked startElem, Type endWhentype) {
		return traverseElements(startElem, endWhentype, (cmdElem, sb) -> sb.append(cmdElem.getAliasUri()));
	}
	
	public String buildUri(CommandElementLinked startElem) {
		return traverseElements(startElem, (cmdElem, sb) -> sb.append(cmdElem.getUri()));
	}
	public String buildUri(Type endWhenType) {
		return traverseElements(root(), endWhenType, (cmdElem, sb) -> sb.append(cmdElem.getUri()));
	}
	public String buildUri(CommandElementLinked startElem, Type endWhenType) {
		return traverseElements(startElem, endWhenType, (cmdElem, sb) -> sb.append(cmdElem.getUri()));
	}
	
	
	public String traverseElements(CommandElementLinked startElem, BiConsumer<CommandElement, StringBuilder> cb) {
		StringBuilder sb = new StringBuilder();
		traverseElements(startElem, (cmdElem) -> cb.accept(cmdElem, sb));
		return sb.toString();
	}
	
	public void traverseElements(CommandElementLinked startElem, Consumer<CommandElement> cb) {
		while (startElem != null) {
			cb.accept(startElem);
			startElem = startElem.next();
		}
	}
	
	public String traverseElements(CommandElementLinked startElem, Type type, BiConsumer<CommandElement, StringBuilder> cb) {
		StringBuilder sb = new StringBuilder();
		traverseElements(startElem, type, (cmdElem) -> cb.accept(cmdElem, sb));
		return sb.toString();
	}
	
	public void traverseElements(CommandElementLinked startElem, Type type, Consumer<CommandElement> cb) {
		while (startElem != null) {
			cb.accept(startElem);
			if (startElem.getType().equals(type)) {
				break;
			}
			startElem = startElem.next();
		}
	}

	

	public CommandElementLinked createRoot(Type type, String uri) {
		CommandElementLinked root = new CommandElementLinked();
		root.setType(type);
		root.setUri(uri);
		setRoot(root);
		
		return getRoot();
	}

	public String toUri() {
		String baseUri = buildUri(getRoot());
		StringBuilder sb = new StringBuilder(baseUri);
		
		/* action */
		sb.append(Constants.SEPARATOR_URI.code).append(getAction().name());
		
		/* event */
		if(isEvent()) {
			sb.append(Constants.SEPARATOR_URI.code).append(getEvent());	
		}
		
		/* behavior(s) */
		sb.append(Constants.REQUEST_PARAMETER_MARKER.code).append(Constants.MARKER_URI_BEHAVIOR.code).append(Constants.PARAM_ASSIGNMENT_MARKER.code);	//	?b=
		sb.append(getBehaviors().get(0).name());	// $execute (or other behavior)	
		
		getBehaviors().stream().sequential().skip(1).forEach(b->{
			sb.append(Constants.SEPARATOR_AND.code).append(b.name());
		});
		
		addRequestParamsToUri(sb);
		
		return sb.toString();
	}
	
	public String toRemoteUri(Type endWhenType, Action withAction, Behavior withBehavior) {
		String baseUri = buildUri(endWhenType);
		StringBuilder sb = new StringBuilder(baseUri);
		
		/* action */
		sb.append(Constants.SEPARATOR_URI.code).append(withAction.name());
		
		/* behavior=$execute */
		sb.append(Constants.REQUEST_PARAMETER_MARKER.code).append(Constants.MARKER_URI_BEHAVIOR.code).append(Constants.PARAM_ASSIGNMENT_MARKER.code);	//	?b=
		sb.append(withBehavior);	// $execute (or other behavior)	
		
		return sb.toString();
	}

	private void addRequestParamsToUri(StringBuilder sb) {
		if(MapUtils.isEmpty(getRequestParams()))
			return;
		
		getRequestParams().entrySet().stream()
			.filter(e -> !StringUtils.equals(e.getKey()+Constants.PARAM_ASSIGNMENT_MARKER.code, Constants.MARKER_URI_BEHAVIOR.code+Constants.PARAM_ASSIGNMENT_MARKER.code))
			.forEach(e -> Stream.of(e.getValue()).forEach(v -> sb.append(Constants.REQUEST_PARAMETER_DELIMITER.code).append(e.getKey()).append(Constants.PARAM_ASSIGNMENT_MARKER.code).append(v)));
	}
	
	public String[] getParameterValue(String requestParameter){
		if(requestParams != null && requestParams.containsKey(requestParameter)){
			return requestParams.get(requestParameter);
		}
		return null;
	}
	
	public String getFirstParameterValue(String requestParameter){
		if(requestParams != null && requestParams.containsKey(requestParameter)){
			String[] value = requestParams.get(requestParameter);
			if(value != null && value.length > 0){
				return value[0];
			}
		}
		return null;
	}
	
	public boolean hasRawPayload() {
		return getFirstParameterValue("rawPayload") != null;
	}
	
	public String getRawPayload() {
		return getFirstParameterValue("rawPayload");
	}
	
	public boolean containsFunction() {
		return requestParams != null && requestParams.containsKey(Constants.KEY_FUNCTION.code);
	}
	
	public Long acquireTenantId() {
		return SupplierUtils.acquire(this::getTenantId, "Tenant id must not be null for command: " + this);
	}
}
