import React, { useState } from 'react';
import { Play, Send, Settings, Maximize2 } from 'lucide-react';
import { Button } from './Button';
interface CodeEditorProps {
  onRun: (code: string, lang: string) => void;
  onSubmit: (code: string, lang: string) => void;
  isSubmitting: boolean;
  isRunning: boolean;
}
const LANGUAGES = ['Python', 'Java', 'C++', 'JavaScript', 'Go'];
const DEFAULT_CODE: Record<string, string> = {
  Python:
  'class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        # Write your code here\n        pass',
  JavaScript:
  '/**\n * @param {number[]} nums\n * @param {number} target\n * @return {number[]}\n */\nvar twoSum = function(nums, target) {\n    // Write your code here\n};',
  Java: 'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your code here\n        return new int[]{};\n    }\n}',
  'C++':
  'class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Write your code here\n        return {};\n    }\n};',
  Go: 'func twoSum(nums []int, target int) []int {\n    // Write your code here\n    return nil\n}'
};
export function CodeEditor({
  onRun,
  onSubmit,
  isSubmitting,
  isRunning
}: CodeEditorProps) {
  const [language, setLanguage] = useState('Python');
  const [code, setCode] = useState(DEFAULT_CODE['Python']);
  const handleLanguageChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newLang = e.target.value;
    setLanguage(newLang);
    setCode(DEFAULT_CODE[newLang] || '');
  };
  // Generate mock line numbers
  const lineCount = code.split('\n').length;
  const lines = Array.from(
    {
      length: Math.max(20, lineCount)
    },
    (_, i) => i + 1
  );
  return (
    <div className="flex flex-col h-full bg-[#1e1e1e] rounded-lg overflow-hidden border border-slate-700 shadow-xl">
      {/* Editor Toolbar */}
      <div className="flex items-center justify-between px-4 py-2 bg-[#2d2d2d] border-b border-slate-700">
        <div className="flex items-center space-x-4">
          <select
            value={language}
            onChange={handleLanguageChange}
            className="bg-[#3c3c3c] text-slate-200 text-sm rounded px-2 py-1 border-none focus:ring-1 focus:ring-primary-dark outline-none cursor-pointer">
            
            {LANGUAGES.map((lang) =>
            <option key={lang} value={lang}>
                {lang}
              </option>
            )}
          </select>
          <div className="h-4 w-px bg-slate-600" />
          <button className="text-slate-400 hover:text-slate-200 transition-colors">
            <Settings className="w-4 h-4" />
          </button>
        </div>
        <div className="flex items-center space-x-2">
          <button className="text-slate-400 hover:text-slate-200 transition-colors p-1">
            <Maximize2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Editor Area */}
      <div className="flex-1 flex overflow-hidden relative">
        {/* Line Numbers */}
        <div className="w-12 bg-[#1e1e1e] border-r border-slate-800 flex flex-col items-end py-4 pr-3 select-none overflow-hidden text-slate-500 font-mono text-sm">
          {lines.map((line) =>
          <div key={line} className="leading-6">
              {line}
            </div>
          )}
        </div>

        {/* Text Area */}
        <textarea
          value={code}
          onChange={(e) => setCode(e.target.value)}
          spellCheck={false}
          className="flex-1 bg-transparent text-slate-300 font-mono text-sm p-4 leading-6 resize-none outline-none whitespace-pre overflow-auto"
          style={{
            tabSize: 4
          }} />
        
      </div>

      {/* Bottom Actions */}
      <div className="flex items-center justify-end px-4 py-3 bg-[#2d2d2d] border-t border-slate-700 space-x-3">
        <Button
          variant="secondary"
          size="sm"
          leftIcon={<Play className="w-4 h-4" />}
          onClick={() => onRun(code, language)}
          isLoading={isRunning}
          disabled={isSubmitting}
          className="bg-[#3c3c3c] text-slate-200 border-none hover:bg-[#4c4c4c]">
          
          Run Code
        </Button>
        <Button
          variant="primary"
          size="sm"
          leftIcon={<Send className="w-4 h-4" />}
          onClick={() => onSubmit(code, language)}
          isLoading={isSubmitting}
          disabled={isRunning}
          className="bg-emerald-600 hover:bg-emerald-700 text-white">
          
          Submit
        </Button>
      </div>
    </div>);

}