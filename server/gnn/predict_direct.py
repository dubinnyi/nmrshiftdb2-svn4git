#!/usr/bin/env python3
"""
Direct prediction runner for NMRShiftDB2 GNN predictor (Jonas & Kuhn / ResPredict).
Uses default_13C.checkpoint with proper 2D molecular embedding to generate accurate NMR chemical shifts.
"""

import os
import sys
import json
import argparse
import time
import warnings

warnings.filterwarnings("ignore")
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, SCRIPT_DIR)

from rdkit import Chem
from rdkit.Chem import AllChem
import torch
import predict_standalone
import predwrap

_PREDICTOR_CACHE = {}

def get_cached_predictor(nucleus="13C"):
    if nucleus in _PREDICTOR_CACHE:
        return _PREDICTOR_CACHE[nucleus]
    
    meta_path = os.path.join(SCRIPT_DIR, "models", f"default_{nucleus}.meta")
    ckpt_path = os.path.join(SCRIPT_DIR, "models", f"default_{nucleus}.checkpoint")
    if not os.path.exists(ckpt_path):
        raise FileNotFoundError(f"Model checkpoint not found: {ckpt_path}")
    
    predictor = predwrap.PredModel(
        meta_path,
        ckpt_path,
        USE_CUDA=False,
        override_pred_config={}
    )
    _PREDICTOR_CACHE[nucleus] = (predictor, meta_path)
    return predictor, meta_path

def predict_structure(smiles=None, molfile=None, nucleus="13C", solvent="Any"):
    t_start = time.time()
    
    mol = None
    if molfile:
        if os.path.isfile(molfile):
            with open(molfile, "r") as f:
                content = f.read()
            mol = Chem.MolFromMolBlock(content, removeHs=False)
        else:
            mol = Chem.MolFromMolBlock(molfile, removeHs=False)
            
    if mol is None and smiles:
        mol = Chem.MolFromSmiles(smiles)
        if mol is not None:
            AllChem.Compute2DCoords(mol)
            
    if mol is None:
        raise ValueError(f"Failed to parse molecular structure from SMILES: '{smiles}' or Molfile input")
    
    predictor, meta_path = get_cached_predictor(nucleus)
    max_n = predictor.meta.get("max_n", 128)
    
    if mol.GetNumAtoms() > max_n:
        raise ValueError(f"Molecule has {mol.GetNumAtoms()} atoms, exceeding maximum of {max_n}")
    
    # Run Jonas & Kuhn's predict_mols pipeline
    res_list = predict_standalone.predict_mols(
        [mol],
        predictor,
        max_n,
        to_pred=nucleus,
        add_h=True,
        sanitize=True,
        add_default_conf=False,
        num_workers=0
    )
    
    if not res_list or not res_list[0].get("success"):
        raise RuntimeError("GNN prediction returned empty result")
    
    raw_shifts = res_list[0].get(f"shifts_{nucleus}", [])
    
    peaks = []
    for s in raw_shifts:
        atom_idx = int(s["atom_idx"])
        atom_symbol = "C" if nucleus == "13C" else "H"
        shift_val = float(s["pred_mu"])
        std_val = float(s["pred_std"])
        
        # Solvent delta if specified
        if "DMSO" in solvent:
            shift_val += 0.5
        elif "CD3OD" in solvent:
            shift_val += 0.3
            
        peaks.append({
            "atomIndex": atom_idx + 1, # 1-based index matching visual molecular diagram
            "atomSymbol": atom_symbol,
            "shift": round(shift_val, 2),
            "uncertainty": round(std_val, 2),
            "confidence": round(max(50.0, min(99.9, 100.0 - std_val * 4.0)), 1),
            "multiplicity": "s",
            "intensity": 1.0,
            "method": f"{nucleus} GNN (NMRShiftDB2 Retrained)",
            "source": "gnn"
        })
    
    peaks.sort(key=lambda p: p["atomIndex"])
    t_end = time.time()
    
    formula = Chem.rdMolDescriptors.CalcMolFormula(mol)
    
    return {
        "success": True,
        "smiles": smiles or "",
        "nucleus": nucleus,
        "solvent": solvent,
        "formula": formula,
        "method": f"{nucleus} GNN (Jonas & Kuhn 2024 / NMRShiftDB2)",
        "peaks": peaks,
        "runtime_ms": round((t_end - t_start) * 1000, 1),
        "gnnDetails": {
            "model": "GraphVertConfigBootstrapWithMultiMax",
            "checkpoint": f"default_{nucleus}.checkpoint (53 MB)",
            "atomsEvaluated": len(peaks),
            "maxAtomsSupported": max_n
        }
    }

def main():
    parser = argparse.ArgumentParser(description="NMRShiftDB2 GNN Chemical Shift Predictor")
    parser.add_argument("--smiles", type=str, default=None, help="SMILES string to predict")
    parser.add_argument("--molfile", type=str, default=None, help="Molfile path or string")
    parser.add_argument("--pred", type=str, default="13C", help="Nucleus (13C or 1H)")
    parser.add_argument("--solvent", type=str, default="Any", help="Solvent (e.g. CDCl3, DMSO-d6)")
    
    args = parser.parse_args()
    try:
        res = predict_structure(smiles=args.smiles, molfile=args.molfile, nucleus=args.pred, solvent=args.solvent)
        print(json.dumps(res, indent=2))
    except Exception as e:
        import traceback
        traceback.print_exc(file=sys.stderr)
        err_res = {
            "success": False,
            "error": str(e),
            "traceback": traceback.format_exc(),
            "smiles": args.smiles,
            "nucleus": args.pred
        }
        print(json.dumps(err_res, indent=2))
        sys.exit(1)

if __name__ == "__main__":
    main()
