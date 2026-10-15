# NMRShiftDB2: Porting Original 13C NN, 1H NN and HOSE Predictor Engine

Comprehensive implementation plan to extract, filter, and port the authentic Java/CDK/Weka neural network and HOSE-code prediction engine from `nmrshiftdb2-svn4git` into a native TypeScript/Node.js engine within AI Studio, avoiding the 1,000-file repository limit.

## User Review & Critical Decisions

> [!IMPORTANT]
> Key decisions confirmed during clarification:
> - **Source Delivery**: The original Java predictor files will be isolated into a dedicated Git branch or streamlined repository containing solely the predictor source files (excluding 1,875 DocBook documentation files and build artifacts to stay well under the 1,000-file platform quota).
> - **Execution Target**: CDK descriptor extraction and Weka multilayer perceptron inference will be ported directly to native TypeScript/Node.js for zero-dependency, low-latency execution inside AI Studio.
> - **Functional Scope**: Both **13C NN** and **1H NN**, complemented by the hierarchical **HOSE-code** sphere database lookup.

### What We Need to Know About the Repository to Complete the Port

To accurately extract and port every neural network weight and descriptor calculation, the following repository components are required:

1. **Path to the Predictor Java Packages**: The exact directory of `org.openscience.nmrshiftdb2.util` containing `NmrshiftdbPredict.java`, `WekaPredictor.java`, and `ExtendedHOSECodeGenerator.java`.
2. **Pretrained Weka Model Files**: Location of the serialized neural network models (e.g. `.model` binary or text exports) or weight matrices used by Weka's `MultilayerPerceptron` for 13C and 1H.
3. **HOSE Code Database Dump / Subset**: The reference table format for HOSE code lookup (Bremser sphere codes and associated chemical shift distributions).

---

## 1. Overview & Core Concept

NMRShiftDB2 relies on two complementary prediction engines:
- **Neural Network Prediction (13C NN & 1H NN)**: A multi-layer feedforward neural network trained on topological, spatial, and electronic atomic descriptors (hybridization $sp/sp^2/sp^3$, charge, electronegativities, substituents, and topological distances).
- **HOSE Code Sphere Lookup**: Hierarchical Organization of Spherical Environments (Bremser spheres 1 through 6) matching structural environments against verified experimental shift distributions.

By stripping out 1,875 `docbook-xsl` files and legacy portal templates, the predictor core fits easily within AI Studio's 1,000-file boundary while running 100% natively in Node.js 22.

---

## 2. User Experience & Visual Design

The UI preserves the authentic NMRShiftDB2 layout while providing live inspection into the neural network evaluation:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ NMRShiftDB   Registered: 3297 | Structures: 271,817 | Spectra: 466,613       │
│ [ Home ] [ Search ] [ Quick Check ] [ Predict (Active) ] [ API Docs ]       │
├─────────────────────────────────────────────────────────────────────────────┤
│ Predict an NMR Spectrum                                                     │
│ ┌───────────────────────────────────────┐                                   │
│ │ 2D Chemical Canvas                    │ [ H ] [ C ] [ N ] [ O ]           │
│ │ H3C - CH2 - O - C(=O) - CH3           │ [ S ] [ P ] [ F ] [ Cl ]          │
│ └───────────────────────────────────────┘                                   │
│ Method: [ 13C NN ▾ ]   Solvent: [ Any ▾ ]   [ Predict Spectrum ]            │
├─────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 13C NMR Spectrum Display (ppm scale: 220 ppm ─── 0 ppm relative to TMS) │ │
│ │ Signals: 161.72 (C=O) | 76.85 (-CH2-) | 48.46 (-CH3) | 38.19 (CH3-C=O)  │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│ Neural Network Inspector: 18 Descriptors → Hidden [24, 12] → Shift Output   │
└─────────────────────────────────────────────────────────────────────────────┘
```

- **Visual Fidelity**: Retains the authentic portal design (yellow active tab, blue action buttons, and red/blue logo).
- **Interactive Inspector**: Displays each carbon's computed topological descriptors (sigma/pi electronegativity, sphere neighbors) alongside the predicted shift and uncertainty.

---

## 3. Key Technical Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale | Alternatives Considered |
|---|---|---|---|
| **Repository Filtering** | Sparse branch containing only predictor files (<100 files) | Stays safely below AI Studio's 1,000-file ceiling without touching documentation | Importing full repo (truncated at 1,000 files, missing Java source) |
| **Runtime Porting** | Native TypeScript CDK/Weka transpilation | Fast, deterministic, zero JVM overhead in Node.js container | WebAssembly JVM (CheerpJ) — large initial payload and memory overhead |
| **HOSE Code DB Storage** | Embedded SQLite or compressed JSON HOSE index | Instant query times without needing an external MariaDB server | External MySQL connection (blocked by sandbox TCP constraints) |

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Client Interface (React + Vite)                    │
│  - 2D Molecule Canvas (SMILES/Molfile)                                  │
│  - Interactive Stick Spectrum Canvas (PPM Axis)                         │
│  - Shift Attribution & Descriptor Inspection Table                      │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ HTTP POST /predict
┌────────────────────────────────────▼────────────────────────────────────┐
│                  Node.js / Express Server (server.ts)                   │
├─────────────────────────────────────────────────────────────────────────┤
│ 1. Chemical Graph Parsing                                               │
│    - Bond perception, atom typing, aromaticity, and valence calculation │
├─────────────────────────────────────────────────────────────────────────┤
│ 2. Descriptor Extraction Engine (Ported from CDK / header-1.arff)       │
│    - pkc_sigmaEN, pkc_piEN, hybridization (sp/sp2/sp3)                  │
│    - Topological substituents, valence electrons, distance matrix       │
├─────────────────────────────────────────────────────────────────────────┤
│ 3. Weka MLP Forward Pass (server/wekaNeuralNetwork.ts)                  │
│    - Input feature normalization (normalizer.xml parameters)            │
│    - Sigmoid/Tanh hidden layer forward activations                      │
│    - De-normalized shift output (ppm) & confidence bounds               │
├─────────────────────────────────────────────────────────────────────────┤
│ 4. HOSE-Code Engine (server/extendedHoseCode.ts)                        │
│    - Bremser sphere generation (1–6 spheres) with prime hashing         │
│    - Exact & partial match fallback from compressed sphere store        │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step-by-Step Execution Plan

1. **Step 1: Repository Isolation**
   - Provide or create a clean Git branch with only `org/openscience/nmrshiftdb2/` Java files relevant to prediction (`ExtendedHOSECodeGenerator`, `Prediction`, `WekaPredictor`, and ARFF configurations).
2. **Step 2: TypeScript Descriptor Engine**
   - Port molecular graph traversal and the 18 atom descriptors defined in `header-1.arff` and `header.arff`.
3. **Step 3: Neural Network Weight Extraction & Forward Pass**
   - Transcribe the Weka `MultilayerPerceptron` weights and normalization constants into a pure TypeScript matrix multiplier.
4. **Step 4: HOSE Code Generation & Database Matching**
   - Implement the Bremser sphere generation algorithm using `primes.dat` hashing, coupled with the sphere lookup table.
5. **Step 5: Verification & Calibration**
   - Benchmark against established reference molecules (ethyl acetate, benzene, pyridine, etc.) ensuring identical numerical outputs to the original Java portal.
