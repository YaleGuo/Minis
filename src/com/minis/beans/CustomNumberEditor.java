package com.minis.beans;

import java.text.NumberFormat;

import com.minis.util.NumberUtils;
import com.minis.util.StringUtils;

public class CustomNumberEditor implements PropertyEditor {
	private Class<? extends Number> numberClass;
	private NumberFormat numberFormat;
	private boolean allowEmpty;
	private Object value;
	
	public CustomNumberEditor(Class<? extends Number> numberClass,
			  boolean allowEmpty) throws IllegalArgumentException {
		this(numberClass, null, allowEmpty);
	}
	
	public CustomNumberEditor(Class<? extends Number> numberClass,
			 NumberFormat numberFormat, boolean allowEmpty) throws IllegalArgumentException {
		this.numberClass = numberClass;
		this.numberFormat = numberFormat;
		this.allowEmpty = allowEmpty;
	}
	
	@Override
	public void setAsText(String text) {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		}
		else if (this.numberFormat != null) {
			// Use given NumberFormat for parsing text.
			setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
		}
		else {
			// Use default valueOf methods for parsing text.
			setValue(NumberUtils.parseNumber(text, this.numberClass));
		}
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Number) {
			this.value = (NumberUtils.convertNumberToTargetClass((Number) value, this.numberClass));
		}
		else {
			this.value = value;
		}
	}

	@Override
	public String getAsText() {
		Object value = this.value;
		if (value == null) {
			return "";
		}
		if (this.numberFormat != null) {
			// Use NumberFormat for rendering value.
			return this.numberFormat.format(value);
		}
		else {
			// Use toString method for rendering value.
			return value.toString();
		}
	}

	@Override
	public Object getValue() {
		return this.value;
	}

}
