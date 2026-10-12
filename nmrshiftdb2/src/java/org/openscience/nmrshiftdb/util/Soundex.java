/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * Copyright 2010 Stefan Kuhn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openscience.nmrshiftdb.util;

/**
 * Find the Soundex of a string. This is an adapted version with it's own soundex rules.
 * This code is taken from jakarta-taglibs who took it from the codesnippers at sourceforge.
 * See http://svn.apache.org/repos/asf/jakarta/taglibs/proper/string/trunk/src/main/java/org/apache/taglibs/string/util/Soundex.java
 */
public class Soundex {

	private final static char[] US_ENGLISH_MAPPING ="01230120022455012623010202".toCharArray();

	private static char[] soundexMapping=US_ENGLISH_MAPPING;

	/**
	 * Gives the soundex of a letter (figures are coded as themselves)
	 *
	 * @param  c  The letter
	 * @return    The soundex
	 */
	public static char getMappingCode(char c) {
		if (Character.isDigit(c)) {
			return (c);
		}
		if (!Character.isLetter(c)) {
			return '0';
		} else {
			if(Character.toUpperCase(c) - 'A'>=soundexMapping.length || Character.toUpperCase(c) - 'A' < 0)
				return '0';
			else
				return soundexMapping[Character.toUpperCase(c) - 'A'];
		}
	}


	/**
	 * Get the SoundEx value of a string. This is not the canonical soundex implementation,
	 * but my own one (the first letter is coded as well, length is unlimited, figures are coded)
	 * The base of this implementation is taken from the code-snippers on
	 * http://www.sourceforge.net/
	 *
	 * @param  str              The String the soundex should be calculated from
	 * @param  whereIs          The soundex code the position of which should be established
	 * @param  positionInteger  The myInt field contains the position of whereIS
	 * @return                  The soundex
	 */
	public static String soundex(String str, String whereIs, StringAndInt positionInteger) {
		char[] whereIsChar = whereIs.toCharArray();
		StringBuffer out = new StringBuffer();
		char last= '9';//If the start value would be something the first letter could be the first letter would be missed
		char mapped;
		int incount = 0;
		int startPosition = 0;
		int currentWhereIsPosition = 0;//Says which code of the whereIs String is currently compared
		while ((incount < str.length()) && (mapped = getMappingCode(str.charAt(incount++))) != -1) {
			if ((mapped != '0') && (mapped != last)) {
				out.append(last = mapped);
				//This means the currentWhereIsPositons th code of the whereIsString fits with the current soundex code 
				if (currentWhereIsPosition < whereIsChar.length && mapped == whereIsChar[currentWhereIsPosition]) {
					if (currentWhereIsPosition == 0) {
						//The first code of whereIs fits means we remember the current positon in str as starting position
						startPosition = incount;
					}
					if (currentWhereIsPosition == whereIsChar.length - 1) {
						//The last code of whereIs fits means the complete whereIs is in str starting at startPosition, so we remember this
						positionInteger.myInt = startPosition;
					}
					currentWhereIsPosition++;
				} else {
					currentWhereIsPosition = 0;
				}
			}
		}
		return new String(out);
	}

}

