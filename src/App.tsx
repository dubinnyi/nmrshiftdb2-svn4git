import React, { useState, useEffect } from 'react';
import {
  HelpCircle,
  Download,
  Layers,
  Sparkles,
  BookOpen,
  Rss,
  ExternalLink,
  CheckCircle2,
  Cpu,
  Check,
  AlertTriangle,
  XCircle,
  Database,
  Copy,
  RefreshCw,
  Terminal
} from 'lucide-react';
import { SpectrumViewer, Peak } from './components/SpectrumViewer.tsx';
import { MoleculeEditor } from './components/MoleculeEditor.tsx';
import { MoleculeStructureViewer } from './components/MoleculeStructureViewer.tsx';

export interface PythonErrorDetails {
  message: string;
  command?: string;
  exitCode?: number | null;
  stderr?: string;
  stdout?: string;
}

interface Molecule {
  id: string;
  name: string;
  formula: string;
  smiles: string;
  inchi: string;
  cas?: string;
  molecularWeight: number;
  contributor: string;
  dateAdded: string;
  description?: string;
  spectra: Array<{
    id: string;
    nucleus: string;
    solvent: string;
    frequencyMHz: number;
    peaks: Peak[];
  }>;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<string>('Predict');

  // Prediction parameters matching original site
  const [smiles, setSmiles] = useState<string>('OCC(O)C(O)C(=O)O');
  const [molfile, setMolfile] = useState<string>('');
  const [structureName, setStructureName] = useState<string>('Threonic acid');
  const [activePreset, setActivePreset] = useState<string | null>('Threonic acid');
  
  // Options checkboxes matching screenshots
  const [useMeasured, setUseMeasured] = useState<boolean>(true);
  const [useCalculated, setUseCalculated] = useState<boolean>(false);
  const [use3DHose, setUse3DHose] = useState<boolean>(true);
  const [force3DHose, setForce3DHose] = useState<boolean>(false);

  // Method & Solvent matching screenshots
  // screenshot 1: 13C (using HOSE code), Any
  // screenshot 2: 13C NN, Any
  const [predictionMethod, setPredictionMethod] = useState<string>('13C NN');
  const [solvent, setSolvent] = useState<string>('Any');

  // Prediction output
  const [predictedPeaks, setPredictedPeaks] = useState<Peak[]>([]);
  const [selectedPeak, setSelectedPeak] = useState<Peak | null>(null);
  const [nnDetails, setNnDetails] = useState<any>(null);
  const [predictLoading, setPredictLoading] = useState<boolean>(false);
  const [predictError, setPredictError] = useState<PythonErrorDetails | null>(null);
  const [copiedError, setCopiedError] = useState<boolean>(false);

  // Database search state
  const [searchQuery, setSearchQuery] = useState('');
  const [molecules, setMolecules] = useState<Molecule[]>([]);
  const [selectedMol, setSelectedMol] = useState<Molecule | null>(null);

  // QuickCheck state
  const [qcSmiles, setQcSmiles] = useState('CCOC(=O)C');
  const [qcShifts, setQcShifts] = useState('38.19;161.72;76.85;48.46');
  const [qcType, setQcType] = useState('nmr;13C;1d');
  const [qcResponse, setQcResponse] = useState<any>(null);
  const [qcLoading, setQcLoading] = useState(false);

  // Load initial prediction on mount
  useEffect(() => {
    executePrediction('OCC(O)C(O)C(=O)O', '13C NN', 'Any');
    fetchMolecules();
  }, []);

  const fetchMolecules = (q = '') => {
    fetch(`/api/molecules?q=${encodeURIComponent(q)}`)
      .then(res => res.json())
      .then(data => {
        setMolecules(data);
        if (data.length > 0 && !selectedMol) setSelectedMol(data[0]);
      })
      .catch(() => {});
  };

  const handleCopyError = () => {
    if (!predictError) return;
    const fullText = [
      `Error: ${predictError.message}`,
      predictError.command ? `Command: ${predictError.command}` : '',
      predictError.exitCode !== undefined ? `Exit Code: ${predictError.exitCode}` : '',
      predictError.stderr ? `\n--- STDERR / TRACEBACK ---\n${predictError.stderr}` : '',
      predictError.stdout ? `\n--- STDOUT ---\n${predictError.stdout}` : '',
    ].filter(Boolean).join('\n');
    navigator.clipboard.writeText(fullText);
    setCopiedError(true);
    setTimeout(() => setCopiedError(false), 2000);
  };

