package org.qipeng.excel.support;

public abstract class ExcelDataConverter {

	public abstract Object convertFromString(String str);
	
	public abstract String convertToString(Object t);
	
	
	public static class Void extends ExcelDataConverter{

		@Override
		public Object convertFromString(String str) {
			return null;
		}

		@Override
		public String convertToString(Object t) {
			return null;
		}
		
	}
}
