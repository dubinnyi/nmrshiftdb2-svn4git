import React, { useMemo } from 'react';
import { NMRPeak } from '../../server/nmrData';
import { parseSmilesTo2D, parseMolfileTo2D, EditorAtom, EditorBond } from '../utils/molecule2d';

interface MoleculeStructureViewerProps {
  smiles: string;
  molfile?: string;
  peaks: NMRPeak[];
  selectedAtomIndex: number | null;
  onSelectAtom: (atomIndex: number) => void;
  structureName?: string;
}

export const MoleculeStructureViewer: React.FC<MoleculeStructureViewerProps> = ({
  smiles,
  molfile,
  peaks,
  selectedAtomIndex,
  onSelectAtom,
  structureName
}) => {
  const { atoms, bonds } = useMemo(() => {
    if (molfile && molfile.trim()) {
      const parsed = parseMolfileTo2D(molfile, 580, 240);
      if (parsed.atoms.length > 0) return parsed;
    }
    return parseSmilesTo2D(smiles, 580, 240);
  }, [smiles, molfile]);

  // Map atom index (1-based) to peak if predicted
  const peakMap = useMemo(() => {
    const map = new Map<number, NMRPeak>();
    peaks.forEach(p => {
      map.set(p.atomIndex, p);
    });
    return map;
  }, [peaks]);

  const atomMap = useMemo(() => {
    const map = new Map<number, EditorAtom>();
    atoms.forEach(a => map.set(a.id, a));
    return map;
  }, [atoms]);

  // Color mapping by element
  const getAtomColor = (sym: string) => {
    switch (sym.toUpperCase()) {
      case 'O': return '#dc2626'; // red
      case 'N': return '#2563eb'; // blue
      case 'S': return '#d97706'; // amber
      case 'P': return '#ea580c'; // orange
      case 'F': return '#059669'; // emerald
      case 'CL': return '#16a34a'; // green
      case 'BR': return '#9a3412'; // brown
      case 'I': return '#7c3aed'; // purple
      default: return '#1e293b'; // slate-800 for carbon
    }
  };

  // Smart positioning for chemical shift pills to avoid overlapping bonds and atoms
  const getShiftBadgeOffset = (atom: EditorAtom) => {
    const connectedNeighbors = bonds
      .filter(b => b.source === atom.id || b.target === atom.id)
      .map(b => (b.source === atom.id ? atomMap.get(b.target) : atomMap.get(b.source)))
      .filter(Boolean) as EditorAtom[];

    if (connectedNeighbors.length === 0) {
      return { x: 0, y: 20 };
    }

    let sumDy = 0;
    connectedNeighbors.forEach(n => {
      sumDy += (n.y - atom.y);
    });
    const avgDy = sumDy / connectedNeighbors.length;

    // If neighbors are generally below, place shift badge ABOVE atom (y: -24)
    if (avgDy > 4) {
      return { x: 0, y: -24 };
    }
    // Otherwise place shift badge BELOW atom (y: 20)
    return { x: 0, y: 20 };
  };

  return (
    <div className="bg-white border border-slate-300 rounded p-4 shadow-xs">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 pb-2 mb-3">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-blue-600" />
          <h3 className="font-bold text-slate-900 text-xs uppercase tracking-wider">
            Molecular Structure &amp; Atom Numbering (Mapped to NMR Shifts)
          </h3>
          {structureName && (
            <span className="text-slate-500 text-xs font-medium">
              — {structureName}
            </span>
          )}
        </div>
        
        {/* Legend */}
        <div className="flex items-center gap-3 text-[11px] text-slate-600">
          <div className="flex items-center gap-1.5">
            <span className="w-4 h-4 rounded-full bg-blue-700 text-white flex items-center justify-center text-[9px] font-bold">#</span>
            <span>Atom index</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="px-1.5 py-0.5 rounded bg-emerald-100 text-emerald-800 border border-emerald-300 text-[10px] font-bold">δ ppm</span>
            <span>Predicted shift</span>
          </div>
        </div>
      </div>

      {/* SVG Molecular Canvas */}
      <div className="w-full bg-slate-50/50 border border-slate-200 rounded flex items-center justify-center p-2 overflow-x-auto">
        <svg
          viewBox="0 0 580 240"
          className="w-full max-w-[580px] h-[240px] select-none"
        >
          {/* Subtle grid background */}
          <defs>
            <pattern id="viewer_grid" width="20" height="20" patternUnits="userSpaceOnUse">
              <circle cx="2" cy="2" r="0.75" fill="#cbd5e1" opacity="0.6" />
            </pattern>
          </defs>
          <rect width="580" height="240" fill="url(#viewer_grid)" rx="4" />

          {/* BONDS */}
          {bonds.map(bond => {
            const a1 = atomMap.get(bond.source);
            const a2 = atomMap.get(bond.target);
            if (!a1 || !a2) return null;

            const isDouble = bond.order === 2;
            const isTriple = bond.order === 3;
            const isAromatic = bond.order === 1.5 || bond.aromatic;

            const dx = a2.x - a1.x;
            const dy = a2.y - a1.y;
            const len = Math.hypot(dx, dy) || 1;
            const perpX = (-dy / len) * 2.8;
            const perpY = (dx / len) * 2.8;

            if (isDouble) {
              return (
                <g key={`b-${bond.id}`}>
                  <line
                    x1={a1.x + perpX}
                    y1={a1.y + perpY}
                    x2={a2.x + perpX}
                    y2={a2.y + perpY}
                    stroke="#1e293b"
                    strokeWidth="2.2"
                    strokeLinecap="round"
                  />
                  <line
                    x1={a1.x - perpX}
                    y1={a1.y - perpY}
                    x2={a2.x - perpX}
                    y2={a2.y - perpY}
                    stroke="#1e293b"
                    strokeWidth="2.2"
                    strokeLinecap="round"
                  />
                </g>
              );
            }

            if (isTriple) {
              return (
                <g key={`b-${bond.id}`}>
                  <line x1={a1.x} y1={a1.y} x2={a2.x} y2={a2.y} stroke="#1e293b" strokeWidth="2.2" strokeLinecap="round" />
                  <line x1={a1.x + perpX * 1.5} y1={a1.y + perpY * 1.5} x2={a2.x + perpX * 1.5} y2={a2.y + perpY * 1.5} stroke="#1e293b" strokeWidth="2.0" strokeLinecap="round" />
                  <line x1={a1.x - perpX * 1.5} y1={a1.y - perpY * 1.5} x2={a2.x - perpX * 1.5} y2={a2.y - perpY * 1.5} stroke="#1e293b" strokeWidth="2.0" strokeLinecap="round" />
                </g>
              );
            }

            if (isAromatic) {
              return (
                <g key={`b-${bond.id}`}>
                  <line x1={a1.x} y1={a1.y} x2={a2.x} y2={a2.y} stroke="#1e293b" strokeWidth="2.2" strokeLinecap="round" />
                  <line x1={a1.x + perpX} y1={a1.y + perpY} x2={a2.x + perpX} y2={a2.y + perpY} stroke="#1e293b" strokeWidth="1.8" strokeDasharray="3 3" strokeLinecap="round" />
                </g>
              );
            }

            // Single bond
            return (
              <line
                key={`b-${bond.id}`}
                x1={a1.x}
                y1={a1.y}
                x2={a2.x}
                y2={a2.y}
                stroke="#1e293b"
                strokeWidth="2.4"
                strokeLinecap="round"
              />
            );
          })}

          {/* ATOMS & NUMBERING */}
          {atoms.map(atom => {
            const rawPeak = peakMap.get(atom.id);
            // Strict element match guard: 13C carbon NMR shifts must ONLY attach to Carbon atoms, never Oxygen or heteroatoms
            const peak = (rawPeak && (rawPeak.atomSymbol === 'C' ? atom.symbol === 'C' : true)) ? rawPeak : null;
            const isSelected = selectedAtomIndex === atom.id;
            const isCarbon = atom.symbol === 'C';
            const color = getAtomColor(atom.symbol);

            return (
              <g
                key={`a-${atom.id}`}
                className="cursor-pointer transition-transform hover:opacity-90"
                onClick={() => onSelectAtom(atom.id)}
              >
                {/* Highlight ring for selected atom */}
                {isSelected && (
                  <circle
                    cx={atom.x}
                    cy={atom.y}
                    r={22}
                    fill="none"
                    stroke="#2563eb"
                    strokeWidth="2.5"
                    strokeDasharray="4 3"
                    className="animate-spin-slow"
                  />
                )}

                {/* Base circle background for readability */}
                <circle
                  cx={atom.x}
                  cy={atom.y}
                  r={isCarbon ? 14 : 16}
                  fill="#ffffff"
                  stroke={isSelected ? '#2563eb' : (peak ? '#059669' : '#94a3b8')}
                  strokeWidth={isSelected ? '2.5' : (peak ? '2' : '1.5')}
                  className="shadow-xs"
                />

                {/* Atom Element Symbol */}
                <text
                  x={atom.x}
                  y={atom.y + 4.5}
                  textAnchor="middle"
                  fill={color}
                  fontWeight={isCarbon ? 'bold' : '900'}
                  fontSize={isCarbon ? '12px' : '13px'}
                  fontFamily="system-ui, -apple-system, sans-serif"
                >
                  {atom.symbol}
                </text>

                {/* ATOM NUMBER BADGE (Top-right corner of atom) */}
                <g transform={`translate(${atom.x + 8}, ${atom.y - 14})`}>
                  <rect
                    x="0"
                    y="0"
                    width={atom.id >= 10 ? 17 : 14}
                    height="13"
                    rx="3"
                    fill={isSelected ? '#1d4ed8' : '#334155'}
                  />
                  <text
                    x={atom.id >= 10 ? 8.5 : 7}
                    y="9.5"
                    textAnchor="middle"
                    fill="#ffffff"
                    fontSize="9px"
                    fontWeight="800"
                    fontFamily="monospace"
                  >
                    {atom.id}
                  </text>
                </g>

                {/* CHEMICAL SHIFT PILL (Smartly positioned above or below carbon) */}
                {peak && (() => {
                  const offset = getShiftBadgeOffset(atom);
                  return (
                    <g transform={`translate(${atom.x + offset.x}, ${atom.y + offset.y})`}>
                      <rect
                        x="-25"
                        y="0"
                        width="50"
                        height="15"
                        rx="4"
                        fill={isSelected ? '#eff6ff' : '#ecfdf5'}
                        stroke={isSelected ? '#3b82f6' : '#10b981'}
                        strokeWidth="1.2"
                      />
                      <text
                        x="0"
                        y="10.5"
                        textAnchor="middle"
                        fill={isSelected ? '#1e40af' : '#065f46'}
                        fontSize="9.5px"
                        fontWeight="bold"
                        fontFamily="monospace"
                      >
                        {peak.shift.toFixed(1)} ppm
                      </text>
                    </g>
                  );
                })()}
              </g>
            );
          })}
        </svg>
      </div>

      <div className="mt-2 flex items-center justify-between text-[11px] text-slate-500">
        <span>Click any atom in the diagram to highlight its assignment in the table below.</span>
        <span>Atoms numbered 1..{atoms.length}</span>
      </div>
    </div>
  );
};
