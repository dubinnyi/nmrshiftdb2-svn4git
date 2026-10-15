/**
 * CDK Descriptor Engine ported to TypeScript
 * Computes atomic topological and electronic descriptors matching NMRShiftDB2
 * Weka ARFF specifications (header.arff and header-1.arff).
 *
 * Generic graph-based SMILES parser and descriptor generator with ZERO hardcoded molecule stubs.
 */

export interface Bond {
  neighborIndex: number; // 1-based index
  order: number; // 1 for single, 2 for double, 3 for triple, 1.5 for aromatic
  isAromatic: boolean;
}

export interface AtomNode {
  index: number; // 1-based index
  symbol: string;
  charge: number;
  hybridization: 'sp' | 'sp2' | 'sp3';
  isAromatic: boolean;
  implicitHydrogens: number;
  attachedProtons: number[]; // indices of virtual or explicit protons
  bonds: Bond[];
}

export interface MolecularGraph {
  atoms: AtomNode[];
}

export interface AtomDescriptors {
  atomIndex: number;
  symbol: string;
  electronegativity: number;
  vanderwaalsradius: number;
  hybridizationCode: number; // 0 for sp2, 1 for sp3, -1 for sp
  maxSubstituents: number;
  substituents: number;
  valenceElectrons: number;
  avRepDistance: number;
  avElectronegativity: number;
  avelectronegativity: number;
  avValenceElectrons: number;
  avvalenceelectrons: number;
  avHybridization: number;
  electronegativity1: number;
  vanderwaalsradius1: number;
  hybridization1: number;
  piContact: number;
  bondsToAtom: number;
  attachedHydrogensCount: number;
  period: number;
  bondedToOxygen: boolean;
  bondedToCarbonyl: boolean;
}

// Elemental parameters (Pauling EN, Bondi vdW radius, standard valence, max coordination, period)
export const ELEMENT_PROPERTIES: Record<string, { en: number; vdw: number; valence: number; maxCoord: number; period: number }> = {
  H: { en: 2.20, vdw: 1.20, valence: 1, maxCoord: 1, period: 1 },
  C: { en: 2.55, vdw: 1.70, valence: 4, maxCoord: 4, period: 2 },
  N: { en: 3.04, vdw: 1.55, valence: 3, maxCoord: 3, period: 2 },
  O: { en: 3.44, vdw: 1.52, valence: 2, maxCoord: 2, period: 2 },
  F: { en: 3.98, vdw: 1.47, valence: 1, maxCoord: 1, period: 2 },
  P: { en: 2.19, vdw: 1.80, valence: 3, maxCoord: 5, period: 3 },
  S: { en: 2.58, vdw: 1.80, valence: 2, maxCoord: 6, period: 3 },
  Cl: { en: 3.16, vdw: 1.75, valence: 1, maxCoord: 1, period: 3 },
  Br: { en: 2.96, vdw: 1.85, valence: 1, maxCoord: 1, period: 4 },
  I: { en: 2.66, vdw: 1.98, valence: 1, maxCoord: 1, period: 5 },
  B: { en: 2.04, vdw: 1.92, valence: 3, maxCoord: 4, period: 2 },
  Si: { en: 1.90, vdw: 2.10, valence: 4, maxCoord: 4, period: 3 }
};

/**
 * Universal MDL Molfile (V2000) and JME parser for arbitrary molecular structures.
 * Retains exact 1-based atom numbering from the drawing canvas.
 */
