package com.atosworldline.jira.plugins.valuesync.component.configuration;

public class Configuration {
	private String project;
	private String field;
	private String operator;
	private String valueToCompare;
	private String actionToDo;
	private String fieldToUpdate;
	private String updatedFieldValue;

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValueToCompare() {
		return valueToCompare;
	}

	public void setValueToCompare(String valueToCompare) {
		this.valueToCompare = valueToCompare;
	}

	public String getActionToDo() {
		return actionToDo;
	}

	public void setActionToDo(String actionToDo) {
		this.actionToDo = actionToDo;
	}

	public String getFieldToUpdate() {
		return fieldToUpdate;
	}

	public void setFieldToUpdate(String fieldToUpdate) {
		this.fieldToUpdate = fieldToUpdate;
	}

	public String getUpdatedFieldValue() {
		return updatedFieldValue;
	}

	public void setUpdatedFieldValue(String updatedFieldValue) {
		this.updatedFieldValue = updatedFieldValue;
	}

	public Configuration(String project, String field, String operator,
			String valueToCompare, String fieldToUpdate,
			String updatedFieldValue, String actionToDo) {
		this.project = project;
		this.field = field;
		this.operator = operator;
		this.valueToCompare = valueToCompare;
		this.actionToDo = actionToDo;
		this.fieldToUpdate = fieldToUpdate;
		this.updatedFieldValue = updatedFieldValue;
	}

	public boolean isValid() {
		if (project == null || field == null || operator == null
				|| valueToCompare == null || fieldToUpdate == null
				|| updatedFieldValue == null || actionToDo == null)
			return false;
		else if (project.length() == 0 || field.length() == 0
				|| operator.length() == 0 || valueToCompare.length() == 0
				|| fieldToUpdate.length() == 0
				|| updatedFieldValue.length() == 0 || actionToDo.length() == 0)
			return false;
		return true;
	}
}
