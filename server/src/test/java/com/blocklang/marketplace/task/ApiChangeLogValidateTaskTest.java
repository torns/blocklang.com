package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blocklang.marketplace.data.changelog.ChangeLog;
import com.blocklang.marketplace.data.changelog.NewWidgetChange;
import com.blocklang.marketplace.data.changelog.WidgetEvent;
import com.blocklang.marketplace.data.changelog.WidgetEventArgument;
import com.blocklang.marketplace.data.changelog.WidgetProperty;
import com.blocklang.marketplace.data.changelog.WidgetPropertyOption;
import com.blocklang.marketplace.model.ComponentRepoPublishTask;
import com.blocklang.release.constant.ReleaseResult;

public class ApiChangeLogValidateTaskTest {
	
	private MarketplacePublishContext context;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Before
	public void setup() throws IOException {
		String folder = temp.newFolder().getPath();
		ComponentRepoPublishTask publishTask = new ComponentRepoPublishTask();
		publishTask.setGitUrl("https://github.com/blocklang/blocklang.com.git");
		publishTask.setStartTime(LocalDateTime.now());
		publishTask.setPublishResult(ReleaseResult.INITED);
		context = new MarketplacePublishContext(folder, publishTask);
	}

	@Test
	public void run_can_not_null() {
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, null);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_can_not_empty() {
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, Collections.emptyMap());
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_invalid_key_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("a", "1");
		changelogMap.put("b", "2");
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_id_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("author", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_author_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("changes", new ArrayList<Object>());
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_require_changes_at_root() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_id_should_be_string_and_author_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", 1);
		changelogMap.put("author", 2);
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("newWidget", new Object());
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_changes_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", "I am a String, Not a List");
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_should_has_one_or_more_change() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		changelogMap.put("changes", Collections.emptyList());
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_one_change_should_only_map_to_one_operator() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("operator-1", new Object());
		changes.add(change1);
		Map<String, Object> change2 = new HashMap<String, Object>();
		change1.put("operator-2", new Object());
		changes.add(change2);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_should_be_supported_operator() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		change1.put("not-supported-operator", new Object());
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("invalid-key", "a");
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_name_should_be_required_and_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", 1);
		newWidget.put("label", "a");
		newWidget.put("iconClass", "b");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", 1);
		newWidget.put("iconClass", "b");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("description", 1);
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_iconClass_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", 1);
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_app_type_should_be_string_array() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", "web");
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_app_type_value_should_be_web() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"not-web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_properties_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", "a");
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		newWidget.put("events", "a");
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("invalid-property-key", "a");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", 1);
		propertyMap.put("label", "a");
		propertyMap.put("value", "b");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", 1);
		propertyMap.put("value", "b");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", 1);
		propertyMap.put("valueType", "string");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", 1);
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_value_type_should_be_the_defined_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "invalid-type");
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		propertyMap.put("description", 1);
		propertyMap.put("options", Collections.emptyList());
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		propertyMap.put("options", "not-a-array");
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_invalid_key() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("invalid-key", "a");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// value 必填
	@Test
	public void run_changes_new_widget_properties_options_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// label 必填
	@Test
	public void run_changes_new_widget_properties_options_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", "b");
		optionMap.put("description", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// value description 在界面上可作为 dom 节点的 title 显示
	@Test
	public void run_changes_new_widget_properties_options_value_description_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", "b");
		optionMap.put("description", "c");
		optionMap.put("valueDescription", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_properties_options_icon_class_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "a");
		propertyMap.put("label", "b");
		propertyMap.put("value", "c");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "a");
		optionMap.put("label", "b");
		optionMap.put("iconClass", 1);
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		newWidget.put("events", Collections.emptyList());
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}

	@Test
	public void run_changes_new_widget_events_invalid_keys() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("invalid-event-key", "a");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// name 必填
	@Test
	public void run_changes_new_widget_events_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	// label 必填
	@Test
	public void run_changes_new_widget_events_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		// 不设置 valueType
		// eventMap.put("valueType", "function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_value_type_can_only_be_function() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "not-function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_description_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		eventMap.put("description", 1);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_should_be_list() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		eventMap.put("arguments", "not-a-array");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_can_be_null() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isPresent();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_keys_should_be_valid() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("invalid-key", "a");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_name_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_label_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_value_type_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_value_type_value_should_be_valid() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", "invalid-value");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}
	
	@Test
	public void run_changes_new_widget_events_arguments_description_value_should_be_string() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "1");
		changelogMap.put("author", "2");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "a");
		newWidget.put("label", "b");
		newWidget.put("iconClass", "c");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		newWidget.put("properties", Collections.emptyList());
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "a");
		eventMap.put("label", "b");
		eventMap.put("valueType", "function");
		
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "a");
		argumentMap.put("label", "b");
		argumentMap.put("value", "c");
		argumentMap.put("valueType", "string");
		argumentMap.put("description", 1);
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		assertThat(task.run()).isEmpty();
	}

	// 上面的用例都是校验
	// 下面的用例都是获取值
	// 只需要一个包含最全数据的测试用例
	@Test
	public void run_parse_data() {
		Map<String, Object> changelogMap = new HashMap<String, Object>();
		changelogMap.put("id", "change_id");
		changelogMap.put("author", "change_author");
		
		List<Map<String, Object>> changes = new ArrayList<Map<String,Object>>();
		Map<String, Object> change1 = new HashMap<String, Object>();
		
		Map<String, Object> newWidget = new HashMap<String, Object>();
		newWidget.put("name", "widget_name");
		newWidget.put("label", "widget_label");
		newWidget.put("description", "widget_description");
		newWidget.put("iconClass", "widget_iconClass");
		newWidget.put("appType", Arrays.asList(new String[] {"web"}));
		
		List<Map<String, Object>> properties = new ArrayList<Map<String,Object>>();
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("name", "widget_prop_name");
		propertyMap.put("label", "widget_prop_label");
		propertyMap.put("value", "widget_prop_value");
		propertyMap.put("description", "widget_prop_description");
		propertyMap.put("valueType", "string");
		
		List<Map<String, Object>> options = new ArrayList<Map<String,Object>>();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("value", "widget_prop_option_value");
		optionMap.put("label", "widget_prop_option_label");
		optionMap.put("description", "widget_prop_option_description");
		optionMap.put("iconClass", "widget_prop_option_iconClass");
		options.add(optionMap);
		propertyMap.put("options", options);
		properties.add(propertyMap);
		newWidget.put("properties", properties);
		
		List<Map<String, Object>> events = new ArrayList<Map<String,Object>>();
		Map<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("name", "widget_event_name");
		eventMap.put("label", "widget_event_label");
		eventMap.put("description", "widget_event_description");
		List<Map<String, Object>> arguments = new ArrayList<Map<String,Object>>();
		Map<String, Object> argumentMap = new HashMap<String, Object>();
		argumentMap.put("name", "widget_event_argument_name");
		argumentMap.put("label", "widget_event_argument_label");
		argumentMap.put("value", "widget_event_argument_value");
		argumentMap.put("valueType", "string");
		argumentMap.put("description", "widget_event_argument_description");
		arguments.add(argumentMap);
		eventMap.put("arguments", arguments);
		events.add(eventMap);
		newWidget.put("events", events);
		change1.put("newWidget", newWidget);
		changes.add(change1);
		changelogMap.put("changes", changes);
		
		ApiChangeLogValidateTask task = new ApiChangeLogValidateTask(context, changelogMap);
		Optional<ChangeLog> changelogOption = task.run();
		assertThat(changelogOption).isPresent();
		
		// 注意，event 的 valueType 的值为 function
		ChangeLog changelog = changelogOption.get();
		assertThat(changelog.getId()).isEqualTo("change_id");
		assertThat(changelog.getAuthor()).isEqualTo("change_author");
		assertThat(changelog.getChanges()).hasSize(1);
		
		NewWidgetChange newWidgetChange = (NewWidgetChange) changelog.getChanges().get(0);
		assertThat(newWidgetChange.getName()).isEqualTo("widget_name");
		assertThat(newWidgetChange.getLabel()).isEqualTo("widget_label");
		assertThat(newWidgetChange.getDescription()).isEqualTo("widget_description");
		assertThat(newWidgetChange.getIconClass()).isEqualTo("widget_iconClass");
		assertThat(newWidgetChange.getAppType()).hasSize(1);
		assertThat(newWidgetChange.getAppType().get(0)).isEqualTo("web");
		assertThat(newWidgetChange.getProperties()).hasSize(1);
		assertThat(newWidgetChange.getEvents()).hasSize(1);
		
		WidgetProperty property = newWidgetChange.getProperties().get(0);
		assertThat(property.getName()).isEqualTo("widget_prop_name");
		assertThat(property.getLabel()).isEqualTo("widget_prop_label");
		assertThat(property.getValue()).isEqualTo("widget_prop_value");
		assertThat(property.getDescription()).isEqualTo("widget_prop_description");
		assertThat(property.getValueType()).isEqualTo("string");
		assertThat(property.getOptions()).hasSize(1);
		
		WidgetPropertyOption propertyOption = property.getOptions().get(0);
		assertThat(propertyOption.getValue()).isEqualTo("widget_prop_option_value");
		assertThat(propertyOption.getLabel()).isEqualTo("widget_prop_option_label");
		assertThat(propertyOption.getDescription()).isEqualTo("widget_prop_option_description");
		assertThat(propertyOption.getIconClass()).isEqualTo("widget_prop_option_iconClass");
		
		WidgetEvent event = newWidgetChange.getEvents().get(0);
		assertThat(event.getName()).isEqualTo("widget_event_name");
		assertThat(event.getLabel()).isEqualTo("widget_event_label");
		assertThat(event.getValueType()).isEqualTo("function");
		assertThat(event.getDescription()).isEqualTo("widget_event_description");
		assertThat(event.getArguments()).hasSize(1);
		
		WidgetEventArgument eventArgument = event.getArguments().get(0);
		assertThat(eventArgument.getName()).isEqualTo("widget_event_argument_name");
		assertThat(eventArgument.getLabel()).isEqualTo("widget_event_argument_label");
		assertThat(eventArgument.getValue()).isEqualTo("widget_event_argument_value");
		assertThat(eventArgument.getValueType()).isEqualTo("string");
		assertThat(eventArgument.getDescription()).isEqualTo("widget_event_argument_description");
	}
}
