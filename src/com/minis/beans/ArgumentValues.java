package com.minis.beans;

	import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
	import java.util.LinkedHashMap;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.Map;
	import java.util.Set;

	public class ArgumentValues {
		private final Map<Integer, ArgumentValue> indexedArgumentValues = new HashMap<>(0);
		private final List<ArgumentValue> genericArgumentValues = new LinkedList<ArgumentValue>();

		public ArgumentValues() {
		}

		private void addArgumentValue(Integer key, ArgumentValue newValue) {
			this.indexedArgumentValues.put(key, newValue);
		}

		public boolean hasIndexedArgumentValue(int index) {
			return this.indexedArgumentValues.containsKey(index);
		}


		public ArgumentValue getIndexedArgumentValue(int index) {
			ArgumentValue valueHolder = this.indexedArgumentValues.get(index);
			return valueHolder;
		}

		public void addGenericArgumentValue(Object value, String type) {
			this.genericArgumentValues.add(new ArgumentValue(value, type));
		}


		private void addGenericArgumentValue(ArgumentValue newValue) {
			if (newValue.getName() != null) {
				for (Iterator<ArgumentValue> it = this.genericArgumentValues.iterator(); it.hasNext();) {
					ArgumentValue currentValue = it.next();
					if (newValue.getName().equals(currentValue.getName())) {
						it.remove();
					}
				}
			}
			this.genericArgumentValues.add(newValue);
		}

		public ArgumentValue getGenericArgumentValue(String requiredName) {
			for (ArgumentValue valueHolder : this.genericArgumentValues) {
				if (valueHolder.getName() != null &&
						(requiredName == null || !valueHolder.getName().equals(requiredName))) {
					continue;
				}
				return valueHolder;
			}
			return null;
		}


		public int getArgumentCount() {
			return (this.genericArgumentValues.size());
		}


		public boolean isEmpty() {
			return (this.genericArgumentValues.isEmpty());
		}
	}