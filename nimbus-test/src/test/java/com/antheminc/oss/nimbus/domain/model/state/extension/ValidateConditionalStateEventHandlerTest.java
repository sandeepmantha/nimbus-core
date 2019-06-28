package com.antheminc.oss.nimbus.domain.model.state.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.antheminc.oss.nimbus.domain.cmd.Command;
import com.antheminc.oss.nimbus.domain.cmd.CommandBuilder;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_1;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_2;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_3;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_4;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_5;
import com.antheminc.oss.nimbus.domain.defn.extension.ValidateConditional.GROUP_6;
import com.antheminc.oss.nimbus.domain.model.state.AbstractStateEventHandlerTests;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;

/**
 * 
 * @author Tony Lopez
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValidateConditionalStateEventHandlerTest extends AbstractStateEventHandlerTests {

	@Override
	protected Command createCommand() {
		return CommandBuilder.withUri(PLATFORM_ROOT + "/sample_view/_new").getCommand();
	}
	
	@Test
	public void t00_init() {
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/nested_condition"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_3"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_4"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_1"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p5"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p6"));
		assertNotNull(_q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p7"));
	}
	
	@Test
	public void t01_groupValidation_multipleParams_1() {
		Param<String> condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition");
		
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Assert.assertEquals(2, validate_p1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Assert.assertEquals(2, validate_p2.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		
		Param<String> validate_p4 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p4");
		Assert.assertEquals(1, validate_p4.getConfig().getValidations().size());
		Assert.assertEquals(1, validate_p4.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_3.class, validate_p4.getActiveValidationGroups()[0]);
		
		condition.setState("rigby");
		
		Assert.assertEquals(1, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p1.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p2.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(0, validate_p4.getActiveValidationGroups().length);
	}
	
	@Test
	public void t02_groupValidation_multipleParams_2() {
		Param<String> condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition");
		
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Assert.assertEquals(2, validate_p1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Assert.assertEquals(2, validate_p2.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		
		Param<String> validate_p4 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p4");
		Assert.assertEquals(validate_p4.getConfig().getValidations().size(), 1);
		Assert.assertEquals(1, validate_p4.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_3.class, validate_p4.getActiveValidationGroups()[0]);
		
		condition.setState("hooli");
		
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_2.class, validate_p2.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(1, validate_p4.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_3.class, validate_p4.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t03_groupValidation_nested() {
		Param<String> nested_condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/nested_condition");
		
		Param<String> validate_p3_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_1");
		Assert.assertEquals(1, validate_p3_1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p3_1.getActiveValidationGroups().length);
		
		Param<String> validate_p3_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_2/validate_p3_2_1");
		Assert.assertEquals(1, validate_p3_2_1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p3_2_1.getActiveValidationGroups().length);
		
		nested_condition.setState("rigby");
		
		Assert.assertEquals(1, validate_p3_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p3_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, validate_p3_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p3_2_1.getActiveValidationGroups()[0]);
		
		nested_condition.setState("hooli");
		
		Assert.assertEquals(1, validate_p3_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_2.class, validate_p3_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, validate_p3_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_2.class, validate_p3_2_1.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t04_validationRemoval_1() {
		Param<String> condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition");
		
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Assert.assertEquals(2, validate_p1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Assert.assertEquals(2, validate_p2.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		
		condition.setState("rigby");
		
		Assert.assertEquals(1, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p1.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p2.getActiveValidationGroups()[0]);
		
		condition.setState("hooli");
		
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_2.class, validate_p2.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t05_validateEvents_sibling() {
		Param<String> nested_condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/nested_condition");
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Param<String> validate_p3_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_1");
		Param<String> validate_p3_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_2/validate_p3_2_1");
		Param<String> validate_p3_2_2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_2/validate_p3_2_2");
		
		addListener();
		nested_condition.setState("rigby");
		
		// validate events
		assertNotNull(_paramEvents);
		assertEquals(5, _paramEvents.size());
		
		List<Param<?>> expectedEventParams1 = new ArrayList<>();
		expectedEventParams1.add(validate_p1);
		expectedEventParams1.add(validate_p2);
		expectedEventParams1.add(validate_p3_1);
		expectedEventParams1.add(validate_p3_2_1);
		expectedEventParams1.add(nested_condition);
		
		_paramEvents.stream().forEach(pe->expectedEventParams1.remove(pe.getParam()));
		assertTrue(expectedEventParams1.isEmpty());
		
		nested_condition.setState("hooli");
		
		// validate events
		assertNotNull(_paramEvents);
		assertEquals(6, _paramEvents.size());
		
		List<Param<?>> expectedEventParams2 = new ArrayList<>();
		expectedEventParams2.add(validate_p1);
		expectedEventParams2.add(validate_p2);
		expectedEventParams2.add(validate_p3_1);
		expectedEventParams2.add(validate_p3_2_1);
		expectedEventParams2.add(validate_p3_2_2);
		expectedEventParams2.add(nested_condition);
		
		_paramEvents.stream().forEach(pe->expectedEventParams2.remove(pe.getParam()));
		assertTrue(expectedEventParams2.isEmpty());
	}
	
	@Test
	public void t06_validationRemoval_1_multipleScenarios() {
		Param<String> condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition");
		
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Assert.assertEquals(2, validate_p1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Assert.assertEquals(2, validate_p2.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		
		Param<String> validate_p4 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p4");
		Assert.assertEquals(1, validate_p4.getConfig().getValidations().size());
		Assert.assertEquals(1, validate_p4.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_3.class, validate_p4.getActiveValidationGroups()[0]);
		
		condition.setState("rigby");
		
		Assert.assertEquals(1, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p1.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p2.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(0, validate_p4.getActiveValidationGroups().length);
		
		condition.setState("unknown");
		
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(1, validate_p4.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_3.class, validate_p4.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t07_validationRemoval_2_nested() {
		Param<String> nested_condition = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/nested_condition");
		
		Param<String> validate_p1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p1");
		Assert.assertEquals(2, validate_p1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		
		Param<String> validate_p2 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p2");
		Assert.assertEquals(2, validate_p2.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		
		Param<String> validate_p3_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_1");
		Assert.assertEquals(1, validate_p3_1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p3_1.getActiveValidationGroups().length);
		
		Param<String> validate_p3_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p3/validate_p3_2/validate_p3_2_1");
		Assert.assertEquals(1, validate_p3_2_1.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p3_2_1.getActiveValidationGroups().length);
		
		nested_condition.setState("rigby");
		
		Assert.assertEquals(1, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p2.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, validate_p3_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p3_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, validate_p3_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, validate_p3_2_1.getActiveValidationGroups()[0]);
		
		nested_condition.setState("unknown");
		
		Assert.assertEquals(0, validate_p1.getActiveValidationGroups().length);
		Assert.assertEquals(0, validate_p2.getActiveValidationGroups().length);
		Assert.assertEquals(0, validate_p3_1.getActiveValidationGroups().length);
		Assert.assertEquals(0, validate_p3_2_1.getActiveValidationGroups().length);
	}
	
	@Test
	public void t08_validateEvents_multipleTrueConditions() {
		Param<String> condition_3 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_3");
		
		Param<String> validate_p5 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p5");
		Assert.assertEquals(1, validate_p5.getConfig().getValidations().size());
		Assert.assertEquals(1, validate_p5.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_4.class, validate_p5.getActiveValidationGroups()[0]);
		
		Param<String> validate_p6 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p6");
		Assert.assertEquals(1, validate_p6.getConfig().getValidations().size());
		Assert.assertEquals(0, validate_p6.getActiveValidationGroups().length);
		
		addListener();
		condition_3.setState("paloalto");
		
		Assert.assertEquals(1, validate_p5.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_4.class, validate_p5.getActiveValidationGroups()[0]);
		
		Assert.assertEquals(1, validate_p6.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_5.class, validate_p6.getActiveValidationGroups()[0]);
		
		// validate events
		assertNotNull(_paramEvents);
		assertEquals(2, _paramEvents.size());
		
		List<Param<?>> expectedEventParams = new ArrayList<>();
		expectedEventParams.add(validate_p6);
		expectedEventParams.add(condition_3);
		
		_paramEvents.stream().forEach(pe->expectedEventParams.remove(pe.getParam()));
		assertTrue(expectedEventParams.isEmpty());
	}
	
	@Test
	public void t09_validateConditional_onStateLoad() {
		Param<String> validate_p7 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/validate_p7");
		Assert.assertEquals(1, validate_p7.getConfig().getValidations().size());
		Assert.assertEquals(1, validate_p7.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_6.class, validate_p7.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t10_validateConditional_targetPath_sibling() {
		// ensure no validations are added to start
		Param<String> q1_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/q1_1");
		Assert.assertEquals(1, q1_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_1.getActiveValidationGroups().length);
		Param<String> q1_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/nested/q1_2_1");
		Assert.assertEquals(1, q1_2_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_2_1.getActiveValidationGroups().length);
		
		// trigger validate conditional state change handler
		Param<String> condition_5 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_5");
		condition_5.setState("hello");
		
		// ensure validations
		Assert.assertEquals(1, q1_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, q1_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_2_1.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t11_validateConditional_targetPath_children() {
		// ensure no validations are added to start
		Param<String> q1_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/q1_1");
		Assert.assertEquals(1, q1_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_1.getActiveValidationGroups().length);
		Param<String> q1_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/nested/q1_2_1");
		Assert.assertEquals(1, q1_2_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_2_1.getActiveValidationGroups().length);
		
		// trigger validate conditional state change handler
		Param<String> condition_6 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_6");
		condition_6.setState("hello");
		
		// ensure validations
		Assert.assertEquals(1, q1_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, q1_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_2_1.getActiveValidationGroups()[0]);
	}
	
	@Test
	public void t12_validateConditional_targetPaths() {
		// ensure no validations are added to start
		Param<String> q1_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/q1_1");
		Assert.assertEquals(1, q1_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_1.getActiveValidationGroups().length);
		Param<String> q1_2_1 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested_2/q1/nested/q1_2_1");
		Assert.assertEquals(1, q1_2_1.getConfig().getValidations().size());
		Assert.assertEquals(0, q1_2_1.getActiveValidationGroups().length);
		
		// trigger validate conditional state change handler
		Param<String> condition_7 = _q.getRoot().findParamByPath("/sample_core/attr_validate_nested/condition_7");
		condition_7.setState("hello");
		
		// ensure validations
		Assert.assertEquals(1, q1_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_1.getActiveValidationGroups()[0]);
		Assert.assertEquals(1, q1_2_1.getActiveValidationGroups().length);
		Assert.assertEquals(GROUP_1.class, q1_2_1.getActiveValidationGroups()[0]);
	}
}
