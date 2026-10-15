import { execFile } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import os from 'os';
import { NMRPeak } from './nmrData.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export interface GnnResult {
  success: boolean;
  smiles: string;
  nucleus: string;
  solvent: string;
  formula: string;
  method: string;
  peaks: NMRPeak[];
  runtime_ms: number;
  gnnDetails?: {
    model: string;
    checkpoint: string;
    atomsEvaluated: number;
    maxAtomsSupported: number;
  };
  error?: string;
  command?: string;
  exitCode?: number | null;
  stderr?: string;
  stdout?: string;
}

const SCRIPT_PATH = path.join(__dirname, 'gnn', 'predict_direct.py');
const CHECKPOINT_PATH = path.join(__dirname, 'gnn', 'models', 'default_13C.checkpoint');

export function isGnnAvailable(nucleus: string = '13C'): boolean {
  return nucleus === '13C' && fs.existsSync(CHECKPOINT_PATH) && fs.existsSync(SCRIPT_PATH);
}

export function runGnnPrediction(
  smiles: string,
  nucleus: string = '13C',
  solvent: string = 'Any',
  molfile?: string
): Promise<GnnResult> {
  return new Promise((resolve) => {
    const cleanSmiles = (smiles || '').trim();
    const args: string[] = [
      SCRIPT_PATH,
      '--pred', nucleus,
      '--solvent', solvent
    ];

    if (cleanSmiles) {
      args.push('--smiles', cleanSmiles);
    }

    let tempMolPath: string | null = null;
    if (molfile && molfile.trim()) {
      try {
        tempMolPath = path.join(os.tmpdir(), `mol_${Date.now()}_${Math.random().toString(36).substring(2, 7)}.mol`);
        fs.writeFileSync(tempMolPath, molfile, 'utf8');
        args.push('--molfile', tempMolPath);
      } catch (e) {
        console.warn('[GNN] Failed to write temp molfile:', e);
      }
    }

    const commandStr = `python3 ${SCRIPT_PATH} ${args.slice(1).join(' ')}`;

    execFile('python3', args, { timeout: 20000, maxBuffer: 10 * 1024 * 1024 }, (err, stdout, stderr) => {
      // Clean up temp molfile if created
      if (tempMolPath && fs.existsSync(tempMolPath)) {
        try { fs.unlinkSync(tempMolPath); } catch {}
      }

      if (err) {
        console.error(`[GNN Python Error] Command failed: ${commandStr}`);
        console.error(`[GNN Python Error] Stderr: ${stderr || err.message}`);
        return resolve({
          success: false,
          smiles,
          nucleus,
          solvent,
          formula: smiles,
          method: `${nucleus} GNN`,
          peaks: [],
          runtime_ms: 0,
          command: commandStr,
          exitCode: (err as any).code !== undefined ? (typeof (err as any).code === 'number' ? (err as any).code : 1) : 1,
          stderr: (stderr && stderr.trim()) || err.message,
          stdout: (stdout && stdout.trim()) || '',
          error: (stderr && stderr.trim()) || err.message || 'Python execution failed'
        });
      }

      try {
        const parsed = JSON.parse(stdout);
        if (parsed.success === false) {
          return resolve({
            ...parsed,
            command: commandStr,
            exitCode: 1,
            stderr: (stderr && stderr.trim()) || parsed.traceback || parsed.error || 'Python returned success=false',
            stdout: stdout.trim(),
            error: parsed.error || 'Python returned success=false'
          });
        }
        resolve({
          ...parsed,
          command: commandStr,
          exitCode: 0,
          stderr: (stderr && stderr.trim()) || ''
        });
      } catch (parseErr: any) {
        console.error(`[GNN] JSON parse error:`, parseErr.message, stdout);
        resolve({
          success: false,
          smiles,
          nucleus,
          solvent,
          formula: smiles,
          method: `${nucleus} GNN`,
          peaks: [],
          runtime_ms: 0,
          command: commandStr,
          exitCode: 1,
          stderr: (stderr && stderr.trim()) || stdout || 'Invalid output from Python process',
          stdout: (stdout && stdout.trim()) || '',
          error: `Failed to parse Python GNN output: ${parseErr.message}\n${stderr || stdout}`
        });
      }
    });
  });
}
