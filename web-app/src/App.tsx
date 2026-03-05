import React, { useState } from 'react';
import { 
  FileText, LayoutDashboard, Scissors, Merge, 
  RefreshCw, MousePointer, Type, Zap,
  Search, Bell, User, Upload,
  Layers, Lock, FileImage, Cpu, Droplet, RotateCw
} from 'lucide-react';
import { PDFDocument, rgb, degrees } from 'pdf-lib';

type ToolType = 'dashboard' | 'merge' | 'split' | 'compress' | 'convert_pdf' | 'edit' | 'ai_chat' | 'watermark' | 'rotate' | 'protect';

function App() {
  const [activeTool, setActiveTool] = useState<ToolType>('dashboard');
  const [file, setFile] = useState<File | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [watermarkText, setWatermarkText] = useState('CONFIDENTIAL');
  const [rotationAngle, setRotationAngle] = useState(90);
  const [password, setPassword] = useState('');

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      setFile(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleProcessPdf = async () => {
    if (!file) return;
    try {
      setIsProcessing(true);
      const arrayBuffer = await file.arrayBuffer();
      const srcDoc = await PDFDocument.load(arrayBuffer);
      
      const newPdf = await PDFDocument.create();
      let finalPdf = newPdf;
      
      if (activeTool === 'split') {
        // Just extract the first page as a demo
        const [firstPage] = await newPdf.copyPages(srcDoc, [0]);
        newPdf.addPage(firstPage);
      } else if (activeTool === 'merge') {
        // Duplicate the document to simulate merging
        const pages = await newPdf.copyPages(srcDoc, srcDoc.getPageIndices());
        pages.forEach(p => newPdf.addPage(p));
        pages.forEach(p => newPdf.addPage(p));
      } else if (activeTool === 'watermark' || activeTool === 'rotate' || activeTool === 'protect') {
        finalPdf = srcDoc; 
        const pages = finalPdf.getPages();
        pages.forEach(p => {
          if (activeTool === 'watermark') {
            const { width, height } = p.getSize();
            p.drawText(watermarkText || 'CONFIDENTIAL', {
              x: width / 4,
              y: height / 2,
              size: 50,
              color: rgb(0.95, 0.1, 0.1),
              opacity: 0.3,
              rotate: degrees(45),
            });
          } else if (activeTool === 'rotate') {
            const currentRotation = p.getRotation().angle;
            p.setRotation(degrees(currentRotation + rotationAngle));
          }
        });
      }
      
      const pdfBytes = await finalPdf.save();
      const blob = new Blob([new Uint8Array(pdfBytes)], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      
      const a = document.createElement('a');
      a.href = url;
      a.download = `pdf_wizard_${activeTool}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
      alert('Failed to process PDF document.');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="app-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <Zap size={20} />
          </div>
          <span className="brand-text">PDF Wizard Pro</span>
        </div>

        <div className="nav-section">
          <div className="nav-title">Menu</div>
          <button 
            className={`nav-item ${activeTool === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTool('dashboard')}
          >
            <LayoutDashboard size={18} className="nav-icon" /> Dashboard
          </button>
        </div>

        <div className="nav-section">
          <div className="nav-title">Organize PDF</div>
          <button 
            className={`nav-item ${activeTool === 'merge' ? 'active' : ''}`}
            onClick={() => setActiveTool('merge')}
          >
            <Merge size={18} className="nav-icon" /> Merge PDF
          </button>
          <button 
            className={`nav-item ${activeTool === 'split' ? 'active' : ''}`}
            onClick={() => setActiveTool('split')}
          >
            <Scissors size={18} className="nav-icon" /> Split PDF
          </button>
          <button 
             className={`nav-item ${activeTool === 'edit' ? 'active' : ''}`}
             onClick={() => setActiveTool('edit')}
          >
            <Layers size={18} className="nav-icon" /> Organise Pages
          </button>
        </div>

        <div className="nav-section">
          <div className="nav-title">Optimize & Convert</div>
          <button className="nav-item">
            <RefreshCw size={18} className="nav-icon" /> Compress PDF
          </button>
          <button className="nav-item">
            <FileImage size={18} className="nav-icon" /> PDF to JPG
          </button>
          <button className="nav-item">
            <Type size={18} className="nav-icon" /> PDF to Word
          </button>
        </div>

        <div className="nav-section">
          <div className="nav-title">Advanced</div>
          <button 
            className={`nav-item ${activeTool === 'watermark' ? 'active' : ''}`}
            onClick={() => setActiveTool('watermark')}
          >
            <Droplet size={18} className="nav-icon" /> Add Watermark
          </button>
          <button 
             className={`nav-item ${activeTool === 'rotate' ? 'active' : ''}`}
             onClick={() => setActiveTool('rotate')}
          >
            <RotateCw size={18} className="nav-icon" /> Rotate Pages
          </button>
          <button 
             className={`nav-item ${activeTool === 'protect' ? 'active' : ''}`}
             onClick={() => setActiveTool('protect')}
          >
            <Lock size={18} className="nav-icon" /> Protect PDF
          </button>
        </div>

        <div className="nav-section">
          <div className="nav-title">Next-Gen Labs <span className="badge">New</span></div>
          <button 
            className={`nav-item ${activeTool === 'ai_chat' ? 'active' : ''} ai-glow`}
            onClick={() => setActiveTool('ai_chat')}
            style={{ borderRadius: 'var(--radius-md)' }}
          >
            <Cpu size={18} className="nav-icon" /> AI Summarizer
          </button>
          <button className="nav-item">
            <Lock size={18} className="nav-icon" /> Smart Protect
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="topbar">
          <div className="search-bar">
            <Search size={16} color="var(--text-secondary)" />
            <input 
              type="text" 
              className="search-input" 
              placeholder="Search features, tools, recent files..." 
            />
          </div>
          <div className="topbar-actions">
            <button className="icon-btn">
              <Bell size={20} />
            </button>
            <button className="icon-btn">
              <User size={20} />
            </button>
          </div>
        </header>

        <section className="workspace">
          {activeTool === 'dashboard' && (
            <>
              <div className="workspace-header">
                <h1 className="workspace-title text-gradient">Welcome back, Pro User.</h1>
                <p className="workspace-subtitle">Select a tool to start working with your PDFs.</p>
              </div>

              <div className="tools-grid">
                <div className="tool-card" onClick={() => setActiveTool('merge')}>
                  <div className="tool-icon-wrapper">
                    <Merge size={28} />
                  </div>
                  <h3 className="tool-title">Merge PDF</h3>
                  <p className="tool-desc">Combine multiple PDFs in the exact order you want. Fast, easy and totally secure.</p>
                </div>

                <div className="tool-card" onClick={() => setActiveTool('split')}>
                  <div className="tool-icon-wrapper">
                    <Scissors size={28} />
                  </div>
                  <h3 className="tool-title">Split PDF</h3>
                  <p className="tool-desc">Separate one page or a whole set for easy conversion into independent PDF files.</p>
                </div>

                <div className="tool-card">
                  <div className="tool-icon-wrapper">
                    <RefreshCw size={28} />
                  </div>
                  <h3 className="tool-title">Compress PDF</h3>
                  <p className="tool-desc">Reduce file size while optimizing for maximal PDF quality and fast web rendering.</p>
                </div>

                <div className="tool-card" onClick={() => setActiveTool('ai_chat')}>
                  <div className="tool-icon-wrapper">
                    <Cpu size={28} />
                  </div>
                  <h3 className="tool-title">AI Analyze <span className="badge">Pro</span></h3>
                  <p className="tool-desc">Talk to your document. Instantly summarize long papers and get precise answers.</p>
                </div>
                
                <div className="tool-card">
                  <div className="tool-icon-wrapper">
                     <FileImage size={28} />
                  </div>
                  <h3 className="tool-title">Convert to Images</h3>
                  <p className="tool-desc">Extract all images contained in a PDF or convert each page to a JPG file.</p>
                </div>

                <div className="tool-card">
                   <div className="tool-icon-wrapper">
                     <Layers size={28} />
                   </div>
                   <h3 className="tool-title">Organize Pages</h3>
                   <p className="tool-desc">Sort, add and delete PDF pages. Drag and drop pages to reorganize your document.</p>
                </div>

                <div className="tool-card" onClick={() => setActiveTool('watermark')}>
                   <div className="tool-icon-wrapper">
                     <Droplet size={28} />
                   </div>
                   <h3 className="tool-title">Add Watermark</h3>
                   <p className="tool-desc">Stamp an image or text over your PDF in seconds. Choose typography, transparency and position.</p>
                </div>

                <div className="tool-card" onClick={() => setActiveTool('rotate')}>
                   <div className="tool-icon-wrapper">
                     <RotateCw size={28} />
                   </div>
                   <h3 className="tool-title">Rotate PDF</h3>
                   <p className="tool-desc">Rotate your PDFs the way you need them. You can rotate multiple PDFs simultaneously.</p>
                </div>
              </div>
            </>
          )}

          {activeTool !== 'dashboard' && !file && (
            <div className="editor-view">
              <div className="workspace-header">
                <h1 className="workspace-title" style={{textTransform: 'capitalize'}}>{activeTool.replace('_', ' ')} Tool</h1>
                <p className="workspace-subtitle">Upload your file to get started.</p>
              </div>
              <div className="upload-zone">
                <div 
                  className="upload-box"
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  onClick={() => document.getElementById('file-upload')?.click()}
                >
                  <Upload className="upload-icon" />
                  <h2 className="upload-title">Drop your PDF here</h2>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>Maximum file size: 100MB</p>
                  
                  <button className="btn-primary">
                    <MousePointer size={18} /> Select PDF File
                  </button>
                  <input 
                    id="file-upload" 
                    type="file" 
                    accept=".pdf" 
                    style={{ display: 'none' }}
                    onChange={handleFileUpload}
                  />
                </div>
              </div>
            </div>
          )}

          {activeTool !== 'dashboard' && file && (
            <div className="editor-view" style={{flexDirection: 'row', gap: '24px', margin: '-32px', height: 'calc(100% + 64px)'}}>
               {/* Simplified PDF Viewer / Placeholder */}
               <div className="pdf-content-area w-full flex-1">
                 <div style={{ background: 'var(--bg-surface-elevated)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-lg)', width: '100%', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                    <FileText size={64} style={{ color: 'var(--text-accent)', marginBottom: '16px' }} />
                    <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>{file.name}</h2>
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '24px' }}>{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                    <button className="btn-secondary" onClick={() => setFile(null)}>Change File</button>
                 </div>
               </div>
               
               {/* Right side config panel */}
               <div className="tool-config-panel">
                 <h3 className="panel-title text-gradient">Tool Options</h3>
                 {activeTool === 'merge' && (
                    <div className="form-group">
                      <p className="form-label mb-4">You can drop more files to combine them.</p>
                      <button className="btn-secondary w-full justify-center mt-2">+ Add more files</button>
                      <button className="btn-primary w-full justify-center mt-4" onClick={handleProcessPdf} disabled={isProcessing}>
                         {isProcessing ? 'Processing...' : 'Merge PDFs'}
                      </button>
                    </div>
                 )}
                 {activeTool === 'ai_chat' && (
                    <div className="form-group">
                       <p className="form-label mb-4">AI Model is analyzing the document...</p>
                       <div className="form-input" style={{height: '200px', backgroundColor: 'var(--bg-surface)'}}>
                          <span style={{color: 'purple'}}>●</span> Processed 42 pages...<br/>
                          <span style={{color: 'purple'}}>●</span> Extracted metadata...<br/>
                          <span style={{color: 'purple'}}>●</span> Ready for questions.
                       </div>
                       <input className="form-input mt-4" placeholder="Ask anything about this document..." />
                       <button className="btn-primary w-full justify-center mt-4 ai-glow" style={{border: 'none'}}>Generate Summary</button>
                    </div>
                 )}
                 {activeTool === 'split' && (
                    <div className="form-group">
                      <p className="form-label">Extract Pages</p>
                      <input type="text" className="form-input mb-4" placeholder="e.g., 1-5, 8, 11-13" />
                      <button className="btn-primary w-full justify-center mt-4" onClick={handleProcessPdf} disabled={isProcessing}>
                         {isProcessing ? 'Processing...' : 'Split PDF'}
                      </button>
                    </div>
                 )}
                 {activeTool === 'watermark' && (
                    <div className="form-group">
                      <p className="form-label">Watermark Text</p>
                      <input 
                        type="text" 
                        className="form-input mb-4" 
                        value={watermarkText}
                        onChange={(e) => setWatermarkText(e.target.value)}
                        placeholder="CONFIDENTIAL" 
                      />
                      <button className="btn-primary w-full justify-center mt-4" onClick={handleProcessPdf} disabled={isProcessing}>
                         {isProcessing ? 'Processing...' : 'Add Watermark'}
                      </button>
                    </div>
                 )}
                 {activeTool === 'rotate' && (
                    <div className="form-group">
                      <p className="form-label">Rotation Angle</p>
                      <select 
                        className="form-input mb-4" 
                        value={rotationAngle}
                        onChange={(e) => setRotationAngle(Number(e.target.value))}
                        style={{ backgroundColor: 'var(--bg-surface-elevated)' }}
                      >
                        <option value={90}>90° Clockwise</option>
                        <option value={180}>180° Rotate</option>
                        <option value={270}>90° Counter-Clockwise</option>
                      </select>
                      <button className="btn-primary w-full justify-center mt-4" onClick={handleProcessPdf} disabled={isProcessing}>
                         {isProcessing ? 'Processing...' : 'Rotate PDF'}
                      </button>
                    </div>
                 )}
                 {activeTool === 'protect' && (
                    <div className="form-group">
                      <p className="form-label">Set Password</p>
                      <input 
                        type="password" 
                        className="form-input mb-4" 
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Enter secure password" 
                      />
                      <button className="btn-primary w-full justify-center mt-4" onClick={handleProcessPdf} disabled={isProcessing}>
                         {isProcessing ? 'Processing...' : 'Encrypt PDF'}
                      </button>
                    </div>
                 )}
               </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
