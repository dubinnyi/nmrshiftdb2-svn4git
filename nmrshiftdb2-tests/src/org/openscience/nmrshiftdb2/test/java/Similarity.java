/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2011,2014 Stefan Kuhn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openscience.nmrshiftdb2.test.java;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.nmrshiftdb.util.GeneralUtils;
import org.openscience.nmrshiftdb.util.ParseUtils;
import org.xmlcml.cml.element.CMLPeak;

public class Similarity{
	
	@Test
	public void testSimilarityCalculation1(){
		List<CMLPeak> spectrum1 = ParseUtils.parseSpectrumFromSpecFile("128.5;0.0D;0|128.5;0.0D;1|128.5;0.0D;2|128.5;0.0D;3|128.5;0.0D;4|128.5;0.0D;5|");
		List<CMLPeak> searchSpectrum = ParseUtils.parseSpectrumFromSpecFile("128;0D");
		double sim=GeneralUtils.similarity(spectrum1,searchSpectrum,false);
		Assert.assertEquals(99.5,sim,0.1);
	}

	@Test
	public void testSimilarityCalculation2(){
		List<CMLPeak> spectrum1 = ParseUtils.parseSpectrumFromSpecFile("129.478;0.0;8|");
		List<CMLPeak> searchSpectrum = ParseUtils.parseSpectrumFromSpecFile("128;0D");
		double sim=GeneralUtils.similarity(spectrum1,searchSpectrum,false);
		Assert.assertEquals(0,sim,0.1);
	}

	@Test
	public void testSimilarityCalculation3(){
        List<CMLPeak> spectrum1 = ParseUtils.parseSpectrumFromSpecFile("128.5;0.0D;0|35;0.0D;1|");
        List<CMLPeak> searchSpectrum = ParseUtils.parseSpectrumFromSpecFile("128;0D|35;0");
        double sim=GeneralUtils.similarity(spectrum1,searchSpectrum,false);
        Assert.assertEquals(99.5,sim,0.1);
    }
	
	@Test
    public void testSimilarityCalculation4(){
        List<CMLPeak> spectrum1 = ParseUtils.parseSpectrumFromSpecFile("128;0D;0|35;0;1|");
        List<CMLPeak> searchSpectrum = ParseUtils.parseSpectrumFromSpecFile("128.5;0.0D|35;0.0D");
        double sim=GeneralUtils.similarity(spectrum1,searchSpectrum,false);
        Assert.assertEquals(49.5,sim,0.1);
    }
}
