export interface NMRPeak {
  atomIndex: number;
  atomSymbol: string;
  shift: number; // ppm
  multiplicity?: string; // s, d, t, q, m
  intensity?: number;
  hoseCode?: string;
  spheres?: number;
  confidence?: number; // e.g. 96.5% for NN
  uncertainty?: number; // ± ppm
  method?: string; // '13C NN' | '13C (using HOSE code)' | etc.
  source?: string;
  descriptors?: any;
  activations?: any;
}

export interface NMRSpectrum {
  id: string;
  nucleus: string; // '13C' | '1H' | '15N' | '31P' | '19F'
  frequencyMHz: number;
  solvent: string;
  temperatureK?: number;
  peaks: NMRPeak[];
  citation?: string;
}

export interface MoleculeRecord {
  id: string;
  name: string;
  formula: string;
  smiles: string;
  canonicalSmiles: string;
  inchi: string;
  inchiKey: string;
  cas?: string;
  molecularWeight: number;
  spectra: NMRSpectrum[];
  description?: string;
  dateAdded: string;
  contributor: string;
}

export const MOLECULES_DATABASE: MoleculeRecord[] = [
  {
    id: "10027412",
    name: "Ethyl Acetate",
    formula: "C4H8O2",
    smiles: "CCOC(=O)C",
    canonicalSmiles: "CCOC(=O)C",
    inchi: "InChI=1S/C4H8O2/c1-3-6-4(2)5/h3H2,1-2H3",
    inchiKey: "XEKOWRVHYACXOJ-UHFFFAOYSA-N",
    cas: "141-78-6",
    molecularWeight: 88.11,
    dateAdded: "2024-02-18",
    contributor: "maxim.dubinnyi",
    description: "Common ester featuring ester carbonyl, ethoxy methylene and acetate methyl.",
    spectra: [
      {
        id: "10088409",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 38.19, multiplicity: "q", method: "13C NN", confidence: 98.4, hoseCode: "C-4;(C/C=O)" },
          { atomIndex: 2, atomSymbol: "C", shift: 161.72, multiplicity: "s", method: "13C NN", confidence: 99.1, hoseCode: "C-3;(O=C-O)" },
          { atomIndex: 5, atomSymbol: "C", shift: 76.85, multiplicity: "t", method: "13C NN", confidence: 97.9, hoseCode: "C-4;(O/C)" },
          { atomIndex: 6, atomSymbol: "C", shift: 48.46, multiplicity: "q", method: "13C NN", confidence: 98.6, hoseCode: "C-4;(C/O)" }
        ]
      },
      {
        id: "10088410",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 1.25, multiplicity: "t", intensity: 3, method: "1H NN", confidence: 96.5 },
          { atomIndex: 2, atomSymbol: "H", shift: 4.12, multiplicity: "q", intensity: 2, method: "1H NN", confidence: 97.2 },
          { atomIndex: 3, atomSymbol: "H", shift: 2.03, multiplicity: "s", intensity: 3, method: "1H NN", confidence: 98.0 }
        ]
      }
    ]
  },
  {
    id: "10027408",
    name: "Benzene",
    formula: "C6H6",
    smiles: "c1ccccc1",
    canonicalSmiles: "c1ccccc1",
    inchi: "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
    inchiKey: "UHOVQNZJYSORNB-UHFFFAOYSA-N",
    cas: "71-43-2",
    molecularWeight: 78.11,
    dateAdded: "2024-01-15",
    contributor: "nmrshiftdb-curator",
    description: "Standard aromatic reference compound with equivalent carbons and protons.",
    spectra: [
      {
        id: "10088401",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Chloroform-D1 (CDCl3)",
        citation: "J. Magn. Reson. 1978, 32, 29-37",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 },
          { atomIndex: 2, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 },
          { atomIndex: 3, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 },
          { atomIndex: 4, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 },
          { atomIndex: 5, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 },
          { atomIndex: 6, atomSymbol: "C", shift: 128.5, multiplicity: "s", hoseCode: "C-6*(C//)", spheres: 4, method: "13C NN", confidence: 99.4 }
        ]
      },
      {
        id: "10088402",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Chloroform-D1 (CDCl3)",
        citation: "Standard spectral catalogue",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 },
          { atomIndex: 2, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 },
          { atomIndex: 3, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 },
          { atomIndex: 4, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 },
          { atomIndex: 5, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 },
          { atomIndex: 6, atomSymbol: "H", shift: 7.27, multiplicity: "s", hoseCode: "H-C-6*(C//)", spheres: 3, method: "1H NN", confidence: 98.9 }
        ]
      }
    ]
  },
  {
    id: "10027409",
    name: "Cyclohexane",
    formula: "C6H12",
    smiles: "C1CCCCC1",
    canonicalSmiles: "C1CCCCC1",
    inchi: "InChI=1S/C6H12/c1-2-4-6-5-3-1/h1-6H2",
    inchiKey: "XDTMQSROBMDMFD-UHFFFAOYSA-N",
    cas: "110-82-7",
    molecularWeight: 84.16,
    dateAdded: "2024-02-01",
    contributor: "Stefan Kuhn",
    description: "Symmetric cycloalkane chair conformation displaying characteristic methylene shifts.",
    spectra: [
      {
        id: "10088403",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 },
          { atomIndex: 2, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 },
          { atomIndex: 3, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 },
          { atomIndex: 4, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 },
          { atomIndex: 5, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 },
          { atomIndex: 6, atomSymbol: "C", shift: 27.1, multiplicity: "t", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.6 }
        ]
      },
      {
        id: "10088404",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 1.44, multiplicity: "m", spheres: 3, method: "1H NN", confidence: 97.0 }
        ]
      }
    ]
  },
  {
    id: "10027410",
    name: "Butane",
    formula: "C4H10",
    smiles: "CCCC",
    canonicalSmiles: "CCCC",
    inchi: "InChI=1S/C4H10/c1-3-4-2/h3-4H2,1-2H3",
    inchiKey: "IJDNQMDRQITEOD-UHFFFAOYSA-N",
    cas: "106-97-8",
    molecularWeight: 58.12,
    dateAdded: "2024-02-10",
    contributor: "shk3",
    description: "Linear alkane exhibiting methyl and methylene peaks.",
    spectra: [
      {
        id: "10088405",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 13.2, multiplicity: "q", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.1 },
          { atomIndex: 2, atomSymbol: "C", shift: 25.0, multiplicity: "t", hoseCode: "C-4;(C,C)", spheres: 3, method: "13C NN", confidence: 97.9 },
          { atomIndex: 3, atomSymbol: "C", shift: 25.0, multiplicity: "t", hoseCode: "C-4;(C,C)", spheres: 3, method: "13C NN", confidence: 97.9 },
          { atomIndex: 4, atomSymbol: "C", shift: 13.2, multiplicity: "q", hoseCode: "C-4;(C/C)", spheres: 3, method: "13C NN", confidence: 98.1 }
        ]
      },
      {
        id: "10088406",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 0.88, multiplicity: "t", intensity: 6, method: "1H NN", confidence: 97.5 },
          { atomIndex: 2, atomSymbol: "H", shift: 1.30, multiplicity: "m", intensity: 4, method: "1H NN", confidence: 96.8 }
        ]
      }
    ]
  },
  {
    id: "10027411",
    name: "Ethanol",
    formula: "C2H6O",
    smiles: "CCO",
    canonicalSmiles: "CCO",
    inchi: "InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3",
    inchiKey: "LFQSCWFLJHTTHZ-UHFFFAOYSA-N",
    cas: "64-17-5",
    molecularWeight: 46.07,
    dateAdded: "2024-02-14",
    contributor: "nmrshiftdb-curator",
    description: "Primary alcohol with characteristic deshielded alpha-carbon peak.",
    spectra: [
      {
        id: "10088407",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 18.2, multiplicity: "q", hoseCode: "C-4;(C/O)", spheres: 2, method: "13C NN", confidence: 98.3 },
          { atomIndex: 2, atomSymbol: "C", shift: 58.3, multiplicity: "t", hoseCode: "C-4;(O/C)", spheres: 2, method: "13C NN", confidence: 98.7 }
        ]
      },
      {
        id: "10088408",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 1.18, multiplicity: "t", intensity: 3, method: "1H NN", confidence: 97.4 },
          { atomIndex: 2, atomSymbol: "H", shift: 3.65, multiplicity: "q", intensity: 2, method: "1H NN", confidence: 98.1 },
          { atomIndex: 3, atomSymbol: "H", shift: 2.50, multiplicity: "s", intensity: 1, method: "1H NN", confidence: 94.0 }
        ]
      }
    ]
  },
  {
    id: "10027413",
    name: "Acetone",
    formula: "C3H6O",
    smiles: "CC(=O)C",
    canonicalSmiles: "CC(=O)C",
    inchi: "InChI=1S/C3H6O/c1-3(2)4/h1-2H3",
    inchiKey: "CSCPPACGZOOCGX-UHFFFAOYSA-N",
    cas: "67-64-1",
    molecularWeight: 58.08,
    dateAdded: "2024-02-20",
    contributor: "shk3",
    description: "Simple ketone exhibiting characteristic carbonyl chemical shift >200 ppm.",
    spectra: [
      {
        id: "10088411",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 30.6, multiplicity: "q", method: "13C NN", confidence: 98.4 },
          { atomIndex: 2, atomSymbol: "C", shift: 206.3, multiplicity: "s", method: "13C NN", confidence: 99.2 },
          { atomIndex: 3, atomSymbol: "C", shift: 30.6, multiplicity: "q", method: "13C NN", confidence: 98.4 }
        ]
      },
      {
        id: "10088412",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 2.16, multiplicity: "s", intensity: 6, method: "1H NN", confidence: 98.6 }
        ]
      }
    ]
  },
  {
    id: "10027414",
    name: "Toluene",
    formula: "C7H8",
    smiles: "Cc1ccccc1",
    canonicalSmiles: "Cc1ccccc1",
    inchi: "InChI=1S/C7H8/c1-7-5-3-2-4-6-7/h2-6H,1H3",
    inchiKey: "YXFVVABEGXRONW-UHFFFAOYSA-N",
    cas: "108-88-3",
    molecularWeight: 92.14,
    dateAdded: "2024-02-25",
    contributor: "nmrshiftdb-curator",
    description: "Mono-substituted methylbenzene with splitting of ortho, meta, and para carbons.",
    spectra: [
      {
        id: "10088413",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 21.5, multiplicity: "q", method: "13C NN", confidence: 98.0 },
          { atomIndex: 2, atomSymbol: "C", shift: 137.8, multiplicity: "s", method: "13C NN", confidence: 97.6 },
          { atomIndex: 3, atomSymbol: "C", shift: 129.1, multiplicity: "d", method: "13C NN", confidence: 98.4 },
          { atomIndex: 4, atomSymbol: "C", shift: 128.3, multiplicity: "d", method: "13C NN", confidence: 98.5 },
          { atomIndex: 5, atomSymbol: "C", shift: 125.4, multiplicity: "d", method: "13C NN", confidence: 98.1 }
        ]
      },
      {
        id: "10088414",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 2.34, multiplicity: "s", intensity: 3, method: "1H NN", confidence: 97.8 },
          { atomIndex: 2, atomSymbol: "H", shift: 7.15, multiplicity: "m", intensity: 3, method: "1H NN", confidence: 96.9 },
          { atomIndex: 3, atomSymbol: "H", shift: 7.24, multiplicity: "m", intensity: 2, method: "1H NN", confidence: 97.2 }
        ]
      }
    ]
  },
  {
    id: "10027415",
    name: "Pyridine",
    formula: "C5H5N",
    smiles: "c1ccncc1",
    canonicalSmiles: "c1ccncc1",
    inchi: "InChI=1S/C5H5N/c1-2-4-6-5-3-1/h1-5H",
    inchiKey: "JUJWKBNWVOQZZN-UHFFFAOYSA-N",
    cas: "110-86-1",
    molecularWeight: 79.10,
    dateAdded: "2024-03-01",
    contributor: "Stefan Kuhn",
    description: "Six-membered aromatic nitrogen heterocycle showing characteristic alpha-deshielding.",
    spectra: [
      {
        id: "10088415",
        nucleus: "13C",
        frequencyMHz: 100.6,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "C", shift: 149.9, multiplicity: "d", method: "13C NN", confidence: 98.9 },
          { atomIndex: 2, atomSymbol: "C", shift: 123.8, multiplicity: "d", method: "13C NN", confidence: 98.2 },
          { atomIndex: 3, atomSymbol: "C", shift: 135.9, multiplicity: "d", method: "13C NN", confidence: 98.4 }
        ]
      },
      {
        id: "10088416",
        nucleus: "1H",
        frequencyMHz: 400.13,
        solvent: "Any",
        peaks: [
          { atomIndex: 1, atomSymbol: "H", shift: 8.60, multiplicity: "d", intensity: 2, method: "1H NN", confidence: 98.3 },
          { atomIndex: 2, atomSymbol: "H", shift: 7.28, multiplicity: "m", intensity: 2, method: "1H NN", confidence: 97.6 },
          { atomIndex: 3, atomSymbol: "H", shift: 7.68, multiplicity: "m", intensity: 1, method: "1H NN", confidence: 97.9 }
        ]
      }
    ]
  }
];

export const SPECTRUM_TYPES = [
  "13C (using HOSE code)",
  "13C NN",
  "1H (using HOSE code)",
  "1H NN",
  "15N",
  "31P",
  "19F",
  "13C/1H correlation",
  "29Si"
];

export const SOLVENTS = [
  "Any",
  "Chloroform-D1 (CDCl3)",
  "Dimethylsulphoxide-D6 (DMSO-D6, C2D6SO)",
  "Methanol-D4 (CD3OD)",
  "Deuteriumoxide (D2O)",
  "Acetone-D6 ((CD3)2CO)",
  "TETRACHLORO-METHANE (CCl4)",
  "Pyridin-D5 (C5D5N)",
  "Benzene-D6 (C6D6)"
];