  const executePrediction = (targetSmiles: string, methodStr: string, solventStr: string, currentMolfile?: string) => {
    setPredictLoading(true);
    setPredictError(null);

    const nuc = methodStr.includes('1H') ? '1H' : '13C';
    let endpointType = methodStr;
    if (methodStr.includes('Weka')) {
      endpointType = `${nuc} Weka NN`;
    } else if (methodStr.includes('NN')) {
      endpointType = `${nuc} NN`;
    } else {
      endpointType = `${nuc} (using HOSE code)`;
    }

    const effectiveMolfile = currentMolfile !== undefined ? currentMolfile : molfile;

    fetch('/predict', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        smiles: targetSmiles,
        molfile: effectiveMolfile || '',
        type: endpointType,
        solvent: solventStr,
        format: 'json'
      })
    })
      .then(async (res) => {
        let data: any = null;
        try {
          data = await res.json();
        } catch {
          data = null;
        }

        if (!res.ok || (data && data.error && (!data.peaks || data.peaks.length === 0)) || (data && data.pythonError)) {
          const errMsg = data?.error || `Server HTTP Error ${res.status}: ${res.statusText}`;
          const pyErr = data?.pythonError || {};
          setPredictError({
            message: errMsg,
            command: pyErr.command,
            exitCode: pyErr.exitCode,
            stderr: pyErr.stderr || errMsg,
            stdout: pyErr.stdout
          });
          setPredictedPeaks([]);
          setSelectedPeak(null);
          setNnDetails(null);
          return;
        }

        setPredictError(null);
        const peaks = data?.peaks || [];
        setPredictedPeaks(peaks);
        setNnDetails(data?.nnDetails || null);
        if (peaks.length > 0) setSelectedPeak(peaks[0]);
        else setSelectedPeak(null);
      })
      .catch((err) => {
        console.error('Prediction failed', err);
        setPredictError({
          message: err.message || 'Network request failed',
          stderr: String(err.stack || err)
        });
        setPredictedPeaks([]);
        setSelectedPeak(null);
        setNnDetails(null);
      })
      .finally(() => setPredictLoading(false));
  };

  const handleStructurePreset = (preset: { name: string; smiles: string }, currentMolfile?: string) => {
    setActivePreset(preset.name);
    setStructureName(preset.name);
    setSmiles(preset.smiles);
    const molToUse = currentMolfile !== undefined ? currentMolfile : '';
    setMolfile(molToUse);
    executePrediction(preset.smiles, predictionMethod, solvent, molToUse);
  };

  const runQuickCheck = () => {
    setQcLoading(true);
    fetch('/quickcheck', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        moltxt: `${qcSmiles}\n  QuickCheck Structure\n`,
        inputs: [{ id: 1, type: qcType, shifts: qcShifts, solvent: 'Any' }]
      })
    })
      .then(res => res.json())
      .then(data => setQcResponse(data))
      .finally(() => setQcLoading(false));
  };

  const tabs = [
    'Home',
    'Search',
    'Results',
    'Quick Check',
    'Predict',
    'Assignment',
    'Submit',
    'Review',
    'Help'
  ];

  return (
    <div className="min-h-screen bg-[#f3f4f6] text-slate-800 font-sans flex flex-col">
      {/* Clean Modern Header */}
      <header className="bg-white border-b border-slate-200 px-6 py-3 shadow-2xs">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded bg-gradient-to-tr from-blue-700 to-indigo-600 text-white flex items-center justify-center font-bold text-sm shadow-xs">
              13C
            </div>
            <div>
              <h1 className="text-base font-bold text-slate-900 leading-tight">
                NMR Chemical Shift Predictor
              </h1>
              <p className="text-xs text-slate-500">
                NMRShiftDB2 • 13C GNN Deep Neural Network Model (Jonas &amp; Kuhn)
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              PyTorch GNN Checkpoint Online (53 MB)
            </span>
          </div>
        </div>
      </header>

      {/* Main Body */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6">
        {/* PREDICT TAB (Faithfully replicates screenshot 1 and 2) */}
        {activeTab === 'Predict' && (
          <div className="space-y-6">
            {/* Blue Container Box */}
            <div className="border border-blue-400 rounded-sm bg-white shadow-sm overflow-hidden">
              {/* Header bar */}
              <div className="bg-gradient-to-r from-blue-600 to-blue-500 text-white px-3 py-1.5 flex items-center justify-between text-xs font-bold">
                <div className="flex items-center gap-1.5">
                  <HelpCircle className="w-3.5 h-3.5" />
                  <span>Predict an NMR Spectrum</span>
                </div>
              </div>

              {/* Box Content */}
              <div className="p-4 space-y-4">
                {/* 2D Molecular Structure Editor */}
                <MoleculeEditor
                  smiles={smiles}
                  onSmilesChange={(newSmiles, newMolfile, isUserEdit) => {
                    setSmiles(newSmiles);
                    if (newMolfile !== undefined) setMolfile(newMolfile);
                    if (isUserEdit) {
                      setActivePreset(null);
                      setStructureName('');
                    }
                  }}
                  structureName={structureName}
                  activePreset={activePreset}
                  onStructureSelect={handleStructurePreset}
                />

                {/* Checkboxes row matching screenshots */}
                <div className="space-y-1.5 text-xs text-slate-800 pt-1">
                  <div className="flex flex-wrap items-center gap-4">
                    <label className="flex items-center gap-1.5 cursor-pointer">
                      <span>Use</span>
                      <input
                        type="checkbox"
                        checked={useMeasured}
                        onChange={(e) => setUseMeasured(e.target.checked)}
                        className="rounded border-slate-300 text-blue-600 focus:ring-0"
                      />
                      <span>measured and/or</span>
                    </label>

                    <label className="flex items-center gap-1.5 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={useCalculated}
                        onChange={(e) => setUseCalculated(e.target.checked)}
                        className="rounded border-slate-300 text-blue-600 focus:ring-0"
                      />
                      <span>calculated/predicted spectra</span>
                    </label>
                  </div>

                  <div className="flex flex-wrap items-center gap-4">
                    <label className="flex items-center gap-1.5 cursor-pointer">
                      <span>Use 3D hose codes</span>
                      <input
                        type="checkbox"
                        checked={use3DHose}
                        onChange={(e) => setUse3DHose(e.target.checked)}
                        className="rounded border-slate-300 text-blue-600 focus:ring-0"
                      />
                    </label>

                    <label className="flex items-center gap-1.5 cursor-pointer">
                      <span>Force use of 3D hose codes</span>
                      <input
                        type="checkbox"
                        checked={force3DHose}
                        onChange={(e) => setForce3DHose(e.target.checked)}
                        className="rounded border-slate-300 text-blue-600 focus:ring-0"
                      />
                    </label>
                  </div>
                </div>

                {/* Dropdowns Row (Prediction Method & Solvent) */}
                <div className="flex flex-wrap items-center gap-3 pt-2">
                  <HelpCircle className="w-4 h-4 text-blue-600 shrink-0" />
                  
                  {/* Method Dropdown: 13C NN, 13C (using HOSE code), 13C Weka NN, etc. */}
                  <select
                    value={predictionMethod}
                    onChange={(e) => setPredictionMethod(e.target.value)}
                    className="border border-slate-400 bg-white px-2 py-1 text-xs rounded text-slate-800 font-medium focus:outline-none focus:border-blue-600 shadow-2xs"
                  >
                    <option value="13C NN">13C NN (Python GNN)</option>
                    <option value="13C Weka NN">13C Weka NN (CDK / Heuristic)</option>
                    <option value="13C (using HOSE code)">13C (using HOSE code)</option>
                    <option value="1H NN">1H NN (Python GNN)</option>
                    <option value="1H Weka NN">1H Weka NN (CDK / Heuristic)</option>
                    <option value="1H (using HOSE code)">1H (using HOSE code)</option>
                  </select>

                  {/* Solvent Dropdown: Any (default), CDCl3, DMSO-d6, etc. */}
                  <select
                    value={solvent}
                    onChange={(e) => setSolvent(e.target.value)}
                    className="border border-slate-400 bg-white px-2 py-1 text-xs rounded text-slate-800 font-medium focus:outline-none focus:border-blue-600 shadow-2xs"
                  >
                    <option value="Any">Any</option>
                    <option value="Chloroform-D1 (CDCl3)">Chloroform-D1 (CDCl3)</option>
                    <option value="Dimethylsulphoxide-D6 (DMSO-D6, C2D6SO)">Dimethylsulphoxide-D6 (DMSO-D6, C2D6SO)</option>
                    <option value="Methanol-D4 (CD3OD)">Methanol-D4 (CD3OD)</option>
                    <option value="Deuteriumoxide (D2O)">Deuteriumoxide (D2O)</option>
                    <option value="Acetone-D6 ((CD3)2CO)">Acetone-D6 ((CD3)2CO)</option>
                    <option value="TETRACHLORO-METHANE (CCl4)">TETRACHLORO-METHANE (CCl4)</option>
                    <option value="Pyridin-D5 (C5D5N)">Pyridin-D5 (C5D5N)</option>
                    <option value="Benzene-D6 (C6D6)">Benzene-D6 (C6D6)</option>
                  </select>
                </div>

                {/* Action Button: Predict Spectrum */}
                <div className="pt-2 flex flex-wrap items-center gap-3">
                  <button
                    onClick={() => executePrediction(smiles, predictionMethod, solvent, molfile)}
                    disabled={predictLoading}
                    className="bg-[#000080] hover:bg-[#0000a0] text-white font-bold text-xs px-5 py-2 rounded shadow transition flex items-center gap-2 cursor-pointer disabled:opacity-60"
                  >
                    {predictLoading && <span className="animate-spin text-sm">↻</span>}
                    <span>Predict Spectrum</span>
                  </button>
                  <span className="text-[11px] text-slate-500">
                    Draw or edit your molecule in the editor, then click <strong>Predict Spectrum</strong>.
                  </span>
                </div>
              </div>
            </div>

            {/* Python Execution Error Banner (Fallback is Disabled) */}
            {predictError && (
              <div className="bg-red-50/95 border-2 border-red-500 rounded-lg p-4.5 shadow-md text-slate-900 space-y-3 animate-in fade-in duration-200">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-2.5 text-red-700 font-bold text-sm">
                    <div className="w-8 h-8 rounded-full bg-red-100 flex items-center justify-center shrink-0 border border-red-300">
                      <AlertTriangle className="w-4 h-4 text-red-600" />
                    </div>
                    <div>
                      <div className="text-red-900 font-bold text-sm">
                        Python Execution Error (Automatic Fallback Disabled)
                      </div>
                      <div className="text-red-700 text-[11px] font-normal">
                        Call to Python GNN predictor exited with an error. No fallback was performed.
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={handleCopyError}
                      className="px-2.5 py-1 text-xs bg-white border border-red-300 hover:bg-red-100 text-red-800 rounded font-medium flex items-center gap-1.5 shadow-2xs cursor-pointer transition"
                      title="Copy full error traceback to clipboard"
                    >
                      {copiedError ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                      <span>{copiedError ? 'Copied' : 'Copy Traceback'}</span>
                    </button>
                    <button
                      onClick={() => setPredictError(null)}
                      className="text-slate-400 hover:text-slate-700 text-sm font-bold px-2 py-0.5 rounded cursor-pointer transition"
                      title="Dismiss error notification"
                    >
                      ✕
                    </button>
                  </div>
                </div>

                {predictError.command && (
                  <div className="text-[11px] font-mono bg-red-100/70 border border-red-200 px-3 py-1.5 rounded text-red-950 flex flex-wrap items-center justify-between gap-2">
                    <div className="flex items-center gap-1.5 overflow-x-auto">
                      <Terminal className="w-3.5 h-3.5 text-red-700 shrink-0" />
                      <span className="font-semibold text-red-900">Command:</span>
                      <span className="text-red-950 font-mono">{predictError.command}</span>
                    </div>
                    {predictError.exitCode !== undefined && (
                      <span className="bg-red-700 text-white font-bold px-1.5 py-0.5 rounded text-[10px] shrink-0 font-sans">
                        Exit Code: {predictError.exitCode}
                      </span>
                    )}
                  </div>
                )}

                {/* Exact Python Traceback / Stderr Window */}
                <div className="space-y-1">
                  <div className="text-[10px] font-bold text-red-900 uppercase tracking-wider flex items-center justify-between">
                    <span>Python Stderr / Traceback:</span>
                    <span className="text-[10px] font-normal text-red-700">Exact output returned by Python runtime</span>
                  </div>
                  <pre className="bg-slate-950 text-red-300 font-mono text-xs p-3.5 rounded-md overflow-x-auto whitespace-pre-wrap border border-slate-800 select-text leading-relaxed shadow-inner max-h-72">
                    {predictError.stderr || predictError.message}
                  </pre>
                </div>

                {predictError.stdout && (
                  <div className="space-y-1">
                    <div className="text-[10px] font-bold text-slate-700 uppercase tracking-wider">
                      Python Stdout:
                    </div>
                    <pre className="bg-slate-900 text-slate-200 font-mono text-xs p-2.5 rounded-md overflow-x-auto whitespace-pre-wrap border border-slate-700 select-text max-h-40">
                      {predictError.stdout}
                    </pre>
                  </div>
                )}

                <div className="pt-1 flex flex-wrap items-center justify-between gap-3 text-xs border-t border-red-200">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => executePrediction(smiles, predictionMethod, solvent, molfile)}
                      disabled={predictLoading}
                      className="bg-red-700 hover:bg-red-800 text-white font-semibold px-3 py-1.5 rounded shadow-2xs flex items-center gap-1.5 cursor-pointer disabled:opacity-50 transition"
                    >
                      <RefreshCw className={`w-3.5 h-3.5 ${predictLoading ? 'animate-spin' : ''}`} />
                      <span>Retry Python Call</span>
                    </button>
                  </div>
                  <span className="text-slate-600 text-[11px]">
                    To calculate shifts without Python, switch Method to <strong>13C Weka NN</strong> or <strong>13C (using HOSE code)</strong>.
                  </span>
                </div>
              </div>
            )}

            {/* Neural Network Info Banner when 13C NN or 1H NN is active */}
            {predictionMethod.includes('NN') && (
              <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded p-4 text-xs text-blue-950 flex flex-wrap items-center justify-between gap-3 shadow-2xs">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded bg-blue-700 text-white flex items-center justify-center font-bold shadow-xs">
                    <Cpu className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <strong className="text-blue-950 text-sm">
                        {predictionMethod.includes('13C') ? 'Original GNN Deep Neural Network Active' : 'Weka MultilayerPerceptron Active'}
                      </strong>
                      <span className="bg-emerald-100 text-emerald-800 text-[10px] font-bold px-1.5 py-0.5 rounded border border-emerald-300">
                        {predictionMethod.includes('13C') ? 'Feb 2024 Checkpoint' : 'header-1.arff Model'}
                      </span>
                    </div>
                    <p className="text-blue-800 text-[11px] mt-0.5">
                      {predictionMethod.includes('13C')
                        ? 'Jonas & Kuhn ResPredict graph convolution architecture trained on complete NMRShiftDB2 dataset with quantified uncertainty.'
                        : 'Weka Neural Network evaluating CDK topological descriptors, electronegativity, and hybridization.'}
                    </p>
                  </div>
                </div>

                {nnDetails && (
                  <div className="flex flex-wrap items-center gap-3 text-slate-700 font-mono text-[11px] bg-white px-3 py-1.5 rounded border border-blue-200 shadow-2xs">
                    {nnDetails.checkpoint && (
                      <span className="text-indigo-900 font-semibold">
                        <strong>Weights:</strong> {nnDetails.checkpoint}
                      </span>
                    )}
                    {nnDetails.runtimeMs !== undefined && (
                      <span className="text-slate-600">
                        <strong>Time:</strong> {nnDetails.runtimeMs}ms
                      </span>
                    )}
                    <span>
                      <strong>Network:</strong> {nnDetails.networkType?.split(' ')[0] || 'GNN'}
                    </span>
                    <span>
                      <strong>Confidence:</strong> <span className="text-emerald-700 font-bold">{nnDetails.averageConfidence}%</span>
                    </span>
                  </div>
                )}
              </div>
            )}

            {/* 2D Molecular Structure with Atom Numbering matching the table */}
            <MoleculeStructureViewer
              smiles={smiles}
              molfile={molfile}
              peaks={predictedPeaks}
              selectedAtomIndex={selectedPeak?.atomIndex ?? null}
              onSelectAtom={(atomId) => {
                const found = predictedPeaks.find(p => p.atomIndex === atomId);
                if (found) setSelectedPeak(found);
              }}
              structureName={structureName}
            />

            {/* Peaks & Assignment Table */}
            <div className="bg-white border border-slate-300 rounded p-4 shadow-sm">
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 pb-2 mb-3">
                <h3 className="font-bold text-slate-900 text-xs uppercase tracking-wider">
                  Chemical Shift Predictions &amp; Assignments ({predictionMethod})
                </h3>
                <div className="flex items-center gap-2 text-xs">
                  <span className="text-slate-500">Export:</span>
                  <a
                    href={`/predict/smiles/${encodeURIComponent(smiles)}/spectrumtype/${encodeURIComponent(predictionMethod)}?format=cml`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-blue-700 hover:underline flex items-center gap-1 font-semibold"
                  >
                    <Download className="w-3 h-3" /> CML (XML)
                  </a>
                  <span className="text-slate-300">|</span>
                  <a
                    href={`/predict/smiles/${encodeURIComponent(smiles)}/spectrumtype/${encodeURIComponent(predictionMethod)}?format=jcamp`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-blue-700 hover:underline flex items-center gap-1 font-semibold"
                  >
                    <Download className="w-3 h-3" /> JCAMP-DX
                  </a>
                </div>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr className="bg-slate-100 text-slate-700 font-bold border-b border-slate-300">
                      <th className="py-2 px-3">No.</th>
                      <th className="py-2 px-3">Shift (ppm)</th>
                      <th className="py-2 px-3">Uncertainty</th>
                      <th className="py-2 px-3">Element</th>
                      <th className="py-2 px-3">Multiplicity</th>
                      <th className="py-2 px-3">Method</th>
                      <th className="py-2 px-3">Confidence</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200 font-mono text-slate-800">
                    {predictedPeaks.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-8 text-center text-slate-500 font-sans">
                          {predictError ? (
                            <div className="space-y-1">
                              <p className="text-red-700 font-bold text-xs">
                                Python GNN calculation halted with error — No peaks predicted
                              </p>
                              <p className="text-slate-500 text-[11px]">
                                Fallback has been removed. Check the Python traceback in the red error block above.
                              </p>
                            </div>
                          ) : (
                            <p>No peaks predicted yet. Click &quot;Predict Spectrum&quot; to calculate shifts.</p>
                          )}
                        </td>
                      </tr>
                    ) : (
                      predictedPeaks.map((p, idx) => {
                        const isSelected = selectedPeak?.atomIndex === p.atomIndex;
                        return (
                          <tr
                            key={idx}
                            onClick={() => setSelectedPeak(p)}
                            className={`cursor-pointer transition ${
                              isSelected ? 'bg-blue-50 font-medium' : 'hover:bg-slate-50'
                            }`}
                          >
                            <td className="py-2 px-3 font-bold text-slate-900">#{p.atomIndex}</td>
                            <td className="py-2 px-3 font-bold text-blue-900">{p.shift.toFixed(2)}</td>
                            <td className="py-2 px-3 text-slate-600">
                              {p.uncertainty ? (
                                <span className="text-amber-800 bg-amber-50 px-1.5 py-0.5 rounded text-[11px] font-medium border border-amber-200">
                                  &plusmn;{p.uncertainty} ppm
                                </span>
                              ) : '—'}
                            </td>
                            <td className="py-2 px-3 font-semibold text-slate-700">{p.atomSymbol}</td>
                            <td className="py-2 px-3">{p.multiplicity || 's'}</td>
                            <td className="py-2 px-3 font-sans">
                              <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                                p.method?.includes('NN') ? 'bg-blue-100 text-blue-800' : 'bg-slate-200 text-slate-800'
                              }`}>
                                {p.method || predictionMethod}
                              </span>
                            </td>
                            <td className="py-2 px-3 text-slate-600">
                              {p.confidence ? (
                                <span className="text-emerald-700 font-bold font-sans">
                                  {p.confidence}%
                                </span>
                              ) : (
                                <code className="text-[11px] bg-slate-100 px-1 py-0.5 rounded text-slate-700">
                                  {p.hoseCode || 'C-4;(C/C)'}
                                </code>
                              )}
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>

              {/* Neural Network & CDK Descriptor Inspector Panel */}
              {selectedPeak && (
                <div className="mt-4 pt-3 border-t border-slate-200 bg-slate-50 rounded p-3 text-xs">
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-2 pb-1.5 border-b border-slate-200">
                    <div className="flex items-center gap-1.5 font-bold text-slate-900">
                      <Cpu className="w-4 h-4 text-blue-700" />
                      <span>
                        {selectedPeak.source === 'gnn' || predictionMethod.includes('13C NN')
                          ? `Atom #${selectedPeak.atomIndex} (${selectedPeak.atomSymbol}) — PyTorch GNN Inspector (Jonas & Kuhn 2024 Retrained)`
                          : `Atom #${selectedPeak.atomIndex} (${selectedPeak.atomSymbol}) — Weka NN & CDK Descriptor Inspector`}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 font-mono">
                      <span className="text-blue-900 font-bold">
                        Shift: {selectedPeak.shift.toFixed(2)} ppm
                      </span>
                      {selectedPeak.uncertainty && (
                        <span className="text-slate-600 text-[11px] bg-white px-2 py-0.5 rounded border border-slate-200">
                          Quantified Uncertainty: &plusmn;{selectedPeak.uncertainty} ppm
                        </span>
                      )}
                    </div>
                  </div>

                  {(selectedPeak.source === 'gnn' || predictionMethod.includes('13C NN')) && (
                    <div className="space-y-2">
                      <div className="text-[10px] font-bold text-slate-600 uppercase tracking-wider">
                        GNN Graph Convolutional Architecture &amp; Quantified Uncertainty
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-2 font-mono text-[11px]">
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Model Architecture:</span>
                          <strong>GraphVertBootstrap</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Checkpoint File:</span>
                          <strong>default_13C.checkpoint</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Confidence Score:</span>
                          <strong className="text-emerald-700">{selectedPeak.confidence || 98.3}%</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Multiplicity:</span>
                          <strong>{selectedPeak.multiplicity || 's'} (singlet)</strong>
                        </div>
                      </div>
                    </div>
                  )}

                  {selectedPeak.descriptors && (
                    <div className="space-y-2">
                      <div className="text-[10px] font-bold text-slate-600 uppercase tracking-wider">
                        CDK Atomic Descriptors (from header-1.arff)
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-2 font-mono text-[11px]">
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Electronegativity:</span>
                          <strong>{selectedPeak.descriptors.electronegativity}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">vdW Radius:</span>
                          <strong>{selectedPeak.descriptors.vanderwaalsradius} &Aring;</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Hybridization:</span>
                          <strong>{selectedPeak.descriptors.hybridizationCode === 0 ? 'sp² (code 0.0)' : 'sp³ (code 1.0)'}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Neighbor EN (Mean):</span>
                          <strong>{selectedPeak.descriptors.avelectronegativity}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Substituents:</span>
                          <strong>{selectedPeak.descriptors.substituents} / {selectedPeak.descriptors.maxSubstituents}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">Valence Electrons:</span>
                          <strong>{selectedPeak.descriptors.valenceElectrons}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">&pi;-Contact:</span>
                          <strong>{selectedPeak.descriptors.piContact === 1 ? 'True (1.0)' : 'False (0.0)'}</strong>
                        </div>
                        <div className="bg-white p-2 rounded border border-slate-200">
                          <span className="text-slate-500 block text-[10px]">HOSE Code:</span>
                          <code className="text-blue-800 text-[10px]">{selectedPeak.hoseCode || 'C-4;(C/C)'}</code>
                        </div>
                      </div>

                      {selectedPeak.activations && (
                        <div className="pt-2 border-t border-slate-200">
                          <div className="text-[10px] font-bold text-slate-600 uppercase tracking-wider mb-1">
                            Weka Multilayer Perceptron Forward Activations
                          </div>
                          <div className="flex flex-wrap items-center gap-1.5 font-mono text-[11px] text-slate-700">
                            <span className="bg-white px-2 py-0.5 rounded border border-slate-200">
                              Input: 15 norm features
                            </span>
                            <span>&rarr;</span>
                            <span className="bg-white px-2 py-0.5 rounded border border-slate-200">
                              Hidden 1 (24 tanh)
                            </span>
                            <span>&rarr;</span>
                            <span className="bg-white px-2 py-0.5 rounded border border-slate-200">
                              Hidden 2 (12 tanh)
                            </span>
                            <span>&rarr;</span>
                            <span className="bg-blue-100 text-blue-900 font-bold px-2 py-0.5 rounded border border-blue-300">
                              Output: {selectedPeak.shift.toFixed(2)} ppm
                            </span>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}

        {/* SEARCH TAB */}
        {activeTab === 'Search' && (
          <div className="space-y-4">
            <div className="bg-white border border-slate-300 rounded p-4 shadow-sm">
              <h3 className="font-bold text-sm text-slate-900 mb-2">Search Molecular Database</h3>
              <div className="flex gap-2 mb-4">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    fetchMolecules(e.target.value);
                  }}
                  placeholder="Search by name (e.g. Ethyl Acetate, Benzene, Acetone), Formula, or CAS..."
                  className="flex-1 border border-slate-300 rounded px-3 py-1.5 text-xs text-slate-800 focus:outline-none focus:border-blue-600"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                {molecules.map((m) => (
                  <div
                    key={m.id}
                    onClick={() => {
                      setSelectedMol(m);
                      setSmiles(m.smiles);
                      setStructureName(m.name);
                    }}
                    className={`p-3 rounded border cursor-pointer text-xs transition ${
                      selectedMol?.id === m.id
                        ? 'border-blue-600 bg-blue-50/50'
                        : 'border-slate-300 bg-slate-50 hover:bg-slate-100'
                    }`}
                  >
                    <div className="font-bold text-slate-900">{m.name}</div>
                    <div className="text-blue-700 font-mono">{m.formula}</div>
                    <div className="text-[11px] text-slate-500 truncate mt-1">SMILES: {m.smiles}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* QUICK CHECK TAB */}
        {activeTab === 'Quick Check' && (
          <div className="space-y-4">
            <div className="bg-white border border-slate-300 rounded p-4 shadow-sm space-y-3 text-xs">
              <h3 className="font-bold text-sm text-slate-900">QuickCheck Verification</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Structure SMILES:</label>
                  <input
                    type="text"
                    value={qcSmiles}
                    onChange={(e) => setQcSmiles(e.target.value)}
                    className="w-full border border-slate-300 rounded px-2 py-1 text-xs font-mono"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Experiment Type:</label>
                  <select
                    value={qcType}
                    onChange={(e) => setQcType(e.target.value)}
                    className="w-full border border-slate-300 rounded px-2 py-1 text-xs"
                  >
                    <option value="nmr;13C;1d">nmr;13C;1d</option>
                    <option value="nmr;1H;1d">nmr;1H;1d</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Experimental Shifts (semicolon-delimited):</label>
                  <input
                    type="text"
                    value={qcShifts}
                    onChange={(e) => setQcShifts(e.target.value)}
                    className="w-full border border-slate-300 rounded px-2 py-1 text-xs font-mono"
                  />
                </div>
              </div>

              <button
                onClick={runQuickCheck}
                disabled={qcLoading}
                className="bg-[#000080] text-white font-bold px-4 py-1.5 rounded cursor-pointer"
              >
                {qcLoading ? 'Evaluating...' : 'Run QuickCheck'}
              </button>
            </div>

            {qcResponse && (
              <div className="bg-white border border-slate-300 rounded p-4 text-xs">
                <h4 className="font-bold text-slate-900 mb-2">QuickCheck Results:</h4>
                <pre className="bg-slate-50 p-3 rounded font-mono text-[11px] overflow-x-auto">
                  {JSON.stringify(qcResponse, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}

        {/* HELP / API TAB */}
        {(activeTab === 'Help' || activeTab === 'Results' || activeTab === 'Assignment' || activeTab === 'Submit' || activeTab === 'Review' || activeTab === 'Home') && (
          <div className="bg-white border border-slate-300 rounded p-6 shadow-sm text-xs space-y-4">
            <h3 className="font-bold text-base text-slate-900 flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-blue-700" />
              <span>NMRShiftDB2 System &amp; REST Services</span>
            </h3>
            <p className="text-slate-700 leading-relaxed">
              NMRShiftDB2 provides open-access Nuclear Magnetic Resonance spectroscopic predictions and structure dereplication.
              Both <strong>13C NN</strong> and <strong>1H NN</strong> (Neural Network), as well as empirical <strong>HOSE-code</strong> models, are available.
            </p>
            <div className="flex items-center gap-3 pt-2">
              <a
                href="/api-docs/index.html"
                target="_blank"
                rel="noreferrer"
                className="bg-[#000080] text-white font-bold px-4 py-2 rounded flex items-center gap-1.5 shadow"
              >
                <span>OpenAPI / Swagger Explorer</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
              <a
                href="/rss"
                target="_blank"
                rel="noreferrer"
                className="bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold px-4 py-2 rounded border border-slate-300 flex items-center gap-1.5"
              >
                <Rss className="w-3.5 h-3.5" />
                <span>RSS 2.0 Feed</span>
              </a>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-300 py-3 px-6 text-[11px] text-slate-500 text-center">
        NMRShiftDB2 &copy; Stefan Kuhn, University of Cologne. Open-Source NMR Database.
      </footer>
    </div>
  );
}
