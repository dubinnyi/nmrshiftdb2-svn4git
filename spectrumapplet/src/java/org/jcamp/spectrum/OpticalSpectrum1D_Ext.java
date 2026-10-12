package org.jcamp.spectrum;

import org.jcamp.math.*;

import org.jcamp.units.*;

/**
 * base class for all optical spectrum types with 1 independent variable.
 * extended Constructor added (Dr. Guenter Schneider)
 * @author Thomas Weber
 */
public abstract class OpticalSpectrum1D_Ext extends Spectrum1D {

    /**
     * default ctor.
     *
     * @param x org.jcamp.spectrum.IOrderedDataArray1D
     * @param y org.jcamp.spectrum.IDataArray1D
     */
    protected OpticalSpectrum1D_Ext(IOrderedDataArray1D x, IDataArray1D y) {
        super(x, y);
    }


    /**
     * default ctor.
     */
    protected OpticalSpectrum1D_Ext() {
        super();
    }

    /**
     * cloning.
     *
     * @return java.lang.Object
     */
    public Object clone() {
        OpticalSpectrum1D spectrum = (OpticalSpectrum1D) super.clone();
        spectrum.xAxisMap = new LinearAxisMap(spectrum.xData);
        spectrum.yAxisMap = new LinearAxisMap(spectrum.yData);
        return spectrum;
    }


    /**
     * convert transmittance data to absorbance
     */
    public void convertToAbsorbance() {
        IDataArray1D newY = convertToAbsorbance(yData);
        if (newY != yData) {
            setData(xData, newY);
            newY.setLabel(null); // force automatic y axis labeling
            adjustFullViewRange();
        }
    }


    /**
     * convert intensities to percent
     */
    public void convertToPercent() {
        IDataArray1D newY = convertToPercent(yData);
        if (newY != yData) {
            setData(xData, newY);
            newY.setLabel(null); // force automatic y axis labeling
            adjustFullViewRange();
        }
    }


    /**
     * convert absorbance to transmittance
     */
    public void convertToTransmittance() {
        IDataArray1D newY = convertToTransmittance(yData);
        if (newY != yData) {
            setData(xData, newY);
            newY.setLabel(null); // force automatic y axis labeling
            adjustFullViewRange();
        }
    }


    /**
     * gets spectrum ID.
     * @return int
     */
    public abstract int getIdentifier();


    /**
     * isSameType method comment.
     */
    public abstract boolean isSameType(Spectrum otherSpectrum);


    /**
     * heuristical check for transmission spectrum
     * @return boolean
     */
    public boolean isTransmission() {
        Unit yunit = yData.getUnit();
        if (yunit.equals(CommonUnit.percentTransmittance) || yunit.equals(CommonUnit.transmittance)) {
            return true;
        }
        if (yunit.equals(CommonUnit.percentAbsorbance) || yunit.equals(CommonUnit.absorbance)) {
            return false;
        }
        return isTransmission(yData);
    }


    /**
     * simple test if spectrum is a transmission spectrum.
     * @return boolean
     */
    private static boolean isTransmission(IArray1D data) {
        Range1D.Double range = data.getRange1D();
        double yhigh         = range.getXMax() - 0.1 * range.getXWidth();
        double ylow          = range.getXMin() + 0.1 * range.getXWidth();
        int nlow             = 0;
        int nhigh            = 0;
        for (int i = 0; i < data.getLength(); i++) {
            double y = data.pointAt(i);
            if (y > yhigh) {
                nhigh++;
            }
            else if (y < ylow) {
                nlow++;
            }
        }
        return (nhigh > nlow);
    }

    /**
     * default ctor.
     *
     * @param x org.jcamp.spectrum.IOrderedDataArray1D
     * @param y org.jcamp.spectrum.IDataArray1D
     * @param fullSpectrum boolean
    */
    protected OpticalSpectrum1D_Ext(IOrderedDataArray1D x, IDataArray1D y, boolean fullSpectrum) {
        super(x, y, fullSpectrum);
        if (isTransmission()) {
            convertToAbsorbance();
            adjustFullViewRange();
        }
    }


    /**
     * default ctor.
     *
     * @param x org.jcamp.spectrum.IOrderedDataArray1D
     * @param y org.jcamp.spectrum.IDataArray1D
     * @param fullSpectrum boolean
     * @param convertToAbsorbance if TRUE: conversion allowed
    */
    protected OpticalSpectrum1D_Ext(IOrderedDataArray1D x, IDataArray1D y, boolean fullSpectrum,
                                    boolean convertToAbsorbance) {
        super(x, y, fullSpectrum);
        if (convertToAbsorbance && isTransmission()) {
            convertToAbsorbance();
            adjustFullViewRange();
        }
    }

    private static IDataArray1D convertToAbsorbance(IDataArray1D data) {
        int n      = data.getLength();
        double ref;
        if (data.getUnit().equals(CommonUnit.percentTransmittance)) {
            ref = 100.;
        }
        else if (data.getUnit().equals(CommonUnit.transmittance)) {
            ref = 1.;
        }
        else {
            return data;
        }
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double t = data.pointAt(i);
            if (t < 0) {
                t = 0;
            }
            if (t > ref) {
                t = ref;
            }
            if (t > 0) {
                y[i] = Math.max(0., Math.log(ref / t) / Math.log(10.));
            }
            else {
                y[i] = 1;
            }
        }
        return new ArrayData(new Array1D(y, false), CommonUnit.absorbance);
    }


    /**
     * convertToAbsorbance method comment.
     */
    private static IDataArray1D convertToPercent(IDataArray1D data) {
        int n = data.getLength();
        if (data.getUnit().equals(CommonUnit.transmittance)) {
            double max = data.getRange1D().getXMax();
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                y[i] = 100.0 * (data.pointAt(i) / max);
            }
            return new ArrayData(new Array1D(y, false), CommonUnit.percentTransmittance);
        }
        return data;
    }


    /**
     * convertToAbsorbance method comment.
     */
    private static IDataArray1D convertToTransmittance(IDataArray1D data) {
        int n = data.getLength();
        if (data.getUnit().equals(CommonUnit.absorbance)) {
            double[] y = new double[n];
            for (int i = 0; i < n; i++) {
                double a = data.pointAt(i);
                if (a > 1) {
                    a = 1;
                }
                if (a < 0) {
                    a = 0;
                }
                y[i] = 100.0 / Math.pow(10, a);
            }
            return new ArrayData(new Array1D(y, false), CommonUnit.percentTransmittance);
        }
        else {
            return data;
        }
    }
}
