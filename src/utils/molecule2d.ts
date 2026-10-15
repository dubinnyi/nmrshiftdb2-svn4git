export interface EditorAtom {
  id: number;
  symbol: string;
  x: number;
  y: number;
  charge?: number;
  aromatic?: boolean;
}

export interface EditorBond {
  id: number;
  source: number; // atom id
  target: number; // atom id
  order: number; // 1 = single, 2 = double, 3 = triple, 1.5 = aromatic
  aromatic?: boolean;
}

export interface MoleculeGraph2D {
  atoms: EditorAtom[];
  bonds: EditorBond[];
}

/**
 * Parses MDL Molfile (SDF) or JME string into a 2D graph with exact JSME coordinates.
 */
export function parseMolfileTo2D(
  molfile: string,
  width = 580,
  height = 240
): MoleculeGraph2D {
  if (!molfile || typeof molfile !== 'string') {
    return { atoms: [], bonds: [] };
  }

  const trimmed = molfile.trim();
  const rawAtoms: Array<{ id: number; symbol: string; x: number; y: number }> = [];
  const rawBonds: Array<{ id: number; source: number; target: number; order: number }> = [];

  // Check if JME format (single line with space-separated tokens: natoms nbonds sym1 x1 y1 ...)
  if (!trimmed.includes('V2000') && !trimmed.includes('V3000') && !trimmed.includes('\n')) {
    const tokens = trimmed.split(/\s+/);
    const nAtoms = parseInt(tokens[0], 10);
    const nBonds = parseInt(tokens[1], 10);
    if (!isNaN(nAtoms) && nAtoms > 0 && tokens.length >= 2 + nAtoms * 3) {
      for (let i = 0; i < nAtoms; i++) {
        const sym = tokens[2 + i * 3] || 'C';
        const x = parseFloat(tokens[2 + i * 3 + 1]);
        const y = parseFloat(tokens[2 + i * 3 + 2]);
        if (!isNaN(x) && !isNaN(y)) {
          rawAtoms.push({ id: i + 1, symbol: sym, x, y });
        }
      }
      for (let j = 0; j < nBonds; j++) {
        const base = 2 + nAtoms * 3 + j * 3;
        if (base + 2 < tokens.length) {
          const src = parseInt(tokens[base], 10);
          const tgt = parseInt(tokens[base + 1], 10);
          const ord = parseInt(tokens[base + 2], 10) || 1;
          rawBonds.push({ id: j + 1, source: src, target: tgt, order: ord });
        }
      }
    }
  }

  // If not parsed via JME format, parse as standard MDL Molfile V2000
  if (rawAtoms.length === 0) {
    const lines = trimmed.split(/\r?\n/);
    if (lines.length >= 4) {
      let countsLineIdx = 3;
      for (let i = 0; i < Math.min(lines.length, 8); i++) {
        if (lines[i].includes('V2000') || lines[i].includes('V3000') || /^\s*\d+\s+\d+\s+/.test(lines[i])) {
          countsLineIdx = i;
          break;
        }
      }

      const countsParts = lines[countsLineIdx].trim().split(/\s+/);
      const numAtoms = parseInt(countsParts[0], 10) || 0;
      const numBonds = parseInt(countsParts[1], 10) || 0;

      for (let i = 0; i < numAtoms; i++) {
        const lineIdx = countsLineIdx + 1 + i;
        if (lineIdx >= lines.length) break;
        const line = lines[lineIdx];
        // Match numbers and symbol: e.g. " -1.2990 0.7500 0.0000 O "
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 4) {
          const x = parseFloat(parts[0]);
          const y = parseFloat(parts[1]);
          const symbol = parts[3] || 'C';
          if (!isNaN(x) && !isNaN(y)) {
            rawAtoms.push({ id: i + 1, symbol, x, y });
          }
        }
      }

      for (let i = 0; i < numBonds; i++) {
        const lineIdx = countsLineIdx + 1 + numAtoms + i;
        if (lineIdx >= lines.length) break;
        const line = lines[lineIdx];
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 3) {
          const source = parseInt(parts[0], 10);
          const target = parseInt(parts[1], 10);
          let order = parseInt(parts[2], 10) || 1;
          if (order === 4) order = 1.5;
          if (!isNaN(source) && !isNaN(target)) {
            rawBonds.push({ id: i + 1, source, target, order });
          }
        }
      }
    }
  }

  if (rawAtoms.length === 0) return { atoms: [], bonds: [] };

  // Calculate bounding box
  let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
  rawAtoms.forEach(a => {
    if (a.x < minX) minX = a.x;
    if (a.x > maxX) maxX = a.x;
    if (a.y < minY) minY = a.y;
    if (a.y > maxY) maxY = a.y;
  });

  const spanX = Math.max(maxX - minX, 0.4);
  const spanY = Math.max(maxY - minY, 0.4);

  // Safe padding for atom circles, badges, and labels
  const paddingX = 65;
  const paddingY = 45;
  const targetW = width - 2 * paddingX;
  const targetH = height - 2 * paddingY;

  // Preserve aspect ratio cleanly
  let scale = Math.min(targetW / spanX, targetH / spanY);
  // Cap scale so small molecules don't become enormous, and large molecules remain legible
  scale = Math.min(scale, 75);
  scale = Math.max(scale, 18);

  const cx = (minX + maxX) / 2;
  const cy = (minY + maxY) / 2;

  const atoms: EditorAtom[] = rawAtoms.map(a => ({
    id: a.id,
    symbol: a.symbol,
    x: Math.round(width / 2 + (a.x - cx) * scale),
    // Invert Cartesian Y for SVG coordinates
    y: Math.round(height / 2 - (a.y - cy) * scale)
  }));

  return { atoms, bonds: rawBonds };
}

