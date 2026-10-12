package org.openscience.nmrshiftdb.util.comparator;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierComparator implements Comparator<String> {

	@Override
	public int compare(String arg0, String arg1) {
        String text1=arg0;
        int intpart1=-1;
        String rest1="";
        String text2=arg1;
        int intpart2=-1;
        String rest2="";
        String patternString = "^[0-9]+";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
	    if(isInteger(arg0)){
	        intpart1=Integer.parseInt(arg0);
	    }else {
	        Matcher matcher = pattern.matcher(text1);
	        if(matcher.find()) {
	        	intpart1=Integer.parseInt(text1.substring(0,matcher.end()));
	            rest1=text1.substring(matcher.end());
	        }
	    }
	    if(isInteger(arg1)){
	        intpart2=Integer.parseInt(arg1);
	    }else{
	    	Matcher matcher = pattern.matcher(text2);
	        if(matcher.find()) {
	        	intpart2=Integer.parseInt(text2.substring(0,matcher.end()));
	            rest2=text2.substring(matcher.end());
	        }
	    }
        if(rest2.startsWith("'") ^ rest1.startsWith("'")){
        	if(rest1.startsWith("'"))
                    return 1;				        
        	else
                    return -1;				        
        }else{
        	if(intpart1==intpart2)
        		if(rest1.isEmpty())
        			return -1;
        		else if(rest2.isEmpty())
        			return 1;
        		else
		            return(rest1.compareTo(rest2));
        	else
                if(intpart1>intpart2){
                        return 1;
                }else if(intpart1<intpart2){
                        return -1;
                }else{
                    return 0;
                }
        }
	}
	
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}