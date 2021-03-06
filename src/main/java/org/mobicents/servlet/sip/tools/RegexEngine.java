package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class RegexEngine {
	
	static private char EXCLAMATION = '!';
	static private char STAR =  '*';
	static private char POUND = '#';
	static private char PLUS =  '+';
	static private char DOT =  '.';
	static private char DASH =  '-';
	
	private String REGEX;
	private String PATTERN;
	private int[] FAILURE;
	private int MATCHPOINT;
	private static ArrayList<RegexRule> regexRules = new ArrayList<RegexRule>(); 
	
	private static Logger logger = Logger.getLogger(RegexEngine.class);
	
	public RegexEngine() {
		
	}
	
	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 * @param string
	 * @param pattern
	 */
	
	public void KMPInit(String string, String pattern) {
		
		    this.REGEX = string;
		    this.PATTERN = pattern;
		    FAILURE = new int[pattern.length()];
		    KMPcomputeFailure();
	}
		      
	public void setRegex(String regex) {
		REGEX = regex;
	}

	public void setPattern(String pattern) {
		PATTERN = pattern;
	}
	
	public int KMPgetMatchPoint() {
		return MATCHPOINT;
	}
	  
	
	/**
	 *  Tries to find an occurrence of the pattern in the string
	 * @return
	 */
	
	private boolean KMPmatch() {
	
		    
		    int j = 0;
		    if (REGEX.length() == 0) return false;
		    
		    for (int i = 0; i < REGEX.length(); i++) {
		      while (j > 0 && PATTERN.charAt(j) != REGEX.charAt(i)) {
		        j = FAILURE[j - 1];
		      }
		      if (PATTERN.charAt(j) == REGEX.charAt(i)) { j++; }
		      if (j == PATTERN.length()) {
		        MATCHPOINT = i - PATTERN.length() + 1;
		        return true;
		      }
		    }
		    return false;
	}
		  
		  
	/** 
	  * Computes the FAILURE function using a boot-strapping process,
      * where the pattern is matched against itself.
	  */
	private void KMPcomputeFailure() {

		    int j = 0;
		    for (int i = 1; i < PATTERN.length(); i++) {
		      while (j > 0 && PATTERN.charAt(j) != PATTERN.charAt(i)) { j = FAILURE[j - 1]; }
		      	if (PATTERN.charAt(j) == PATTERN.charAt(i)) { j++; }
		      	FAILURE[i] = j;
		    }
	}
		  
	/**
	 * 
	 * @param prototype
	 * @return
	 */
	
	public static Pattern generateRegexHelper(String prototype) {
		
        return Pattern.compile(generateRegexpEngine(prototype));
    
	}
	
	/**
	 * Create Regex expression based on input
	 * @param prototype
	 * @return
	 */
	
	private static String generateRegexpEngine(String prototype) {

		StringBuilder regexPrototype = new StringBuilder();
		
        for (int i = 0; i < prototype.length(); i++) {
        	
            char c = prototype.charAt(i);
            if (Character.isDigit(c)) {
            	regexPrototype.append(c);
            } else if (c==EXCLAMATION) {
            	regexPrototype.append("(.*)");
            } else if (c==STAR) {
            	regexPrototype.append("\\*");
            } else if (c==POUND) {
            	regexPrototype.append("\\#");
            } else if (c==PLUS) {
            	regexPrototype.append("\\+");
            } else if (c==DOT) {
            	regexPrototype.append("\\.");
            } else if (c==DASH) {
            	regexPrototype.append("\\-");
            } else if (c=='X' || c=='x') {
            	regexPrototype.append("\\d"); 
            } else { 
            	 logger.error("Unknown character: " +  c);
            	 return null;
                 
            }
        }

        if(validateRule(regexPrototype.toString()))
        	return regexPrototype.toString();
        else
        	return null;
	        
    }
	
	/**
	 * Verifies if Regex contains invalid characters (more than one +)
	 * @param regexPrototype
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean validateRule(String regexPrototype) {
		
		String plus = "\\+";
		String all = "(.*)";
		try {
				
			Pattern srcP = Pattern.compile(regexPrototype);
			Matcher  matcher = srcP.matcher(plus);
			int count = 0;
	        while (matcher.find())
	            count++;
			
			if(StringUtils.countMatches(regexPrototype, plus) > 1) {
				logger.error("validateRule() Invalid Regex: " + regexPrototype );
				return false;
			}
			else if(StringUtils.countMatches(regexPrototype, all)>1) {
				logger.error("validateRule() Invalid Regex: " + regexPrototype );
				return false;
			}
			else {
				return true;
			}
			
		} catch (PatternSyntaxException ex) {
	    	ex.printStackTrace();
	    	logger.error("validateRule() Syntax error in the regular expression");
	    	return false;
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			logger.error("validateRule() Syntax error in the replacement text (unescaped $ signs?)");
			return false;
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
			logger.error("validateRule() Non-existent backreference used the replacement text");
			return false;
		}
				
			
	}
	
	/**
	 * Groups digit common patterns: \d\d\d\d\d\d\d\d\d\d\d = ((\d){11})
	 * @param regexPrototype
	 * @return
	 */
	
	private static String generateSimpleRegexGroup(String regexPrototype) {
		
		String digits = "\\d";
		String all    = "(.*)";
		StringBuilder simplifiedRegex = new StringBuilder();
	    List<RegexGroup> regexGroupObjectList = new ArrayList<RegexGroup>();
		List<Integer> regexGroupElementsList = new ArrayList<Integer>();
		
		int lastIndex = 0;
    	int count = 0;
    	int groupId = 0;
    	boolean containsAll = false;
    	RegexRule newRule = new RegexRule();
		
		
    	if(!validateRule(regexPrototype) || regexPrototype.length() <= 0) {
    		return null;
    	}
    	
    	/**
    	 * Call Knuth-Morris-Pratt algorithm
    	 */
    	
    	if(StringUtils.countMatches(regexPrototype, all) > 0) {
    		
    		RegexEngine matcherAll = new RegexEngine();
        	matcherAll.KMPInit(regexPrototype,all);
        	
        	if (matcherAll.KMPmatch()) {
        		logger.info("generateSimpleRegexGroup() Knuth-Morris-Pratt() Match! String: " + regexPrototype + " Index match(ALL): " + matcherAll.KMPgetMatchPoint());
        	}
        	
        	containsAll = true;
        	
        	if(StringUtils.countMatches(regexPrototype, digits) == 0) {
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(regexPrototype);
        		newRule.setNumberOfGroups(1);
        		newRule.regexGroupsItems.add("(.*)");
        		regexRules.add(newRule);
        		
    		}
    		     	
    	}
    
    	
    	if(StringUtils.countMatches(regexPrototype, digits)>0) {
    		
    		
    		
    		/**
    		 * Find index of match digits and stores in Main Array
    		 */
    		
				while (lastIndex != -1) {			
					lastIndex = regexPrototype.indexOf(digits, lastIndex);
					if (lastIndex != -1) {
						regexGroupElementsList.add(lastIndex);
						lastIndex += digits.length();
						count++;	
					}
				}
		
				/**
				 * Find number of groups
				 * Create Object
				 * Process String
				 * 	
				 */
				for (int i=0;i<regexGroupElementsList.size();i++) {		
					
					if(i+1 < regexGroupElementsList.size()) {
						if(regexGroupElementsList.get(i) == regexGroupElementsList.get(i+1) - digits.length()) {	
							if(groupId==0) {
								//logger.infof("New Regex Group found index: %d\n",regexGroupElementsList.get(i));
								regexGroupObjectList.add(new RegexGroup(1));
								regexGroupObjectList.get(0).processElements(1, regexGroupElementsList);	
								regexGroupObjectList.get(0).setIndexStart(regexGroupElementsList.get(i));
								groupId++;
							}
						}
						else {
							//logger.infof("New Regex Group found index: %d\n",regexGroupElementsList.get(i+1));
							regexGroupObjectList.add(new RegexGroup(groupId+1));
							regexGroupObjectList.get(groupId).processElements(groupId+1, regexGroupElementsList);
							regexGroupObjectList.get(groupId).setIndexStart(regexGroupElementsList.get(i+1));
							groupId++;
						}
					} 
					else {			
						// Only 1 element
						if(regexGroupObjectList.size()==0) {
							regexGroupObjectList.add(new RegexGroup(1));
							regexGroupObjectList.get(0).processElements(1, regexGroupElementsList);	
							regexGroupObjectList.get(0).setIndexStart(regexGroupElementsList.get(i));
						}	
					}
				}
	
						logger.info("generateSimpleRegexGroup() Pattern found: " + count + " time(s). Regex Groups: " + regexGroupObjectList.size() + " " + " All elements: " + regexGroupElementsList); 	
				   
					
						/**
						 * Convert Original string to New String
						 */
						int obj = 0;
						int ptr = 0;
		
						while(ptr < regexPrototype.length()) {
						
							if(ptr == regexGroupObjectList.get(obj).getIndexStart()) {
								simplifiedRegex.append("((\\d){" + regexGroupObjectList.get(obj).getElements() + "})");
								newRule.regexGroupsItems.add("((\\d){" + regexGroupObjectList.get(obj).getElements() + "})");
								ptr += regexGroupObjectList.get(obj).getOffset();
								
								if(regexGroupObjectList.size()>1 && obj+1 < regexGroupObjectList.size())
									obj++;
								
							}
							else {
								simplifiedRegex.append(regexPrototype.charAt(ptr));
								ptr++;
							}
						}
			
						simplifiedRegex.insert(0,"^");
						simplifiedRegex.insert(simplifiedRegex.length(),"$");
    	} 
    	else 
    	{
    		return null;
    	}
    	
    	
    	if(validateRule(simplifiedRegex.toString())) {
    		logger.info("generateSimpleRegexGroup() Original Regex: " + regexPrototype + " New Simplified Regex: " + simplifiedRegex);
    	
    		if (containsAll) {		
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(simplifiedRegex.toString());
        		newRule.setNumberOfGroups(regexGroupObjectList.size()+1);
        		newRule.regexGroupsItems.add("(.*)");
        		regexRules.add(newRule);
    		}
    		else {
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(simplifiedRegex.toString());
        		newRule.setNumberOfGroups(regexGroupObjectList.size());
        		regexRules.add(newRule);
    		}
    		
    		return simplifiedRegex.toString();
    	}	
    	else
    		return null;
	
	   	
	}
	
	
	private static boolean compareRegexRules(RegexRule src,RegexRule dst) {
			
 		logger.info("Comparing: " + src.getRuleInfo() + " and " + dst.getRuleInfo());       		  
 	   
		if (src.getNumberOfGroups()==dst.getNumberOfGroups()) {
				if(src.regexGroupsItems.get(src.getNumberOfGroups()-1).contains(dst.regexGroupsItems.get(src.getNumberOfGroups()-1))) {
					logger.info("compareRegexRules() Same rule group:" + src.regexGroupsItems.get(src.getNumberOfGroups()-1) );
					logger.info("compareRegexRules() Regex Src: " + src.getRuleInfo());
					logger.info("compareRegexRules() Regex Dst: " + dst.getRuleInfo());
					return true;
				}
				else {
					return false;
				}

		}
		else {
			return false;
				
		}
		
	}
	
	
	/**
	 * 
	 * @param input
	 */
	private static void testRegexRule(String input) {
		
		try {
			Pattern pattern = generateRegexHelper(input);
			logger.info(String.format("testRegexRule() String: %s --> Regex value: %s", input, pattern));
			generateSimpleRegexGroup(pattern.toString());
		}
		catch(Exception e) {	
			e.printStackTrace();
			logger.error("testRegexRule() Invalid input for Wildcard: " + input);
		}
        
    }
	
	public String processRegexRules(String regexSrc,String regexDst,String callInformation) {
		
		try {
			Pattern original = Pattern.compile(regexSrc);
		    Matcher matcher = original.matcher(callInformation);
		    
		    RegexTestResult result = new RegexTestResult();
		    result.setText(callInformation);
		    result.setMatches(matcher.matches());
		    matcher.reset();
		    result.setReplacedText(matcher.replaceAll(regexDst));
		    
		    logger.info("processRegexRules() Original text: " + result.getText());
		    
		    /**
		     * while (matcher.find()) {  
		        String matchText = matcher.group();
		        int start = matcher.start();
		        int end = matcher.end();
		        result.addGroup(new Group(matchText, start, end));
		    }
		     */
		    //logger.info("processRegexRules() Replaced text: " + result.getReplacedText());
		    return result.getReplacedText();
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("processRegexRules() Error: " + e.getMessage());
			return null;
		}
		
	    
	   
	    
	}
	
	
	public String processWildCardRules(String regexSrc,String regexDst,String callInformation) {
		/**
		 * Example: TRANSFORM=("2","TRUE","WILDCARD","XXXXXXXX","18668643232**XXXXXXXX","CALLED","FALSE")
		 * 			22223333 Match RULE XXXXXXXX
		 * 			XXXXXXXX is converted to \d\d\d\d\d\d\d\d
		 * 			\d\d\d\d\d\d\d\d is simplified to ((\d){8})
		 * 			(\d){8} is matched against WILDCARD RULE:
		 * 			18668643232**XXXXXXXX
		 * 			18668643232**XXXXXXXX is converted to 18668643232\*\*\d\d\d\d\d\d\d\d
		 * 			
		 */
		
		 String[] prototypes = {
		 		  regexSrc,
				  regexDst
	     };

		  for (String prototype : prototypes) {
	        	testRegexRule(prototype);
	      }
		  
		  for (int i=0;i<regexRules.size();i++) {
	        	//logger.info(regexRules.get(i).getRuleInfo());
	        	//regexRules.get(i).displayGroups();
	        	if(i+1 < regexRules.size()) {
	        		
	        		if(compareRegexRules(regexRules.get(i),regexRules.get(i+1))) {
	        			logger.info("processWildCardRules() Rules: " + regexRules.get(i).getRuleValue() + " " + regexRules.get(i+1).getRuleValue());
	        		
	        			try {
	        				logger.info("processWildCardRules() Call info: " + callInformation);
	        				
	        				if (callInformation.matches(regexRules.get(i).getRuleValue())) {
								logger.info("processWildCardRules() Input: " + callInformation + " Match rule:" + regexRules.get(i).getRuleValue());
								logger.info("processWildCardRules() Rule: " + regexRules.get(i).getSimplifiedRuleValue());
								//logger.info(regexRules.get(i).getGroup(0));
								
								Pattern original = Pattern.compile(regexRules.get(i).getSimplifiedRuleValue());
							    Matcher matcher = original.matcher(callInformation);
							    String firstConversion = "";
							    
							    // Check all occurrences
							    if (matcher.find()) {
							    
						//	      logger.info("processWildCardRules() Input: " + callInformation);	
						//	      logger.info("processWildCardRules() Regex Src: " + regexRules.get(i).getSimplifiedRuleValue());
						//	      logger.info("processWildCardRules() Start index: " + matcher.start(1));
						//	      logger.info("RegexEngine() End index: " + matcher.end(1) + " ");
						//	      logger.info("processWildCardRules() String Match: " + matcher.group(1));
							      firstConversion =  matcher.group(1).toString();
							    }
							    else {
							    	return null;
							    }
							    
							    logger.info("processWildCardRules() Extracted value: " + firstConversion);
								logger.info("processWildCardRules() Regex Dst " + regexRules.get(i+1).getSimplifiedRuleValue());
								
							    int substringIndex = regexRules.get(i+1).getSimplifiedRuleValue().indexOf(regexRules.get(i).getGroup(0));
							    
							    if (substringIndex >=0 ) {
							    	 String finalMatch = regexRules.get(i+1).getSimplifiedRuleValue().substring(0,substringIndex);
									 finalMatch = finalMatch.replaceAll("\\^", "");
									 finalMatch = finalMatch + firstConversion;
									 logger.info("processWildCardRules() Final transform number: " + finalMatch);
									 return finalMatch;
							    }
							    else {
							    	return null;
							    }
							   
	        				}
						}
						catch(Exception e) {
							return null;
						}
	        		
	        		}
	        		
	        	}
	        }
		  
		return null;  
		
	}
	
	
		 
	        
	//logger.info("Processed string: " + processWildCardRules("XXXXXXXX","18668643232**XXXXXXXX","22223333"));
	        
	
	 
}
