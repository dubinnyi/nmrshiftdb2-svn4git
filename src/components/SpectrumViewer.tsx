import React, { useState } from 'react';

export interface Peak {
  atomIndex: number;
  atomSymbol: string;
  shift: number;
  multiplicity?: string;
  intensity?: number;
  hoseCode?: string;
  spheres?: number;
  confidence?: number;
  uncertainty?: number;
  method?: string;
  source?: string;
  descriptors?: any;
  activations?: any;
}

interface SpectrumViewerProps {
  nucleus: string;
  peaks: Peak[];
  title?: string;
}

export const SpectrumViewer: React.FC<SpectrumViewerProps> = ({ nucleus, peaks, title }) => {
  const [hoveredPeak, setHoveredPeak] = useState<Peak | null>(null);

  const is13C = nucleus.toUpperCase().includes('13C');
  // Typical NMR PPM ranges: 13C: 0 - 220 ppm; 1H: 0 - 12 ppm
  const minPpm = 0;
  const maxPpm = is13C ? 220 : 12;

  // In NMR, high ppm is on the left (downfield/deshielded), 0 ppm is on the right (TMS reference)!
  const ppmToPercent = (ppm: number) => {
    const clamped = Math.max(minPpm, Math.min(maxPpm, ppm));
    return ((maxPpm - clamped) / (maxPpm - minPpm)) * 100;
  };

  const ticks = is13C
    ? [220, 200, 180, 160, 140, 120, 100, 80, 60, 40, 20, 0]
    : [12, 10, 8, 6, 4, 2, 0];

  return (
    <div className="bg-slate-900 text-slate-100 rounded-xl p-5 border border-slate-800 shadow-md">
      <div className="flex flex-wrap items-center justify-between gap-2 mb-4 pb-3 border-b border-slate-800">
        <div>
          <h3 className="font-semibold text-lg text-emerald-400 flex items-center gap-2">
            <span>{nucleus} NMR Spectrum</span>
            {title && <span className="text-sm font-normal text-slate-400">— {title}</span>}
          </h3>
          <p className="text-xs text-slate-400">Chemical shift scale in ppm (δ), relative to TMS (0.0 ppm)</p>
        </div>
        <div className="flex items-center gap-3 text-xs bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700">
          <span className="text-emerald-400 font-medium">{peaks.length} Signals</span>
          <span className="text-slate-400">|</span>
          <span className="text-slate-300">Downfield &larr; Deshielded | Shielded &rarr; Upfield</span>
        </div>
      </div>

      {/* Spectrum Chart Area */}
      <div className="relative h-64 bg-slate-950 rounded-lg p-4 border border-slate-800/80 overflow-hidden select-none">
        {/* Grid lines */}
        <div className="absolute inset-x-4 inset-y-8 flex justify-between pointer-events-none opacity-15">
          {ticks.map((t) => (
            <div key={t} className="h-full border-r border-dashed border-slate-400 w-0" />
          ))}
        </div>

        {/* Baseline */}
        <div className="absolute bottom-10 left-4 right-4 h-0.5 bg-emerald-500/40" />

        {/* Peaks */}
        <div className="absolute inset-x-4 top-4 bottom-10">
          {peaks.map((p, idx) => {
            const leftPct = ppmToPercent(p.shift);
            const heightPct = Math.min(90, Math.max(35, (p.intensity || 1) * 35));
            const isHovered = hoveredPeak === p;

            return (
              <div
                key={idx}
                className="absolute bottom-0 -translate-x-1/2 flex flex-col items-center cursor-pointer group transition-all"
                style={{ left: `${leftPct}%`, height: `${heightPct}%` }}
                onMouseEnter={() => setHoveredPeak(p)}
                onMouseLeave={() => setHoveredPeak(null)}
              >
                {/* Peak tag */}
                <div
                  className={`text-[10px] font-mono px-1.5 py-0.5 rounded transition-all whitespace-nowrap mb-1 ${
                    isHovered
                      ? 'bg-emerald-500 text-slate-950 font-bold scale-110 shadow-lg'
                      : 'text-emerald-400 bg-slate-900/90 border border-emerald-500/30 group-hover:border-emerald-400'
                  }`}
                >
                  {p.shift.toFixed(2)}
                </div>

                {/* Vertical Stick / Signal Peak */}
                <div
                  className={`w-0.5 flex-1 transition-colors ${
                    isHovered ? 'bg-emerald-400 w-1 shadow-[0_0_8px_#10b981]' : 'bg-emerald-500/80 group-hover:bg-emerald-300'
                  }`}
                />

                {/* Multiplicity indicator */}
                {p.multiplicity && (
                  <span className="text-[9px] text-slate-400 font-mono mt-1 opacity-70 group-hover:opacity-100">
                    {p.multiplicity}
                  </span>
                )}
              </div>
            );
          })}
        </div>

        {/* X-Axis Scale */}
        <div className="absolute bottom-2 left-4 right-4 flex justify-between text-[11px] font-mono text-slate-400 border-t border-slate-800 pt-1">
          {ticks.map((t) => (
            <div key={t} className="flex flex-col items-center">
              <span className="h-1 w-px bg-slate-700 -mt-1 mb-0.5" />
              <span>{t}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Hover info / Peak metadata */}
      <div className="mt-3 min-h-[32px] flex items-center justify-between text-xs px-2 py-1 bg-slate-950/60 rounded border border-slate-800">
        {hoveredPeak ? (
          <div className="flex flex-wrap items-center gap-4 text-slate-300">
            <span>
              <strong>Atom:</strong> {hoveredPeak.atomSymbol}{hoveredPeak.atomIndex}
            </span>
            <span>
              <strong>Shift:</strong> <span className="text-emerald-400 font-mono font-semibold">{hoveredPeak.shift.toFixed(2)} ppm</span>
            </span>
            {hoveredPeak.multiplicity && (
              <span>
                <strong>Multiplicity:</strong> {hoveredPeak.multiplicity}
              </span>
            )}
            {hoveredPeak.hoseCode && (
              <span>
                <strong>HOSE-Code:</strong> <code className="bg-slate-800 px-1.5 py-0.5 rounded text-emerald-300 font-mono text-[10px]">{hoveredPeak.hoseCode}</code>
              </span>
            )}
          </div>
        ) : (
          <span className="text-slate-500 italic">Hover over any peak to inspect chemical shift, atom index, and HOSE code.</span>
        )}
      </div>
    </div>
  );
};