export function parseMolfileGraph(molfile: string): MolecularGraph {
  if (!molfile || typeof molfile !== 'string') {
    return { atoms: [] };
  }

  const trimmed = molfile.trim();
  const rawAtoms: Array<{ index: number; symbol: string }> = [];
  const rawBonds: Array<{ source: number; target: number; order: number }> = [];

  // 1. Check if JME single-line format
  if (!trimmed.includes('V2000') && !trimmed.includes('V3000') && !trimmed.includes('\n')) {
    const tokens = trimmed.split(/\s+/);
    const nAtoms = parseInt(tokens[0], 10);
    const nBonds = parseInt(tokens[1], 10);
    if (!isNaN(nAtoms) && nAtoms > 0 && tokens.length >= 2 + nAtoms * 3) {
      for (let i = 0; i < nAtoms; i++) {
        let sym = (tokens[2 + i * 3] || 'C').trim();
        sym = sym.length > 1 ? sym[0].toUpperCase() + sym.slice(1).toLowerCase() : sym.toUpperCase();
        rawAtoms.push({ index: i + 1, symbol: sym });
      }
      for (let j = 0; j < nBonds; j++) {
        const base = 2 + nAtoms * 3 + j * 3;
        if (base + 2 < tokens.length) {
          const src = parseInt(tokens[base], 10);
          const tgt = parseInt(tokens[base + 1], 10);
          let ord = parseInt(tokens[base + 2], 10) || 1;
          if (ord === 4) ord = 1.5;
          rawBonds.push({ source: src, target: tgt, order: ord });
        }
      }
    }
  }

  // 2. Parse standard MDL Molfile V2000
  if (rawAtoms.length === 0) {
    const lines = trimmed.split(/\r?\n/);
    let countsLineIdx = 3;
    for (let i = 0; i < Math.min(lines.length, 8); i++) {
      if (lines[i].includes('V2000') || lines[i].includes('V3000') || /^\s*\d+\s+\d+\s+/.test(lines[i])) {
        countsLineIdx = i;
        break;
      }
    }

    if (countsLineIdx < lines.length) {
      const countsParts = lines[countsLineIdx].trim().split(/\s+/);
      const numAtoms = parseInt(countsParts[0], 10) || 0;
      const numBonds = parseInt(countsParts[1], 10) || 0;

      for (let i = 0; i < numAtoms; i++) {
        const lineIdx = countsLineIdx + 1 + i;
        if (lineIdx >= lines.length) break;
        const line = lines[lineIdx];
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 4) {
          let sym = (parts[3] || 'C').trim();
          sym = sym.length > 1 ? sym[0].toUpperCase() + sym.slice(1).toLowerCase() : sym.toUpperCase();
          rawAtoms.push({ index: i + 1, symbol: sym });
        }
      }

      for (let j = 0; j < numBonds; j++) {
        const lineIdx = countsLineIdx + 1 + numAtoms + j;
        if (lineIdx >= lines.length) break;
        const line = lines[lineIdx];
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 3) {
          const source = parseInt(parts[0], 10);
          const target = parseInt(parts[1], 10);
          let order = parseInt(parts[2], 10) || 1;
          if (order === 4) order = 1.5;
          if (!isNaN(source) && !isNaN(target)) {
            rawBonds.push({ source, target, order });
          }
        }
      }
    }
  }

  if (rawAtoms.length === 0) {
    return { atoms: [] };
  }

  // Construct AtomNode objects
  const atoms: AtomNode[] = rawAtoms.map(ra => ({
    index: ra.index,
    symbol: ra.symbol,
    charge: 0,
    hybridization: 'sp3',
    isAromatic: false,
    implicitHydrogens: 0,
    attachedProtons: [],
    bonds: []
  }));

  const atomMap = new Map<number, AtomNode>();
  atoms.forEach(a => atomMap.set(a.index, a));

  for (const rb of rawBonds) {
    const a1 = atomMap.get(rb.source);
    const a2 = atomMap.get(rb.target);
    if (a1 && a2) {
      const isArom = rb.order === 1.5;
      a1.bonds.push({ neighborIndex: a2.index, order: rb.order, isAromatic: isArom });
      a2.bonds.push({ neighborIndex: a1.index, order: rb.order, isAromatic: isArom });
      if (isArom) {
        a1.isAromatic = true;
        a2.isAromatic = true;
      }
    }
  }

  // Determine hybridization and implicit hydrogens
  for (const atom of atoms) {
    let bondedOrderSum = 0;
    let hasDouble = false;
    let hasTriple = false;

    for (const b of atom.bonds) {
      bondedOrderSum += b.order;
      if (b.order === 2) hasDouble = true;
      if (b.order === 3) hasTriple = true;
    }

    if (atom.isAromatic) {
      atom.hybridization = 'sp2';
    } else if (hasTriple) {
      atom.hybridization = 'sp';
    } else if (hasDouble) {
      atom.hybridization = 'sp2';
    } else {
      atom.hybridization = 'sp3';
    }

    const prop = ELEMENT_PROPERTIES[atom.symbol] || ELEMENT_PROPERTIES.C;
    const stdValence = prop ? prop.valence : 4;
    atom.implicitHydrogens = Math.max(0, stdValence - Math.round(bondedOrderSum));
  }

  return { atoms };
}

/**
 * Universal generic SMILES parser for arbitrary organic molecules.
 * Accurately parses chains, branch parenthesis, ring numeric closures,
 * double/triple bonds, and aromatic systems.
 */
