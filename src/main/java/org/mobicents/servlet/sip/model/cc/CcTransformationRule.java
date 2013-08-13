package org.mobicents.servlet.sip.model.cc;

import java.util.Comparator;

class CcTransformationRule implements Comparable<CcTransformationRule> {

	public int transformNumber;
	public boolean transformEnabled;
	public String transformType;
	public String transformSrcString;
	public String transformDestString;
	public String transformApply;
	public boolean transformBlock;


	/**
	 * 
	 * @param num
	 * @param pri
	 */
	public CcTransformationRule(int num) {
		transformNumber = num;
	}

	/**
	 * 
	 * @param num
	 * @param pri
	 * @param trunk
	 * @param port
	 * @param transport
	 */

	public CcTransformationRule(int num,boolean enabled,String type, String srcNumber, String dstNumber,String apply, boolean block) {
		transformNumber = num;
		transformEnabled = enabled;
		transformType = type;
		transformSrcString = srcNumber;
		transformDestString = dstNumber;
		transformApply = apply;
		transformBlock = block;

	}

	public boolean isRuleEnabled() {
		return transformEnabled;
	}

	public String getTransformType() {
		return transformType;
	}

	public String getTransformSrcString() {
		return transformSrcString;
	}

	public String getTransformDestString() {
		return transformDestString;
	}

	public String getTransformApply() {
		return transformApply;
	}

	public boolean isTransformBlock() {
		return transformBlock;
	}

	public int getRuleNumber() {
		return transformNumber;
	}

	
	
	public int compareTo(CcTransformationRule uno) {
		int compareRuleValue = ((CcTransformationRule) uno).getRuleNumber();
		// Ascending order
		return this.transformNumber - compareRuleValue;
	}

	@Override
	public String toString() {
		return "rule id=" + this.transformNumber;
	}

	public static Comparator<CcTransformationRule> RuleNumberComparator = new Comparator<CcTransformationRule>() {

		public int compare(CcTransformationRule e1, CcTransformationRule e2) {
			return (int) (e1.getRuleNumber() - e2.getRuleNumber());
		}
	};

	
}