/**
 * Parses organic SMILES into a 2D graph with layout coordinates.
 */
export function parseSmilesTo2D(
  smiles: string,
  width = 380,
  height = 240
): MoleculeGraph2D {
  const s = smiles.trim();
  if (!s) return { atoms: [], bonds: [] };

  const rawAtoms: Array<{ id: number; symbol: string; aromatic: boolean }> = [];
  const rawBonds: Array<{ source: number; target: number; order: number; aromatic?: boolean }> = [];

  const branchStack: number[] = [];
  const ringOpenings = new Map<number, { atomId: number; bondOrder: number }>();

  let prevAtomId: number | null = null;
  let pendingBondOrder = 1;
  let i = 0;
  let atomCounter = 1;

  while (i < s.length) {
    const ch = s[i];

    if (ch === '(') {
      if (prevAtomId !== null) {
        branchStack.push(prevAtomId);
      }
      i++;
      continue;
    }

    if (ch === ')') {
      if (branchStack.length > 0) {
        prevAtomId = branchStack.pop()!;
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

    // Ring closures (1..9 or %10..%99)
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
        const opening = ringOpenings.get(ringNum)!;
        ringOpenings.delete(ringNum);
        if (prevAtomId !== null && opening.atomId !== prevAtomId) {
          rawBonds.push({
            source: opening.atomId,
            target: prevAtomId,
            order: pendingBondOrder !== 1 ? pendingBondOrder : opening.bondOrder
          });
          pendingBondOrder = 1;
        }
      } else {
        if (prevAtomId !== null) {
          ringOpenings.set(ringNum, { atomId: prevAtomId, bondOrder: pendingBondOrder });
          pendingBondOrder = 1;
        }
      }
      continue;
    }

    // Bracketed atom [nH], [O-], [NH4+], etc.
    if (ch === '[') {
      const endBracket = s.indexOf(']', i);
      if (endBracket !== -1) {
        const content = s.substring(i + 1, endBracket);
        const match = content.match(/([A-Z][a-z]?|[a-z])/);
        let sym = match ? match[1] : 'C';
        const isArom = sym === sym.toLowerCase() && sym.length === 1;
        sym = isArom ? sym.toUpperCase() : sym;

        const currId = atomCounter++;
        rawAtoms.push({ id: currId, symbol: sym, aromatic: isArom });

        if (prevAtomId !== null) {
          rawBonds.push({
            source: prevAtomId,
            target: currId,
            order: pendingBondOrder,
            aromatic: isArom
          });
          pendingBondOrder = 1;
        }
        prevAtomId = currId;
        i = endBracket + 1;
        continue;
      }
    }

    // Two-letter elements
    let sym = '';
    let isArom = false;
    if (i + 1 < s.length && (s.substring(i, i + 2) === 'Cl' || s.substring(i, i + 2) === 'Br' || s.substring(i, i + 2) === 'Si')) {
      sym = s.substring(i, i + 2);
      i += 2;
    } else if (/[A-Z]/.test(ch)) {
      sym = ch;
      i++;
    } else if (/[a-z]/.test(ch)) {
      sym = ch.toUpperCase();
      isArom = true;
      i++;
    } else {
      // Skip unrecognized character
      i++;
      continue;
    }

    const currId = atomCounter++;
    rawAtoms.push({ id: currId, symbol: sym, aromatic: isArom });

    if (prevAtomId !== null) {
      rawBonds.push({
        source: prevAtomId,
        target: currId,
        order: pendingBondOrder,
        aromatic: isArom
      });
      pendingBondOrder = 1;
    }
    prevAtomId = currId;
  }

  // Layout 2D coordinates
  const atomsWithCoords = layoutMolecule2D(rawAtoms, rawBonds, width, height);

  const finalBonds: EditorBond[] = rawBonds.map((b, idx) => ({
    id: idx + 1,
    source: b.source,
    target: b.target,
    order: b.order,
    aromatic: b.aromatic
  }));

  return { atoms: atomsWithCoords, bonds: finalBonds };
}