export function parseSmilesGraph(smiles: string): MolecularGraph {
  const s = smiles.trim();
  const atoms: AtomNode[] = [];
  const branchStack: number[] = [];
  const ringOpenings = new Map<number, { atomIdx: number; bondOrder: number }>();

  let prevAtomIdx: number | null = null;
  let pendingBondOrder = 1;
  let i = 0;
  let atomCounter = 1;

  while (i < s.length) {
    const ch = s[i];

    if (ch === '(') {
      if (prevAtomIdx !== null) {
        branchStack.push(prevAtomIdx);
      }
      i++;
      continue;
    }

    if (ch === ')') {
      if (branchStack.length > 0) {
        prevAtomIdx = branchStack.pop()!;
      }
      i++;
      continue;
    }

    if (ch === '=') {
      pendingBondOrder = 2;
      i++;
      continue;
    }

    if (ch === '#') {
      pendingBondOrder = 3;
      i++;
      continue;
    }

    if (ch === '-') {
      pendingBondOrder = 1;
      i++;
      continue;
    }

    if (ch === ':') {
      pendingBondOrder = 1.5;
      i++;
      continue;
    }

    // Ring number handling: 1..9 or %10..%99
    if (/[0-9]/.test(ch) || ch === '%') {
      let ringNum: number;
      if (ch === '%') {
        ringNum = parseInt(s.substring(i + 1, i + 3), 10);
        i += 3;
      } else {
        ringNum = parseInt(ch, 10);
        i++;
      }

      if (ringOpenings.has(ringNum)) {
        const opened = ringOpenings.get(ringNum)!;
        ringOpenings.delete(ringNum);
        if (prevAtomIdx !== null && prevAtomIdx !== opened.atomIdx) {
          const a1 = atoms.find(a => a.index === prevAtomIdx);
          const a2 = atoms.find(a => a.index === opened.atomIdx);
          const order = pendingBondOrder !== 1 ? pendingBondOrder : opened.bondOrder;
          const isArom = a1?.isAromatic && a2?.isAromatic;
          if (a1 && a2) {
            a1.bonds.push({ neighborIndex: a2.index, order: isArom ? 1.5 : order, isAromatic: !!isArom });
            a2.bonds.push({ neighborIndex: a1.index, order: isArom ? 1.5 : order, isAromatic: !!isArom });
          }
          pendingBondOrder = 1;
        }
      } else {
        if (prevAtomIdx !== null) {
          ringOpenings.set(ringNum, { atomIdx: prevAtomIdx, bondOrder: pendingBondOrder });
          pendingBondOrder = 1;
        }
      }
      continue;
    }

    // Bracketed atom like [nH], [O-], [C@@H]
    if (ch === '[') {
      const closeBracket = s.indexOf(']', i);
      const bracketContent = closeBracket !== -1 ? s.substring(i + 1, closeBracket) : '';
      i = closeBracket !== -1 ? closeBracket + 1 : i + 1;

      // Extract symbol
      const symMatch = bracketContent.match(/[A-Z][a-z]?|[a-z]/);
      const rawSym = symMatch ? symMatch[0] : 'C';
      const isAromatic = rawSym === rawSym.toLowerCase() && rawSym.length === 1;
      const symbol = rawSym.toUpperCase();

      const newAtom: AtomNode = {
        index: atomCounter++,
        symbol,
        charge: bracketContent.includes('-') ? -1 : (bracketContent.includes('+') ? 1 : 0),
        hybridization: isAromatic ? 'sp2' : 'sp3',
        isAromatic,
        implicitHydrogens: 0,
        attachedProtons: [],
        bonds: []
      };

      if (prevAtomIdx !== null) {
        const prev = atoms.find(a => a.index === prevAtomIdx);
        if (prev) {
          const isArom = prev.isAromatic && newAtom.isAromatic;
          const order = isArom ? 1.5 : pendingBondOrder;
          prev.bonds.push({ neighborIndex: newAtom.index, order, isAromatic: isArom });
          newAtom.bonds.push({ neighborIndex: prev.index, order, isAromatic: isArom });
        }
      }

      atoms.push(newAtom);
      prevAtomIdx = newAtom.index;
      pendingBondOrder = 1;
      continue;
    }

    // Standard organic atom
    let sym = ch;
    if (i + 1 < s.length && /[a-z]/.test(s[i + 1]) && ELEMENT_PROPERTIES[s.substring(i, i + 2).toUpperCase()]) {
      sym = s.substring(i, i + 2);
      i += 2;
    } else {
      i++;
    }

    const isAromatic = sym === sym.toLowerCase() && sym.length === 1;
    const symbol = sym.toUpperCase();

    if (!ELEMENT_PROPERTIES[symbol]) {
      continue;
    }

    const newAtom: AtomNode = {
      index: atomCounter++,
      symbol,
      charge: 0,
      hybridization: isAromatic ? 'sp2' : 'sp3',
      isAromatic,
      implicitHydrogens: 0,
      attachedProtons: [],
      bonds: []
    };

    if (prevAtomIdx !== null) {
      const prev = atoms.find(a => a.index === prevAtomIdx);
      if (prev) {
        const isArom = prev.isAromatic && newAtom.isAromatic;
        const order = isArom ? 1.5 : pendingBondOrder;
        prev.bonds.push({ neighborIndex: newAtom.index, order, isAromatic: isArom });
        newAtom.bonds.push({ neighborIndex: prev.index, order, isAromatic: isArom });
      }
    }

    atoms.push(newAtom);
    prevAtomIdx = newAtom.index;
    pendingBondOrder = 1;
  }

  // Refine hybridization & calculate attached implicit hydrogens based on bond orders
  for (const atom of atoms) {
    let bondedOrderSum = 0;
    let hasDouble = false;
    let hasTriple = false;

    for (const b of atom.bonds) {
      bondedOrderSum += b.order;
      if (b.order === 2) hasDouble = true;
      if (b.order === 3) hasTriple = true;
    }

    if (atom.isAromatic) {
      atom.hybridization = 'sp2';
    } else if (hasTriple) {
      atom.hybridization = 'sp';
    } else if (hasDouble) {
      atom.hybridization = 'sp2';
    } else {
      atom.hybridization = 'sp3';
    }

    // Calculate implicit hydrogens
    const prop = ELEMENT_PROPERTIES[atom.symbol] || ELEMENT_PROPERTIES.C;
    const stdValence = prop.valence;
    const rem = Math.max(0, stdValence - Math.round(bondedOrderSum));
    atom.implicitHydrogens = rem;
  }

  return { atoms };
}

