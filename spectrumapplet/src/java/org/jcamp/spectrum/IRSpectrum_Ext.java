package org.jcamp.spectrum;

import org.jcamp.math.*;

import org.jcamp.units.*;

/**
 * IR spectra.
 * @author Thomas Weber
 */
public class IRSpectrum_Ext extends OpticalSpectrum1D_Ext {
    private final static double[] DEFAULT_WAVENUMBERS  = new double[] {
                                                             4000.0, 3500.0, 3000.0, 2500.0, 2000.0, 1800,
                                                             1600, 1400, 1200, 1000, 800, 600, 400
                                                         };
    private final static IrregularGrid1D DEFAULT_XGRID = new IrregularGrid1D(DEFAULT_WAVENUMBERS);
    public final static Unit DEFAULT_XUNIT             = CommonUnit.perCM;
    public final static Unit DEFAULT_YUNIT             = CommonUnit.intensity;

    /**
     */
    protected IRSpectrum_Ext() {
        super();
    }


    /**
     * standard ctor.
     *
     * @param x org.jcamp.spectrum.IOrderedDataArray1D
     * @param y org.jcamp.spectrum.IDataArray1D
     */
    public IRSpectrum_Ext(IOrderedDataArray1D x, IDataArray1D y) {
        super(x, y);
    }


    /**
     * standard ctor.
     *
     * @param x org.jcamp.spectrum.IOrderedDataArray1D
     * @param y org.jcamp.spectrum.IDataArray1D
     * @param fullSpectrum boolean
     */
    public IRSpectrum_Ext(IOrderedDataArray1D x, IDataArray1D y, boolean fullSpectrum) {
        super(x, y, fullSpectrum, false);
        xAxisMap = new IrregularAxisMap(xData, DEFAULT_XGRID);
        yAxisMap = new LinearAxisMap(yData);
    }

    /**
     * cloning.
     *
     * @return java.lang.Object
     */
    public Object clone() {
        IRSpectrum spectrum = (IRSpectrum) super.clone();
        this.xAxisMap = new IrregularAxisMap((IOrderedArray1D) xData, DEFAULT_XGRID);
        return spectrum;
    }


    /**
     * gets spectrum ID
     * @return int
     */
    public int getIdentifier() {
        return ISpectrumIdentifier.IR;
    }


    /**
     * isSameType method comment.
     */
    public boolean isSameType(Spectrum otherSpectrum) {
        boolean r = false;
        if (otherSpectrum instanceof IRSpectrum) {
            r = true;
        }
        return r;
    }


    /**
     * sets full view range on x axis.
     */
    public void setXFullViewRange(Range1D.Double dataRange) {
        IrregularGrid1D newGrid = new IrregularGrid1D(DEFAULT_XGRID, dataRange);
        this.xAxisMap = new IrregularAxisMap((IInterval1D) xData, newGrid);
    }
}
