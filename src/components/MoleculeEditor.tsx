import React, { useState, useEffect, useRef } from 'react';
import {
  Sparkles,
  HelpCircle,
  Trash2,
  Check,
  RefreshCw,
  Copy,
  ExternalLink,
  Info
} from 'lucide-react';

interface MoleculeEditorProps {
  smiles: string;
  onSmilesChange: (smiles: string, molfile?: string, isUserEdit?: boolean) => void;
  structureName: string;
  activePreset: string | null;
  onStructureSelect: (preset: { name: string; smiles: string }, molfile?: string) => void;
}

declare global {
  interface Window {
    JSApplet?: {
      JSME: new (containerId: string, width: string, height: string, options?: any) => any;
    };
    jsmeOnLoad?: () => void;
  }
}

const CONTAINER_ID = 'jsme_container_main';

export const MoleculeEditor: React.FC<MoleculeEditorProps> = ({
  smiles,
  onSmilesChange,
  structureName,
  activePreset,
  onStructureSelect
}) => {
  const [inputSmiles, setInputSmiles] = useState<string>(smiles);
  const [isJsmeReady, setIsJsmeReady] = useState<boolean>(false);
  const [copied, setCopied] = useState<boolean>(false);
  
  const appletRef = useRef<any>(null);
  const lastSmilesRef = useRef<string>(smiles);
  const isInternalChangeRef = useRef<boolean>(false);
  const isProgrammaticRef = useRef<boolean>(false);
  const initializedRef = useRef<boolean>(false);

  const presets = [
    { name: 'Threonic acid', smiles: 'OCC(O)C(O)C(=O)O' },
    { name: 'Benzene', smiles: 'c1ccccc1' },
    { name: 'Ethyl Acetate', smiles: 'CCOC(=O)C' },
    { name: 'Cyclohexane', smiles: 'C1CCCCC1' },
    { name: 'Ethanol', smiles: 'CCO' },
    { name: 'Acetone', smiles: 'CC(=O)C' },
    { name: 'Pyridine', smiles: 'c1ccncc1' }
  ];

  // Extract structure (Molfile V2000 or JME) directly from JSME applet
  const getStructureFromJsme = (): string | null => {
    const app = appletRef.current;
    if (!app) return null;
    try {
      if (typeof app.molFile === 'function') {
        const res = app.molFile();
        if (res && typeof res === 'string' && res.trim()) return res;
      }
      if (typeof app.molfile === 'function') {
        const res = app.molfile();
        if (res && typeof res === 'string' && res.trim()) return res;
      }
      if (typeof app.jmeFile === 'function') {
        const res = app.jmeFile();
        if (res && typeof res === 'string' && res.trim()) return res;
      }
      if (typeof app.getMolfile === 'function') {
        const res = app.getMolfile();
        if (res && typeof res === 'string' && res.trim()) return res;
      }
    } catch (e) {
      console.warn('[JSME] getStructureFromJsme error:', e);
    }
    return null;
  };

  // Initialize JSME Molecule Editor exactly once
  useEffect(() => {
    let checkInterval: any = null;
    let cancelled = false;

    const initJsme = () => {
      if (cancelled || initializedRef.current || appletRef.current) return;
      const container = document.getElementById(CONTAINER_ID);
      if (!container || !window.JSApplet?.JSME) return;

      try {
        initializedRef.current = true;
        if (checkInterval) {
          clearInterval(checkInterval);
          checkInterval = null;
        }

        container.innerHTML = '';
        const applet = new window.JSApplet.JSME(
          CONTAINER_ID,
          '100%',
          '390px',
          {
            options: 'hydrogens,star,autoez'
          }
        );

        appletRef.current = applet;
        setIsJsmeReady(true);

        // Register modification listener - only fires when user edits directly
        applet.setCallBack('AfterStructureModified', (jsmeEvent: any) => {
          if (isProgrammaticRef.current) {
            return;
          }
          try {
            const currentSmiles = (applet.smiles && typeof applet.smiles === 'function')
              ? applet.smiles()
              : ((jsmeEvent && typeof jsmeEvent.smiles === 'function') ? jsmeEvent.smiles() : '');
            const currentMolfile = getStructureFromJsme();

            isInternalChangeRef.current = true;
            if (currentSmiles !== undefined && currentSmiles !== '') {
              lastSmilesRef.current = currentSmiles;
              setInputSmiles(currentSmiles);
              // Mark isUserEdit = true so presets become inactive immediately
              onSmilesChange(currentSmiles, currentMolfile || undefined, true);
            } else if (currentMolfile) {
              onSmilesChange(lastSmilesRef.current || smiles, currentMolfile, true);
            }
          } catch (err) {
            console.warn('[JSME] Error reading modified structure:', err);
          }
        });

        // Load initial SMILES and capture exact coordinates without dispatching user edit events
        if (smiles && smiles.trim()) {
          try {
            isProgrammaticRef.current = true;
            applet.readGenericMolecularInput(smiles.trim());
            const initMol = getStructureFromJsme();
            isProgrammaticRef.current = false;
            if (initMol) {
              onSmilesChange(smiles, initMol, false);
            }
          } catch (loadErr) {
            isProgrammaticRef.current = false;
            console.warn('[JSME] Failed to load initial SMILES:', loadErr);
          }
        }
      } catch (e) {
        console.error('[JSME] Initialization failed:', e);
      }
    };

    // If JSME is already loaded
    if (window.JSApplet?.JSME) {
      initJsme();
    } else {
      const prevOnLoad = window.jsmeOnLoad;
      window.jsmeOnLoad = () => {
        if (prevOnLoad) prevOnLoad();
        initJsme();
      };

      let attempts = 0;
      checkInterval = setInterval(() => {
        attempts++;
        if (window.JSApplet?.JSME) {
          if (checkInterval) clearInterval(checkInterval);
          initJsme();
        } else if (attempts > 60) {
          if (checkInterval) clearInterval(checkInterval);
        }
      }, 100);
    }

    return () => {
      cancelled = true;
      if (checkInterval) clearInterval(checkInterval);
    };
  }, []);

  // Sync incoming prop SMILES changes to JSME
  useEffect(() => {
    setInputSmiles(smiles);

    if (isInternalChangeRef.current) {
      isInternalChangeRef.current = false;
      return;
    }

    if (appletRef.current && smiles !== lastSmilesRef.current) {
      lastSmilesRef.current = smiles;
      try {
        isProgrammaticRef.current = true;
        if (smiles.trim()) {
          appletRef.current.readGenericMolecularInput(smiles.trim());
          const mol = getStructureFromJsme();
          if (mol) onSmilesChange(smiles, mol, false);
        } else {
          appletRef.current.reset();
        }
        isProgrammaticRef.current = false;
      } catch (err) {
        isProgrammaticRef.current = false;
        console.warn('[JSME] readGenericMolecularInput error:', err);
      }
    }
  }, [smiles]);

  // Handle clicking a Preset explicitly
  const handleSelectPreset = (preset: { name: string; smiles: string }) => {
    lastSmilesRef.current = preset.smiles;
    setInputSmiles(preset.smiles);

    if (appletRef.current) {
      try {
        isProgrammaticRef.current = true;
        appletRef.current.readGenericMolecularInput(preset.smiles);
        const mol = getStructureFromJsme();
        isProgrammaticRef.current = false;
        onStructureSelect(preset, mol || undefined);
      } catch (err) {
        isProgrammaticRef.current = false;
        onStructureSelect(preset);
      }
    } else {
      onStructureSelect(preset);
    }
  };

  // Apply custom SMILES from text box
  const handleApplySmiles = (targetSmiles?: string) => {
    const s = (targetSmiles !== undefined ? targetSmiles : inputSmiles).trim();
    lastSmilesRef.current = s;
    setInputSmiles(s);

    if (appletRef.current) {
      try {
        isProgrammaticRef.current = true;
        if (s) {
          appletRef.current.readGenericMolecularInput(s);
        } else {
          appletRef.current.reset();
        }
        const mol = getStructureFromJsme();
        isProgrammaticRef.current = false;
        // User edit from custom input
        onSmilesChange(s, mol || undefined, true);
      } catch (err) {
        isProgrammaticRef.current = false;
        console.warn('[JSME] Failed to apply SMILES:', err);
        onSmilesChange(s, undefined, true);
      }
    } else {
      onSmilesChange(s, undefined, true);
    }
  };

  const handleClear = () => {
    if (appletRef.current) {
      try {
        isProgrammaticRef.current = true;
        appletRef.current.reset();
        isProgrammaticRef.current = false;
      } catch (e) {
        isProgrammaticRef.current = false;
        console.warn(e);
      }
    }
    lastSmilesRef.current = '';
    setInputSmiles('');
    onSmilesChange('', '', true);
  };

  const handleCopySmiles = () => {
    navigator.clipboard.writeText(smiles);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-3">
      {/* Top Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2 bg-slate-100 border border-slate-300 p-2 rounded-t text-xs">
        <div className="flex items-center gap-2">
          <div className="font-bold text-slate-800 flex items-center gap-1.5">
            <span className="bg-blue-600 text-white px-2 py-0.5 rounded text-[11px] font-mono">JSME</span>
            <span>Molecular Structure Editor</span>
          </div>
          <span className="text-slate-400">|</span>
          <span className="text-slate-600 font-medium">
            Active: <strong className="text-blue-900">{structureName || 'Custom Molecule'}</strong>
          </span>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleClear}
            className="bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-300 px-2.5 py-1 rounded text-xs font-medium flex items-center gap-1 transition-colors cursor-pointer"
            title="Clear molecular canvas"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>Clear</span>
          </button>
        </div>
      </div>

      {/* Main JSME Canvas Container */}
      <div className="relative border-x border-b border-slate-300 bg-white rounded-b overflow-hidden shadow-xs min-h-[390px]">
        {!isJsmeReady && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-slate-50 text-slate-500 gap-2">
            <RefreshCw className="w-6 h-6 animate-spin text-blue-600" />
            <p className="text-xs font-medium">Loading JSME Chemical Molecule Editor...</p>
          </div>
        )}
        <div
          id={CONTAINER_ID}
          className="w-full h-[390px] bg-white"
        />
      </div>

      {/* Quick Helper Tips for Chemists */}
      <div className="flex flex-wrap items-center justify-between text-[11px] text-slate-600 bg-blue-50/60 border border-blue-200 px-3 py-1.5 rounded">
        <div className="flex items-center gap-3">
          <Info className="w-3.5 h-3.5 text-blue-600 shrink-0" />
          <span>
            <strong>Chemist Controls:</strong> Click bond to cycle <em>single &rarr; double &rarr; triple</em> • Click atom to change element • Select ring templates (Benzene, Cyclohexane, etc.) at bottom bar.
          </span>
        </div>
        <span className="text-slate-400 hidden md:inline">Full periodic table supported</span>
      </div>

      {/* SMILES Input & Presets Bar */}
      <div className="bg-slate-50 border border-slate-300 p-2.5 rounded space-y-2">
        <div className="flex items-center gap-2">
          <label className="text-xs font-bold text-slate-700 shrink-0">
            SMILES:
          </label>
          <input
            type="text"
            value={inputSmiles}
            onChange={(e) => {
              setInputSmiles(e.target.value);
              // Mark user edit so all presets become inactive
              onSmilesChange(e.target.value, undefined, true);
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleApplySmiles();
            }}
            placeholder="e.g. O=C(O)C(O)C(O)C(=O)O or c1ccccc1"
            className="flex-1 border border-slate-300 bg-white rounded px-2.5 py-1 text-xs font-mono text-slate-900 focus:outline-none focus:border-blue-600 shadow-2xs"
          />
          <button
            onClick={() => handleApplySmiles()}
            className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-xs font-bold transition-colors cursor-pointer flex items-center gap-1 shadow-2xs"
          >
            <RefreshCw className="w-3 h-3" />
            <span>Apply SMILES</span>
          </button>
          <button
            onClick={handleCopySmiles}
            className="bg-white hover:bg-slate-100 text-slate-700 border border-slate-300 px-2.5 py-1 rounded text-xs font-medium transition-colors cursor-pointer flex items-center gap-1 shadow-2xs"
            title="Copy current SMILES"
          >
            {copied ? <Check className="w-3 h-3 text-emerald-600" /> : <Copy className="w-3 h-3" />}
            <span>{copied ? 'Copied' : 'Copy'}</span>
          </button>
        </div>

        {/* Preset Molecules Pills */}
        <div className="flex flex-wrap items-center gap-1.5 pt-1 text-xs">
          <span className="text-slate-500 font-semibold text-[11px] mr-1">Presets:</span>
          {presets.map((preset) => {
            const isSelected = activePreset === preset.name;
            return (
              <button
                key={preset.name}
                type="button"
                onClick={() => handleSelectPreset(preset)}
                className={`px-2 py-0.5 rounded text-[11px] font-medium transition-colors cursor-pointer ${
                  isSelected
                    ? 'bg-blue-600 text-white shadow-xs font-semibold'
                    : 'bg-white text-slate-700 hover:bg-slate-200 border border-slate-300'
                }`}
              >
                {preset.name.split(' (')[0]}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