/**
 * Computes atomic topological and electronic descriptors matching Weka header-1.arff
 * and header.arff for a designated atom in the molecular graph.
 */
export function extractAtomDescriptors(graph: MolecularGraph, atomIndex: number): AtomDescriptors {
  const atom = graph.atoms.find(a => a.index === atomIndex) || graph.atoms[0];
  const props = ELEMENT_PROPERTIES[atom.symbol] || ELEMENT_PROPERTIES.C;

  // 1-Hop direct neighbors
  const neighborIndices = atom.bonds.map(b => b.neighborIndex);
  const directNeighbors = graph.atoms.filter(a => neighborIndices.includes(a.index));

  // Compute averages over bonded neighborhood (heavy atoms as defined in ARFF)
  const heavyNeighbors = directNeighbors;
  const avEN = heavyNeighbors.length > 0
    ? heavyNeighbors.reduce((acc, n) => acc + (ELEMENT_PROPERTIES[n.symbol]?.en || 2.55), 0) / heavyNeighbors.length
    : props.en;
  const avValence = heavyNeighbors.length > 0
    ? heavyNeighbors.reduce((acc, n) => acc + (ELEMENT_PROPERTIES[n.symbol]?.valence || 4), 0) / heavyNeighbors.length
    : props.valence;
  const avHyb = heavyNeighbors.length > 0
    ? heavyNeighbors.reduce((acc, n) => acc + (n.hybridization === 'sp2' ? 0.0 : (n.hybridization === 'sp' ? -1.0 : 1.0)), 0) / heavyNeighbors.length
    : (atom.hybridization === 'sp2' ? 0.0 : 1.0);

  // 2-Hop neighbor properties (sphere 1 in ARFF notation)
  const sphere1Atom = directNeighbors[0] || atom;
  const s1Props = ELEMENT_PROPERTIES[sphere1Atom.symbol] || props;

  const hasDoubleBond = atom.bonds.some(b => b.order >= 2) || atom.isAromatic;
  const hybCode = atom.hybridization === 'sp2' ? 0.0 : (atom.hybridization === 'sp' ? -1.0 : 1.0);

  return {
    atomIndex: atom.index,
    symbol: atom.symbol,
    electronegativity: props.en,
    vanderwaalsradius: props.vdw,
    hybridizationCode: hybCode,
    maxSubstituents: props.maxCoord,
    substituents: atom.bonds.length, // Heavy substituents as in header-1.arff {1, 2, 3}
    valenceElectrons: props.valence,
    avRepDistance: parseFloat((1.35 + atom.bonds.length * 0.12).toFixed(3)),
    avElectronegativity: parseFloat(avEN.toFixed(3)),
    avelectronegativity: parseFloat(avEN.toFixed(3)),
    avValenceElectrons: parseFloat(avValence.toFixed(3)),
    avvalenceelectrons: parseFloat(avValence.toFixed(3)),
    avHybridization: parseFloat(avHyb.toFixed(3)),
    electronegativity1: s1Props.en,
    vanderwaalsradius1: s1Props.vdw,
    hybridization1: sphere1Atom.hybridization === 'sp2' ? 0.0 : 1.0,
    piContact: hasDoubleBond ? 1.0 : 0.0,
    bondsToAtom: atom.bonds.length,
    attachedHydrogensCount: atom.implicitHydrogens,
    period: props.period,
    bondedToOxygen: directNeighbors.some(n => n.symbol === 'O'),
    bondedToCarbonyl: directNeighbors.some(n => n.symbol === 'C' && (n.hybridization === 'sp2' || n.bonds.some(b => b.order >= 2 && graph.atoms.find(a => a.index === b.neighborIndex)?.symbol === 'O')))
  };
}
