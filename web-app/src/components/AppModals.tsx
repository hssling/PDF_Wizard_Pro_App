import React, { useState } from 'react';
import { Brain, FileImage, Lock, X } from 'lucide-react';

export interface SignaturePadModalProps {
  show: boolean;
  onClose: () => void;
  onSave: () => void;
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
  startDrawing: (e: React.MouseEvent | React.TouchEvent) => void;
  draw: (e: React.MouseEvent | React.TouchEvent) => void;
  stopDrawing: () => void;
  clear: () => void;
  handleImageUpload: (e: React.ChangeEvent<HTMLInputElement>, overlayId?: string) => void;
}

export const SignaturePadModal = ({
  show,
  onClose,
  onSave,
  canvasRef,
  startDrawing,
  draw,
  stopDrawing,
  clear,
  handleImageUpload
}: SignaturePadModalProps) => {
  if (!show) return null;
  return (
    <div className="signature-overlay" onClick={onClose}>
      <div className="signature-pad" onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '1.2rem', color: 'var(--accent-color)' }}>Draw Signature</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#666' }}><X size={20} /></button>
        </div>
        <canvas
          ref={canvasRef}
          className="sig-canvas"
          width={500}
          height={200}
          onMouseDown={startDrawing}
          onMouseMove={draw}
          onMouseUp={stopDrawing}
          onMouseLeave={stopDrawing}
          onTouchStart={startDrawing}
          onTouchMove={draw}
          onTouchEnd={stopDrawing}
        />
        <div style={{ display: 'flex', gap: 12, justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-secondary" onClick={clear}>Clear Drawing</button>
            <label className="btn-secondary" style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', margin: 0 }}>
              <FileImage size={16} />
              Upload Signature
              <input type="file" style={{ display: 'none' }} onChange={(e) => handleImageUpload(e, undefined)} accept="image/*" />
            </label>
          </div>
          <button className="btn-primary" onClick={onSave}>Commit to Document</button>
        </div>
      </div>
    </div>
  );
};

export interface SecurityVaultModalProps {
  show: boolean;
  onClose: () => void;
  onConfirm: (pass: string) => void;
}

export const SecurityVaultModal = ({ show, onClose, onConfirm }: SecurityVaultModalProps) => {
  const [pass, setPass] = useState('');
  if (!show) return null;
  return (
    <div className="signature-overlay" onClick={onClose}>
      <div className="signature-pad" style={{ width: 400 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, color: 'var(--accent-color)' }}><Lock size={20} style={{ verticalAlign: 'middle', marginRight: 8 }} /> Secure Document</h3>
          <X size={20} onClick={onClose} style={{ cursor: 'pointer' }} />
        </div>
        <p style={{ fontSize: 12, color: '#666', margin: '12px 0' }}>Enter a master password to encrypt this document payload. This is required for AES-256 enforcement.</p>
        <input
          type="password"
          autoFocus
          className="property-input"
          style={{ width: '100%', padding: '12px', fontSize: '1rem' }}
          placeholder="Enter Passphrase"
          value={pass}
          onChange={e => setPass(e.target.value)}
        />
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 20 }}>
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={() => onConfirm(pass)} disabled={!pass}>Lock & Process</button>
        </div>
      </div>
    </div>
  );
};

export interface AiSettingsModalProps {
  show: boolean;
  onClose: () => void;
  config: { provider: 'gemini' | 'openai' | 'anthropic' | 'qwen' | 'none', apiKey: string };
  onSave: (config: { provider: 'gemini' | 'openai' | 'anthropic' | 'qwen' | 'none', apiKey: string }) => void;
}

export const AiSettingsModal = ({ show, onClose, config, onSave }: AiSettingsModalProps) => {
  const [provider, setProvider] = useState(config.provider);
  const [apiKey, setApiKey] = useState(config.apiKey);

  if (!show) return null;

  return (
    <div className="signature-overlay" onClick={onClose} style={{ zIndex: 10000 }}>
      <div className="signature-pad" style={{ width: 500 }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ margin: 0, color: 'var(--accent-color)' }}><Brain size={20} style={{ verticalAlign: 'middle', marginRight: 8 }} /> Secure Engine Configuration</h3>
          <X size={20} onClick={onClose} style={{ cursor: 'pointer' }} />
        </div>

        <div className="control-group" style={{ marginBottom: 16 }}>
          <label style={{ fontSize: 13, fontWeight: 600 }}>Intelligence Provider</label>
          <select className="property-input" value={provider} onChange={e => { setProvider(e.target.value as 'gemini' | 'openai' | 'anthropic' | 'qwen' | 'none'); setApiKey(''); }} style={{ width: '100%', marginTop: 8 }}>
            <option value="none">Select Provider</option>
            <option value="gemini">Google Gemini (Gemini 2.5 Flash)</option>
            <option value="openai">OpenAI (GPT-4o)</option>
            <option value="anthropic">Anthropic (Claude 3.5 Sonnet)</option>
            <option value="qwen">HuggingFace Serverless (Qwen 72B Instruct)</option>
          </select>
        </div>

        <div className="control-group" style={{ marginBottom: 8 }}>
          <label style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, fontWeight: 600 }}>
            API Key / Access Token
            {provider === 'gemini' && <a href="https://aistudio.google.com/app/apikey" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-color)', textDecoration: 'none' }}>Get Gemini Key</a>}
            {provider === 'openai' && <a href="https://platform.openai.com/api-keys" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-color)', textDecoration: 'none' }}>Get OpenAI Key</a>}
            {provider === 'anthropic' && <a href="https://console.anthropic.com/settings/keys" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-color)', textDecoration: 'none' }}>Get Anthropic Key</a>}
            {provider === 'qwen' && <a href="https://huggingface.co/settings/tokens" target="_blank" rel="noreferrer" style={{ color: 'var(--accent-color)', textDecoration: 'none' }}>Get HF Token</a>}
          </label>
          <input
            type="password"
            className="property-input"
            style={{ width: '100%', padding: '12px', fontSize: '1rem', marginTop: 8 }}
            placeholder={provider === 'none' ? 'Select a provider first' : 'Enter Private Key...'}
            value={apiKey}
            onChange={e => setApiKey(e.target.value)}
            disabled={provider === 'none'}
          />
        </div>
        <p style={{ fontSize: 11, color: 'var(--text-secondary)', marginBottom: 24 }}>Tokens are processed locally within your browser context (localStorage). No data is stored or transmitted by WizardPro. Direct API connection is established only with your selected provider.</p>

        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={() => { onSave({ provider, apiKey }); onClose(); }} disabled={!apiKey && provider !== 'none'}>Secure Link</button>
        </div>
      </div>
    </div>
  );
};
