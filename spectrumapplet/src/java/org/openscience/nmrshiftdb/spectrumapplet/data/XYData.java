/*
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 *
 * Created on 10. Februar 2005
 */
package org.openscience.nmrshiftdb.spectrumapplet.data;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;

import java.util.*;

/**
 *
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class XYData extends AbstractDataContainer {
    private SpectrumPeak[] peaks;
    private double[] xValues;
    private double[] yValues;
    private double yMin;
    private double yMax;

    public void addPeaks(ArrayList peakList, double factorX) {
        peaks=null;
        if ((peakList != null) && (peakList.size() > 0)) {
            peaks = new SpectrumPeak[peakList.size()];
            for (int i = 0; i < peakList.size(); i++) {
                peaks[i] = (SpectrumPeak) peakList.get(i);
                peaks[i].setValueX(peaks[i].getValueX() * factorX);
            }
            Arrays.sort(peaks);
        }
    }


    public void addCoordinates(double[] xVals, double[] yVals, double factorX) {
        if (xVals.length != yVals.length) {
            System.out.println("different length");
        }
        int z = xVals.length;
        yMin    = yVals[0];
        yMax    = yMin;
        xValues = new double[z];
        yValues = new double[z];
        if (xVals[0] <= xVals[z - 1]) {
            for (int i = 0; i < z; i++) {
                xValues[i] = xVals[i] * factorX;
                yValues[i] = yVals[i];
                if (yVals[i] < yMin) {
                    yMin = yVals[i];
                }
                else if (yVals[i] > yMax) {
                    yMax = yVals[i];
                }
            }
        }
        else {
            // invers sorted data
            z--;
            for (int i = 0; i < xVals.length; i++) {
                xValues[z] = xVals[i] * factorX;
                yValues[z] = yVals[i];
                z--;
                if (yVals[i] < yMin) {
                    yMin = yVals[i];
                }
                else if (yVals[i] > yMax) {
                    yMax = yVals[i];
                }
            }
        }
    }


    public Object[] getPeaks() {
        return peaks;
    }


    /**
    * Return the minimal x-value e.g. minimal chemical shift in nmr spectrum
    * @return the minimal x-value
    */
    public double getXmin() {
        return xValues[0];
    }


    /**
     * Return the maximal x-value e.g. maximal chemical shift in nmr spectrum
     * @return the maximal x-value
     */
    public double getXmax() {
        return xValues[xValues.length - 1];
    }


    /**
    * Return the maximal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the maximal intensity
    */
    public double getMaxIntensity(boolean signed) {
        return yMax;
    }


    /**
    * Return the minimal intensity in this spectrum
    * @param signed if TRUE, the peak orientation is used
    * @return the minimal intensity
    */
    public double getMinIntensity(boolean signed) {
        return yMin;
    }


    public int getIndexOfClosestPoint(double xValue, boolean inversOrder) {
        int r =0;
        if (inversOrder ){
           r= getIndexOfClosestPoint(xValue, xValues.length-1,  inversOrder);
        }
        else {
            r=getIndexOfClosestPoint(xValue, 0,  inversOrder);
        }
        return r;
    }


    public int getIndexOfClosestPoint(double xValue, int startIndex, boolean inversOrder) {
        int z = startIndex;
        if (inversOrder) {
            for (int i = startIndex -1; i >= 0; i--) {
                if (xValues[i] < xValue) {
                    if (Math.abs(xValue - xValues[z]) > Math.abs(xValue - xValues[i])) {
                        z--;
                    }
                    break;
                }
                else {
                    z--;
                }
            }
        }
        else {
            for (int i = startIndex + 1; i < xValues.length; i++) {
                if (xValues[i] > xValue) {
                    if (Math.abs(xValue - xValues[z]) < Math.abs(xValue - xValues[i])) {
                        z++;
                    }
                    break;
                }
                else {
                    z++;
                }
            }
        }
        return z;
    }


    public int[][] getValuesBetween(double xMin, double xMax, Metrics metrics, double yZoomFactor) {
        double min=Double.MAX_VALUE;
        double max=Double.MIN_VALUE;
        double actual      = xMin;
        int start          = metrics.getXoffsetLeft() + 1;
        int end            = metrics.getSize().width - metrics.getXoffsetRight() - 1;
        int numberOfPoints = end - start;
        double delta  = (xMax - xMin) / numberOfPoints;
        int[] xVals   = new int[numberOfPoints];
        int[] yVals   = new int[numberOfPoints];
        double[] temp = new double[numberOfPoints];
        int pos       = xValues.length-1;
        for (int i = 0; i < numberOfPoints; i++) {
            xVals[i] = i + start;
            actual   = metrics.translateMouseX(xVals[i]);
            pos     = getIndexOfClosestPoint(actual, pos,true);
            temp[i] = searchMaxima(pos, delta);
           // System.out.println("i=" + i + "    actual=" + actual + "  pos=" + pos);

            min = Math.min(min, temp[i]);
            max = Math.max(max, temp[i]);
        }


        metrics.setLimitsY(min/yZoomFactor, max/yZoomFactor, false);
        for (int i = 0; i < numberOfPoints; i++) {
            // for NMR flip orientation!!!
            yVals[i] = metrics.translateY_int(temp[i]);
            
        }
        return new int[][] { xVals, yVals };
    }


    private double searchMaxima(int index, double delta) {
        double end = xValues[index] + delta / 2;
        double min = yValues[index];
        double max = yValues[index];
        int z      = index + 1;
        int l      = xValues.length;
        int zmax   = z;
        while ((l > z) && (xValues[z] < end)) {
            if (yValues[z] > max) {
                max  = yValues[z];
                zmax = z;
            }
            else if (yValues[z] < min) {
                min = yValues[z];
            }
            z++;
        }
        z   = index - 1;
        end = xValues[index] - delta / 2;
        while ((z >= 0) && (end < xValues[z])) {
            if (yValues[z] > max) {
                max  = yValues[z];
                zmax = z;
            }
            else if (yValues[z] < min) {
                min = yValues[z];
            }
            z--;
        }
        double r = max;

        //System.out.println("min= "+min+"  max= "+max+"   actual: "+yValues[index]);
        if (Math.abs(yValues[index] - min) > Math.abs(yValues[index] - max)) {
            // r=min;
        }
        if (r > 2) {
            // System.out.println("maxIndex= "+zmax+"    index:"+index);
        }

        //System.out.println("     pos= "+xValues[index]+"  return= "+r);
        return r;
    }


    public AbstractDataContainer clone(boolean option) {
        XYData r = new XYData();
        cloneData(r);
        return r;
    }
    
    public void cloneData(AbstractDataContainer r){
        r.setSpectrumProperties(this.cloneProperties());
        if (getPeaks() != null) {
            ArrayList l = new ArrayList(peaks.length);
            for (int i = 0; i < peaks.length; i++) {
                l.add(peaks[i].clone()); 
            }
            ((XYData)r).addPeaks(l, 1); 
        }
        ((XYData)r).addCoordinates(xValues, yValues, 1); 
        
    }
}
