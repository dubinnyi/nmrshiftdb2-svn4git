package org.jcamp.parser;

import org.apache.commons.logging.*;

import org.jcamp.spectrum.*;

import java.util.*;

/**
 * reader for JCAMP-DX spectrum format.
 * @author Thomas Weber
 */
public class JCAMPReader_Ext {
    private static Log log                                   = LogFactory.getLog(JCAMPReader_Ext.class);
    private final static IErrorHandler DEFAULT_ERROR_HANDLER = new ErrorHandlerAdapter() {
        public void fatal(String msg) throws JCAMPException {
            log.fatal(msg);
            throw new JCAMPException("FATAL ERROR! " + msg);
        }


        public void error(String msg) throws JCAMPException {
            log.error(msg);
            throw new JCAMPException("ERROR! " + msg);
        }


        public void warn(String msg) throws JCAMPException {
            log.warn(msg);
        }
    };
    private IErrorHandler errorHandler                       = DEFAULT_ERROR_HANDLER;
    private static JCAMPReader_Ext theInstance               = null;
    private static Hashtable adapters                        = new Hashtable(20);

    /**
     * JCAMPReaderExt constructor comment.
     */
    private JCAMPReader_Ext() {
        super();
    }

    /**
     * create spectrum from JCAMPBlock.
     *
     * @return Spectrum
     * @param block int
     */
    public Spectrum createSpectrum(JCAMPBlock block) throws JCAMPException {
        if (block.isLinkBlock()) {
            errorHandler.warn("compound JCAMP encountered: using first spectrum block");
            block = findFirstSpectrumBlock(block);
        }
        ISpectrumJCAMPReader reader = findAdapter(block.getSpectrumID());
        System.out.println("Adapter: " + reader.getClass().getName());
        if (reader == null) {
            errorHandler.fatal("spectrum type not implemented");
        }
        return reader.createSpectrum(block);
    }


    /**
     * create spectrum from JCAMPBlock.
     *
     * @return Spectrum
     * @param block JCAMPBlock
     * @param blockID int
     */
    public Spectrum createSpectrum(JCAMPBlock block, int blockID)
                            throws JCAMPException {
        return createSpectrum(block.getBlock(blockID));
    }


    /**
     * create spectrum from JCAMP-DX string.
     *
     * @return Spectrum
     * @param jcamp JCAMP-DX source
     * @exception JCAMPException The exception description.
     */
    public Spectrum createSpectrum(String jcamp) throws JCAMPException {
        JCAMPBlock block = new JCAMPBlock(jcamp, errorHandler);
        return createSpectrum(block);
    }


    /**
     * find JCAMPAdapter for specific spectrum ID.
     *
     * @return ISpectrumJCAMPReader
     * @param spectrumID int
     */
    private ISpectrumJCAMPReader findAdapter(int spectrumID) {
        ISpectrumJCAMPReader adapter = (ISpectrumJCAMPReader) adapters.get(new Integer(spectrumID));
        return adapter;
    }


    /**
     * find first spectrum block within a compound JCAMP link block,
     * first tries to find a full spectrum block, otherwise uses first block encountered
     *
     * @return com.creon.chem.jcamp.JCAMPBlock
     * @param block com.creon.chem.jcamp.JCAMPBlock
     */
    private JCAMPBlock findFirstSpectrumBlock(JCAMPBlock block)
                                       throws JCAMPException {
        // first try, returning only full spectra
        Enumeration blocks = block.getBlocks();
        if (blocks != null) {
            while (blocks.hasMoreElements()) {
                JCAMPBlock b = (JCAMPBlock) blocks.nextElement();
                if (b.isStructureBlock() || b.isLinkBlock()) {
                    continue;
                }
                JCAMPDataRecord dataTypeLDR = b.getDataRecord("DATATYPE");
                if (dataTypeLDR == null) {
                    continue; //bad block
                }
                String dataType = dataTypeLDR.getContent().toUpperCase();
                if (dataType.endsWith("TABLE") || dataType.endsWith("ASSIGNMENTS")) {
                    continue;
                }
                return b;
            }
        }

        // second try, returning any spectrum
        blocks = block.getBlocks();
        if (blocks != null) {
            while (blocks.hasMoreElements()) {
                JCAMPBlock b = (JCAMPBlock) blocks.nextElement();
                if (b.isStructureBlock() || b.isLinkBlock()) {
                    continue;
                }
                return b;
            }
        }
        errorHandler.fatal("link block contains no spectrum block");
        return null;
    }


    /**
     * gets current error handler.
     *
     * @return com.creon.chem.jcamp.IErrorHandler
     */
    public IErrorHandler getErrorHandler() {
        return errorHandler;
    }


    /**
     * access method for JCAMPReader singleton instance.
     *
     * @return JCAMPReader
     */
    public static JCAMPReader_Ext getInstance() {
        if (theInstance == null) {
            theInstance = new JCAMPReader_Ext();
            initAdapters();
        }
        return theInstance;
    }


    /**
     * initialize adapter map
     *
     */
    private static void initAdapters() {
        adapters.put(new Integer(ISpectrumIdentifier.NMR), new NMRJCAMPReader());
        adapters.put(new Integer(ISpectrumIdentifier.IR), new IRJCAMPReader_Ext());
        adapters.put(new Integer(ISpectrumIdentifier.UV), new UVJCAMPReader());
        adapters.put(new Integer(ISpectrumIdentifier.MS), new MSJCAMPReader());
        adapters.put(new Integer(ISpectrumIdentifier.RAMAN), new RamanJCAMPReader());
        adapters.put(new Integer(ISpectrumIdentifier.CHROMATOGRAM), new ChromatogramJCAMPReader());
        adapters.put(new Integer(ISpectrumIdentifier.FLUORESCENCE), new FluorescenceJCAMPReader());
    }


    /**
     * sets the error handler.
     *
     * @param newErrorHandler com.creon.chem.jcamp.IErrorHandler
     */
    public void setErrorHandler(IErrorHandler newErrorHandler) {
        errorHandler = newErrorHandler;
    }
}