/**
 * 2D coordinate generation using DFS chain angle progression and relaxation.
 */
export function layoutMolecule2D(
  rawAtoms: Array<{ id: number; symbol: string; aromatic?: boolean }>,
  rawBonds: Array<{ source: number; target: number; order?: number }>,
  width = 380,
  height = 240
): EditorAtom[] {
  if (rawAtoms.length === 0) return [];

  // Build adjacency
  const adj = new Map<number, number[]>();
  rawAtoms.forEach(a => adj.set(a.id, []));
  rawBonds.forEach(b => {
    adj.get(b.source)?.push(b.target);
    adj.get(b.target)?.push(b.source);
  });

  const positions = new Map<number, { x: number; y: number }>();
  const visited = new Set<number>();

  const bondLength = 40;

  // Find start atom (preferably atom with degree 1 or first atom)
  let startAtom = rawAtoms[0].id;
  for (const a of rawAtoms) {
    if ((adj.get(a.id)?.length || 0) === 1) {
      startAtom = a.id;
      break;
    }
  }

  positions.set(startAtom, { x: 0, y: 0 });
  visited.add(startAtom);

  const queue: Array<{ id: number; angle: number; step: number }> = [
    { id: startAtom, angle: 0, step: 0 }
  ];

  while (queue.length > 0) {
    const { id, angle, step } = queue.shift()!;
    const pos = positions.get(id)!;
    const neighbors = adj.get(id) || [];
    const unvisited = neighbors.filter(n => !visited.has(n));

    if (unvisited.length === 0) continue;

    if (unvisited.length === 1) {
      const nextId = unvisited[0];
      const zigzagSign = step % 2 === 0 ? 1 : -1;
      const nextAngle = zigzagSign * (Math.PI / 6);
      positions.set(nextId, {
        x: pos.x + bondLength * Math.cos(nextAngle),
        y: pos.y + bondLength * Math.sin(nextAngle)
      });
      visited.add(nextId);
      queue.push({ id: nextId, angle: nextAngle, step: step + 1 });
    } else {
      // Prioritize main chain continuation (carbon or atom with higher degree)
      const sorted = [...unvisited].sort((a, b) => {
        const degA = adj.get(a)?.length || 0;
        const degB = adj.get(b)?.length || 0;
        const symA = rawAtoms.find(at => at.id === a)?.symbol === 'C' ? 1 : 0;
        const symB = rawAtoms.find(at => at.id === b)?.symbol === 'C' ? 1 : 0;
        return (degB + symB * 2) - (degA + symA * 2);
      });

      const mainChainNext = sorted[0];
      const substituents = sorted.slice(1);

      // Main chain proceeds horizontally with alternating zig-zag
      const zigzagSign = step % 2 === 0 ? 1 : -1;
      const nextAngle = zigzagSign * (Math.PI / 6);
      positions.set(mainChainNext, {
        x: pos.x + bondLength * Math.cos(nextAngle),
        y: pos.y + bondLength * Math.sin(nextAngle)
      });
      visited.add(mainChainNext);
      queue.push({ id: mainChainNext, angle: nextAngle, step: step + 1 });

      // Substituents branch perpendicularly away from the zig-zag vertex
      substituents.forEach((subId, subIdx) => {
        const subAngle = zigzagSign > 0
          ? -Math.PI / 2 + (subIdx * 0.4)
          : Math.PI / 2 - (subIdx * 0.4);
        positions.set(subId, {
          x: pos.x + bondLength * Math.cos(subAngle),
          y: pos.y + bondLength * Math.sin(subAngle)
        });
        visited.add(subId);
        queue.push({ id: subId, angle: subAngle, step: step + 1 });
      });
    }
  }

  // Handle any disconnected components
  rawAtoms.forEach(a => {
    if (!positions.has(a.id)) {
      positions.set(a.id, { x: 0, y: 0 });
    }
  });

  // Simple force relaxation (15 iterations) to avoid overlaps and equalize lengths
  const coords = Array.from(positions.entries()).map(([id, p]) => ({
    id,
    x: p.x,
    y: p.y
  }));

  for (let iter = 0; iter < 15; iter++) {
    for (let j = 0; j < coords.length; j++) {
      for (let k = j + 1; k < coords.length; k++) {
        const dx = coords[k].x - coords[j].x;
        const dy = coords[k].y - coords[j].y;
        const dist = Math.hypot(dx, dy) || 0.1;
        if (dist < 32) {
          const repel = (32 - dist) / 2;
          const nx = dx / dist;
          const ny = dy / dist;
          coords[j].x -= nx * repel;
          coords[j].y -= ny * repel;
          coords[k].x += nx * repel;
          coords[k].y += ny * repel;
        }
      }
    }
  }

  // Find bounding box
  let minX = Infinity;
  let maxX = -Infinity;
  let minY = Infinity;
  let maxY = -Infinity;

  coords.forEach(c => {
    if (c.x < minX) minX = c.x;
    if (c.x > maxX) maxX = c.x;
    if (c.y < minY) minY = c.y;
    if (c.y > maxY) maxY = c.y;
  });

  const spanX = Math.max(maxX - minX, 20);
  const spanY = Math.max(maxY - minY, 20);

  const padding = 45;
  const targetW = width - 2 * padding;
  const targetH = height - 2 * padding;

  const scale = Math.min(1.2, Math.min(targetW / spanX, targetH / spanY));
  const cx = (minX + maxX) / 2;
  const cy = (minY + maxY) / 2;

  const coordMap = new Map<number, { x: number; y: number }>();
  coords.forEach(c => {
    coordMap.set(c.id, {
      x: Math.round(width / 2 + (c.x - cx) * scale),
      y: Math.round(height / 2 + (c.y - cy) * scale)
    });
  });

  return rawAtoms.map(a => ({
    id: a.id,
    symbol: a.symbol,
    x: coordMap.get(a.id)?.x || width / 2,
    y: coordMap.get(a.id)?.y || height / 2,
    aromatic: a.aromatic
  }));
}

