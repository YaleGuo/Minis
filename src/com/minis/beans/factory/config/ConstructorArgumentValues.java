package com.minis.beans.factory.config;

	import java.util.ArrayList;
	import java.util.List;


	public class ConstructorArgumentValues {
		private final List<ConstructorArgumentValue> argumentValueList = new ArrayList<ConstructorArgumentValue>();

		public ConstructorArgumentValues() {
		}

		public void addArgumentValue(ConstructorArgumentValue argumentValue) {
			this.argumentValueList.add(argumentValue);
		}

		public ConstructorArgumentValue getIndexedArgumentValue(int index) {
			ConstructorArgumentValue argumentValue = this.argumentValueList.get(index);
			return argumentValue;
		}

		public int getArgumentCount() {
			return (this.argumentValueList.size());
		}

		public boolean isEmpty() {
			return (this.argumentValueList.isEmpty());
		}
	}