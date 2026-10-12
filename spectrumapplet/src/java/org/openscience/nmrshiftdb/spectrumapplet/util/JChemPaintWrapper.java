package org.openscience.nmrshiftdb.spectrumapplet.util;

import org.openscience.jchempaint.applet.JChemPaintAbstractApplet;


public class JChemPaintWrapper
                implements StructureApplet{

        private JChemPaintAbstractApplet structureApplet;

    public JChemPaintWrapper(Object object)
    {
         ;
         structureApplet = (JChemPaintAbstractApplet)object;
         return;
    }


    public void highlightAtoms(int[] iArr)
    {
         for (int j=0;j < iArr.length;j++) {
              if (iArr[j] > -1 && iArr[j] <= 99999)
                   structureApplet.selectAtom(iArr[j] - 1);
         }
         return;
    }


    public void clearHighlighting()
    {
         return;
    }

}