/**
 * Converts a 2D editor graph back into a standard organic SMILES string.
 */
export function graphToSmiles(atoms: EditorAtom[], bonds: EditorBond[]): string {
  if (atoms.length === 0) return '';
  if (atoms.length === 1) return atoms[0].symbol;

  // Build adjacency
  const adj = new Map<number, Array<{ target: number; order: number; bondId: number }>>();
  atoms.forEach(a => adj.set(a.id, []));
  bonds.forEach(b => {
    adj.get(b.source)?.push({ target: b.target, order: b.order, bondId: b.id });
    adj.get(b.target)?.push({ target: b.source, order: b.order, bondId: b.id });
  });

  const visitedAtoms = new Set<number>();
  const visitedBonds = new Set<number>();
  const atomRingNumber = new Map<number, number>();
  let nextRingNumber = 1;

  // Start with atom with degree 1 or lowest id
  let startAtom = atoms[0].id;
  for (const a of atoms) {
    if ((adj.get(a.id)?.length || 0) === 1) {
      startAtom = a.id;
      break;
    }
  }

  function getBondSymbol(order: number): string {
    if (order === 2) return '=';
    if (order === 3) return '#';
    return '';
  }

  function getAtomString(atom: EditorAtom): string {
    let sym = atom.symbol;
    if (atom.aromatic) sym = sym.toLowerCase();
    return sym;
  }

  function dfs(currId: number): string {
    visitedAtoms.add(currId);
    const atom = atoms.find(a => a.id === currId)!;
    let res = getAtomString(atom);

    const neighbors = adj.get(currId) || [];
    const unvisitedEdges: Array<{ target: number; order: number; bondId: number }> = [];

    for (const edge of neighbors) {
      if (visitedBonds.has(edge.bondId)) continue;
      visitedBonds.add(edge.bondId);

      if (visitedAtoms.has(edge.target)) {
        // Ring closure!
        let rNum = atomRingNumber.get(edge.target);
        if (!rNum) {
          rNum = nextRingNumber++;
          atomRingNumber.set(edge.target, rNum);
        }
        res += getBondSymbol(edge.order) + rNum.toString();
      } else {
        unvisitedEdges.push(edge);
      }
    }

    if (atomRingNumber.has(currId)) {
      res += atomRingNumber.get(currId)!.toString();
    }

    if (unvisitedEdges.length === 0) {
      return res;
    }

    // Traverse children: branches in ( ... ) except last one
    for (let i = 0; i < unvisitedEdges.length; i++) {
      const edge = unvisitedEdges[i];
      const branchRes = getBondSymbol(edge.order) + dfs(edge.target);
      if (i < unvisitedEdges.length - 1) {
        res += `(${branchRes})`;
      } else {
        res += branchRes;
      }
    }

    return res;
  }

  return dfs(startAtom);
}
